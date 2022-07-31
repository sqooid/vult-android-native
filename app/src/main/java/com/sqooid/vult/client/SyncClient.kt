package com.sqooid.vult.client

import android.content.Context
import android.util.Log
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialRepository
import com.sqooid.vult.database.DatabaseManager
import com.sqooid.vult.database.MutationType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class SyncClient {
    data class ClientParams(
        val host: String,
        val key: String?,
    )

    companion object {
        private var client: HttpClient? = null

        fun getSyncEnabled(context: Context): Boolean {
            return context.getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(Vals.SYNC_ENABLED_KEY, false)
        }

        fun setSyncEnabled(context: Context, value: Boolean) {
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

        suspend fun doInitialUpload(context: Context): RequestResult {
            val credentials =
                CredentialRepository.getCredentials(context).value ?: return RequestResult.Failed
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

        fun getEncryptedCredential(context: Context, id: String): String? {
            val dao = DatabaseManager.storeDao(context)
            val credential = dao.getById(id) ?: return null
            val key = KeyManager.getSyncKey() ?: return null
            return Crypto.encryptObj(key, credential)
        }

        suspend fun doSync(context: Context): RequestResult {
            val cacheDao = DatabaseManager.cacheDao(context)
            val mutations = cacheDao.getAll().map {
                when (it.type) {
                    MutationType.Add -> {
                        val credStr =
                            getEncryptedCredential(context, it.id) ?: return RequestResult.Failed
                        SyncMutation.Add(SyncCredential(it.id, credStr))
                    }
                    MutationType.Modify -> {
                        val credStr =
                            getEncryptedCredential(context, it.id) ?: return RequestResult.Failed
                        SyncMutation.Modify(SyncCredential(it.id, credStr))
                    }
                    MutationType.Delete -> SyncMutation.Delete(SyncCredential(it.id, ""))
                }
            }

            val response: SyncResponse = client?.post("sync") {
                contentType(ContentType.Application.Json)
                setBody(SyncRequest(getStateId(context), mutations))
            }?.body() ?: return RequestResult.Failed

            val storeDao = DatabaseManager.storeDao(context)
            return when (response.status) {
                "success" -> {
                    // Id changes
                    if (response.idChanges != null) {
                        response.idChanges.forEach {
                            val changedCredential = storeDao.getById(it[0]) ?: return@forEach
                            storeDao.deleteById(it[0])
                            changedCredential.id = it[1]
                            storeDao.insert(changedCredential)
                        }
                    }
                    // Mutations
                    if (response.mutations != null) {
                        response.mutations.forEach {
                            applyMutations(context, it)
                        }
                    }
                    // Store
                    if (response.store != null) {
                        val newCredentials: List<Credential> = response.store.map {
                            decryptCredential(context, it.value) ?: return RequestResult.Failed
                        }
                        val backup = storeDao.getAll().value?.toList() ?: return RequestResult.Failed
                        storeDao.clear()
                        runCatching {
                            storeDao.insertBulk(newCredentials)
                        }.onFailure {
                            storeDao.insertBulk(backup)
                            return RequestResult.Failed
                        }
                    }
                    if (response.stateId != null) {
                        setStateId(context, response.stateId)
                        RequestResult.Success
                    } else {
                        RequestResult.Failed
                    }
                }
                else -> RequestResult.Failed
            }
        }

        private fun decryptCredential(context: Context, encrypted: String): Credential? {
            val key = KeyManager.getSyncKey() ?: return null
            return Crypto.decryptObj<Credential>(key, encrypted)
        }

        fun applyMutations(context: Context, mutation: SyncMutation): Boolean {
            val storeDao = DatabaseManager.storeDao(context)
            val credential = decryptCredential(context, mutation.credential.value) ?: return false
            return when (mutation.type) {
                "add" -> {
                    storeDao.insert(credential)
                    false
                }
                "modify" -> {
                    if (storeDao.update(credential) == 0) {
                        storeDao.insert(credential)
                        true
                    } else false
                }
                "delete" -> {
                    storeDao.deleteById(mutation.credential.id)
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

        suspend fun testStuff(context: Context) {
            initializeClient(ClientParams("http://192.168.0.26:8000", "test"))
            val salt = importUser()
            Log.d("sync", "salt $salt")
            doSync(context)
        }
    }
}