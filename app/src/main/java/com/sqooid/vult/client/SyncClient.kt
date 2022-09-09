package com.sqooid.vult.client

import android.util.Base64
import android.util.Log
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.IDatabase
import com.sqooid.vult.database.MutationType
import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.util.toPrettyString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Inject

interface ISyncClient {
    fun initializeClient(host: String, key: String)

    /**
     * Sets the sync master key and login hash
     */
    suspend fun importUser(password: String): RequestResult

    suspend fun initializeUser(): RequestResult

    suspend fun doInitialUpload(): RequestResult

    suspend fun doSync(): RequestResult

    suspend fun deleteUser(): RequestResult
}

class SyncClient @Inject constructor(
    private val databaseManager: IDatabase,
    private val keyManager: IKeyManager,
    private val preferences: IPreferences
) : ISyncClient {

    data class ClientParams(
        val host: String,
        val key: String?,
    )

    private var client: HttpClient? = null

    override fun initializeClient(host: String, key: String) {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url(host)
                header("Authentication", key)
            }
        }
    }

    override suspend fun importUser(password: String): RequestResult {
        try {
            val response: UserImportResponse? = client?.get("user/import")?.body()
            if (response == null || response.status == "failed") {
                return RequestResult.Failed
            } else if (response.status == "uninitialized") {
                return RequestResult.Conflict
            }
            if (response.salt == null || response.hash == null) {
                return RequestResult.Failed
            }
            Log.d("app", response.toString())
            keyManager.createSyncKey(
                password,
                Base64.decode(response.salt, Base64.NO_PADDING or Base64.NO_WRAP)
            )
            preferences.loginHash = response.hash
            preferences.syncSalt = response.salt
            return RequestResult.Success
        } catch (e: Exception) {
            Log.d("app", e.toString())
            return RequestResult.Failed
        }
    }

    override suspend fun initializeUser(): RequestResult {
        try {
            val salt = preferences.syncSalt
            val hash = preferences.loginHash
            Log.d("app", "Initialize user: $salt $hash")
            val response: InitializeUserResponse = client?.post("user/init") {
                contentType(ContentType.Application.Json)
                setBody(InitializeUserRequest(salt, hash))
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

    override suspend fun doInitialUpload(): RequestResult {
        val storeDao = databaseManager.storeDao()
        val credentials: List<SyncCredential> =
            storeDao.getAllStatic().map {
                SyncCredential(it.id, encryptCredential(it) ?: run {
                    Log.d("sync", "Failed to decrypt credential")
                    return RequestResult.Failed
                })
            }
        val response: InitialUploadResponse = client?.post("init/upload") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }?.body() ?: return RequestResult.Failed
        return when (response.status) {
            "success" -> {
                preferences.stateId = response.stateId!!
                RequestResult.Success
            }
            "existing" -> RequestResult.Conflict
            else -> RequestResult.Failed
        }
    }

    private fun getEncryptedCredential(id: String): String? {
        val storeDao = databaseManager.storeDao()
        val credential = storeDao.getById(id) ?: return null
        val key = keyManager.getSyncKey() ?: return null
        return Crypto.encryptObj(key, credential)
    }

    override suspend fun doSync(): RequestResult {
        val cacheDao = databaseManager.cacheDao()
        val storeDao = databaseManager.storeDao()
        val cache = cacheDao.getAll()
        Log.d("sync", cache.toPrettyString())
        val mutations = cache.map {
            when (it.type) {
                MutationType.Add -> {
                    Log.d("sync", "Getting add mutation")
                    val credStr =
                        getEncryptedCredential(it.id) ?: return RequestResult.Failed
                    SyncMutation.Add(SyncCredential(it.id, credStr))
                }
                MutationType.Modify -> {
                    Log.d("sync", "Getting modify mutation")
                    val credStr =
                        getEncryptedCredential(it.id) ?: return RequestResult.Failed
                    SyncMutation.Modify(SyncCredential(it.id, credStr))
                }
                MutationType.Delete -> SyncMutation.Delete(SyncCredential(it.id, ""))
            }
        }

        Log.d("sync", "Posting to sync endpoint")
        val response: SyncResponse
        try {
            response = client?.post("sync") {
                contentType(ContentType.Application.Json)
                setBody(SyncRequest(preferences.stateId, mutations))
            }?.body() ?: return RequestResult.Failed
        } catch (e: Exception) {
            Log.d("sync", "Failed sync with error\n$e")
            return RequestResult.Failed
        }

        Log.d("app", response.toString())
        when (response.status) {
            "success" -> {
                Log.d("app", "successful sync request")
                // Id changes
                if (response.idChanges != null) {
                    response.idChanges.forEach {
                        val changedCredential =
                            storeDao.getById(it[0]) ?: return@forEach
                        storeDao.deleteById(it[0])
                        changedCredential.id = it[1]
                        storeDao.insert(changedCredential)
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
                        storeDao.getAllStatic()
                    Log.d("app", "backed up store")
                    storeDao.clear()
                    runCatching {
                        storeDao.insertBulk(newCredentials)
                    }.onFailure {
                        storeDao.insertBulk(backup)
                        Log.d("app", "failed to bulk insert remote store")
                        return RequestResult.Failed
                    }
                }
                return if (response.stateId != null) {
                    preferences.stateId = response.stateId
                    cacheDao.clear()
                    RequestResult.Success
                } else {
                    Log.d("app", "missing state id")
                    RequestResult.Failed
                }
            }
            else -> return RequestResult.Failed
        }
    }

    override suspend fun deleteUser(): RequestResult {
        val response = client?.post("test/reset") ?: return RequestResult.Failed
        return if (response.status == HttpStatusCode.OK) {
            RequestResult.Success
        } else {
            RequestResult.Failed
        }
    }

    private fun decryptCredential(encrypted: String): Credential? {
        val key = keyManager.getSyncKey() ?: return null
        val decrypted = Crypto.decryptObj<Credential>(key, encrypted)
        if (decrypted == null) {
            Log.d("app", "failed to decrypt $encrypted")
        }
        return decrypted
    }

    private fun encryptCredential(credential: Credential): String? {
        val key = keyManager.getSyncKey() ?: return null
        val encrypted = Crypto.encryptObj(key, credential)
        if (encrypted == null) {
            Log.d("app", "failed to encrypt $encrypted")
        }
        return encrypted
    }

    private fun applyMutations(mutation: SyncMutation): Boolean {
        val storeDao = databaseManager.storeDao()
        val credential = decryptCredential(mutation.credential.value) ?: return false
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
}