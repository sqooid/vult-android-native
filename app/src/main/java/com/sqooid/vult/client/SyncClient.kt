package com.sqooid.vult.client

import android.content.Context
import android.util.Log
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.DatabaseManager
import com.sqooid.vult.database.MutationType
import com.sqooid.vult.repository.CredentialRepository
import com.sqooid.vult.repository.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Inject

class SyncClient @Inject constructor(
    @ApplicationContext val context: Context,
    val repository: CredentialRepository
) {
    data class ClientParams(
        val host: String,
        val key: String?,
    )

    private var client: HttpClient? = null

    fun getSyncEnabled(): Boolean {
        return context.getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE)
            .getBoolean(Vals.SYNC_ENABLED_KEY, false)
    }

    fun setSyncEnabled( value: Boolean) {
        context.getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit().apply {
            putBoolean(Vals.SYNC_ENABLED_KEY, value)
            apply()
        }
    }

    fun initializeClient(params: ClientParams) {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url(params.host)
                header("Authentication", params.key)
            }
        }
    }

    suspend fun importUser(): String? {
        return try {
            val response: UserImportResponse? = client?.get("user/import")?.body()
            response?.salt
        } catch (e: Exception) {
            null
        }
    }

    suspend fun initializeUser(salt: String): RequestResult {
        try {
            val response: InitializeUserResponse = client?.post("user/init") {
                contentType(ContentType.Application.Json)
                setBody(InitializeUserRequest(salt))
            }?.body() ?: return RequestResult.Failed
            return when (response.status) {
                "success" -> RequestResult.Success
                "existing" -> RequestResult.Conflict
                else -> RequestResult.Failed
            }
        } catch (e: Exception) {
            return RequestResult.Failed
        }
    }

    suspend fun doInitialUpload(): RequestResult {
        val credentials: List<SyncCredential> =
            repository.getCredentialsLive().value?.map {
                SyncCredential(it.id, encryptCredential(it) ?: return RequestResult.Failed)
            } ?: return RequestResult.Failed
        val response: InitialUploadResponse = client?.post("init/upload") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }?.body() ?: return RequestResult.Failed
        return when (response.status) {
            "success" -> {
                setStateId(context, response.stateId!!)
                RequestResult.Success
            }
            "existing" -> RequestResult.Conflict
            else -> RequestResult.Failed
        }
    }

    suspend fun getEncryptedCredential( id: String): String? {
        val credential = repository.getCredentialById(id) ?: return null
        val key = KeyManager.getSyncKey() ?: return null
        return Crypto.encryptObj(key, credential)
    }

    suspend fun doSync(context: Context): RequestResult {
        val mutations = repository.getCache().map {
            when (it.type) {
                MutationType.Add -> {
                    val credStr =
                        getEncryptedCredential( it.id) ?: return RequestResult.Failed
                    SyncMutation.Add(SyncCredential(it.id, credStr))
                }
                MutationType.Modify -> {
                    val credStr =
                        getEncryptedCredential( it.id) ?: return RequestResult.Failed
                    SyncMutation.Modify(SyncCredential(it.id, credStr))
                }
                MutationType.Delete -> SyncMutation.Delete(SyncCredential(it.id, ""))
            }
        }

        val response: SyncResponse = client?.post("sync") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(getStateId(context), mutations))
        }?.body() ?: return RequestResult.Failed

        Log.d("app", response.toString())
        when (response.status) {
            "success" -> {
                Log.d("app", "successful sync request")
                // Id changes
                if (response.idChanges != null) {
                    response.idChanges.forEach {
                        val changedCredential = repository.getCredentialById(it[0]) ?: return@forEach
                        repository.deleteCredential(it[0], false)
                        changedCredential.id = it[1]
                        repository.addCredential(changedCredential, false)
                    }
                }
                // Mutations
                if (response.mutations != null) {
                    response.mutations.forEach {
                        applyMutations(it)
                    }
                }
                // Store
                if (response.store != null) {
                    val newCredentials: List<Credential> = response.store.map {
                        decryptCredential(it.value) ?: return RequestResult.Failed
                    }
                    val backup =
                        repository.getCredentialsStatic()
                    Log.d("app", "backed up store")
                    repository.deleteAllCredentials()
                    runCatching {
                        repository.addCredentialBulk(newCredentials)
                    }.onFailure {
                        repository.addCredentialBulk(backup)
                        Log.d("app", "failed to bulk insert remote store")
                        return RequestResult.Failed
                    }
                }
                return if (response.stateId != null) {
                    setStateId(context, response.stateId)
                    repository.clearCache()
                    RequestResult.Success
                } else {
                    Log.d("app", "missing state id")
                    RequestResult.Failed
                }
            }
            else -> return RequestResult.Failed
        }
    }

    private fun decryptCredential(encrypted: String): Credential? {
        val key = KeyManager.getSyncKey() ?: return null
        val decrypted = Crypto.decryptObj<Credential>(key, encrypted)
        if (decrypted == null) {
            Log.d("app", "failed to decrypt $encrypted")
        }
        return decrypted
    }

    private fun encryptCredential(credential: Credential): String? {
        val key = KeyManager.getSyncKey() ?: return null
        val encrypted = Crypto.encryptObj(key, credential)
        if (encrypted == null) {
            Log.d("app", "failed to encrypt $encrypted")
        }
        return encrypted
    }

    suspend fun applyMutations(mutation: SyncMutation): Boolean {
        val credential = decryptCredential(mutation.credential.value) ?: return false
        return when (mutation.type) {
            "add" -> {
                repository.addCredential(credential, false)
                false
            }
            "modify" -> {
                if (repository.updateCredential(credential, false) == 0) {
                    repository.addCredential(credential, false)
                    true
                } else false
            }
            "delete" -> {
                repository.deleteCredential(mutation.credential.id, false)
                true
            }
            else -> false
        }
    }

    private fun setStateId(context: Context, stateId: String) {
        KeyManager.getSecurePrefs(context).edit().apply {
            putString(Vals.STATE_ID_KEY, stateId)
            apply()
        }
    }

    private fun getStateId(context: Context): String {
        return KeyManager.getSecurePrefs(context).getString(Vals.STATE_ID_KEY, "")!!
    }
}