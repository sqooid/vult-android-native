package com.sqooid.vult.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.sqooid.vult.auth.Crypto
import net.sqlcipher.database.SQLiteConstraintException

class CredentialRepository {
    companion object {
        private var credentialList: LiveData<List<Credential>>? = null
        private var tagMap: MutableMap<String, Int> = mutableMapOf()
        private var tagMapInitialized = false

        fun getCredentials(context: Context): LiveData<List<Credential>> {
            if (credentialList == null) {
                credentialList = DatabaseManager.storeDao(context).getAll()
            }
            return credentialList!!
        }

        fun getTagsByUsage(context: Context): List<String> {
            if (!tagMapInitialized) {
                for (credential in getCredentials(context).value!!) {
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

        suspend fun updateCredential(context: Context, credential: Credential) {
            val dao = DatabaseManager.storeDao(context)
            dao.update(credential)
        }

        suspend fun addCredential(context: Context, credential: Credential) {
        val dao = DatabaseManager.storeDao(context)
            var successful = false
            do {
                try {
                    dao.insert(credential)
                    successful = true
                    for (tag in credential.tags) {
                        tagMap[tag] = tagMap.getOrDefault(tag, 0) + 1
                    }
                } catch (e: SQLiteConstraintException) {
                    Log.d("app", e.toString())
                    credential.id = Crypto.generateId(24)
                }
            } while (!successful)
        }

        suspend fun deleteCredential(context: Context, id: String) {
            val dao = DatabaseManager.storeDao(context)
            dao.deleteById(id) // note tag map is not updated because cbs...
        }
    }
}