package com.sqooid.vult.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.client.SyncClientInterface
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.DatabaseInterface
import com.sqooid.vult.database.Mutation
import com.sqooid.vult.database.MutationType
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SQLiteConstraintException
import javax.inject.Inject

class Repository @Inject constructor(
    @ApplicationContext val context: Context,
    private val databaseManager: DatabaseInterface,
    private val syncClient: SyncClientInterface
) : CredentialRepository {

    private var credentialList: LiveData<List<Credential>>? = null
    private var tagMap: MutableMap<String, Int> = mutableMapOf()
    private var tagMapInitialized = false

    override fun getCredentialsLive(): LiveData<List<Credential>> {
        if (credentialList == null) {
            credentialList = databaseManager.storeDao().getAll()
        }
        return credentialList!!
    }

    override fun getCredentialsStatic(): List<Credential> {
        val dao = databaseManager.storeDao()
        return dao.getAllStatic()
    }

    override fun getTagsByUsage(): List<String> {
        if (!tagMapInitialized) {
            for (credential in getCredentialsLive().value!!) {
                for (tag in credential.tags) {
                    tagMap[tag] = tagMap.getOrDefault(tag, 0) + 1
                }
            }
            tagMapInitialized = true
        }
        return tagMap.iterator().asSequence().sortedBy {
            -it.value
        }.map { it.key }.toList()
    }

    override suspend fun updateCredential(credential: Credential): Int {
        val dao = databaseManager.storeDao()
        val result = dao.update(credential)

        // Cache - turn into add mutation if add already there
        if (syncClient.getSyncEnabled()) {
            val cacheDao = databaseManager.cacheDao()
            if (cacheDao.getById(credential.id) != null) {
                cacheDao.update(Mutation(credential.id, MutationType.Add))
            } else {
                cacheDao.insert(Mutation(credential.id, MutationType.Modify))
            }
        }
        return result
    }

    override suspend fun addCredential(credential: Credential) {
        val dao = databaseManager.storeDao()
        var successful = false
        do {
            try {
                dao.insert(credential)
                successful = true
                for (tag in credential.tags) {
                    tagMap[tag] = tagMap.getOrDefault(tag, 0) + 1
                }
                // Cache - convert earlier deletes into updates
                if (syncClient.getSyncEnabled()) {
                    val cacheDao = databaseManager.cacheDao()
                    if (cacheDao.getById(credential.id) != null) {
                        cacheDao.update(Mutation(credential.id, MutationType.Modify))
                    } else {
                        cacheDao.insert(Mutation(credential.id, MutationType.Add))
                    }
                }
            } catch (e: SQLiteConstraintException) {
                Log.d("app", e.toString())
                credential.id = Crypto.generateId(24)
            }
        } while (!successful)
    }

    override suspend fun deleteCredential(id: String) {
        val dao = databaseManager.storeDao()
        dao.deleteById(id) // note tag map is not updated because cbs...

        // Cache
        if (syncClient.getSyncEnabled()) {
            val cacheDao = databaseManager.cacheDao()
            cacheDao.insert(Mutation(id, MutationType.Delete))
        }
    }

    override suspend fun getCredentialById(id: String): Credential? {
        val dao = databaseManager.storeDao()
        return dao.getById(id)
    }

    override suspend fun getCache(): List<Mutation> {
        val dao = databaseManager.cacheDao()
        return dao.getAll()
    }

    override suspend fun clearCache() {
        val dao = databaseManager.cacheDao()
        dao.clear()
    }

    override suspend fun deleteAllCredentials() {
        val dao = databaseManager.storeDao()
        dao.clear()
    }

    override suspend fun addCredentialBulk(credentials: List<Credential>) {
        val dao = databaseManager.storeDao()
        dao.insertBulk(credentials)
    }
}