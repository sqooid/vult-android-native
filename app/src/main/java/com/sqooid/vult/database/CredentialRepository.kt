package com.sqooid.vult.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.sqooid.vult.auth.Crypto
import net.sqlcipher.database.SQLiteConstraintException
import java.lang.Exception

class CredentialRepository {
    companion object {
        private var credentialList: LiveData<List<Credential>>? = null
        private var tagList: MutableMap<String, Int>? = null

        fun getCredentials(context: Context): LiveData<List<Credential>> {
            if (credentialList == null) {
                credentialList = DatabaseManager.storeDao(context).getAll()
            }
            return credentialList!!
        }

        fun getTagsByUsage(context: Context): List<String> {
            if (tagList == null) {
                tagList = mutableMapOf()
                for (credential in getCredentials(context).value!!) {
                    for (tag in credential.tags) {
                        tagList!![tag] = tagList!!.getOrDefault(tag, 0) + 1
                    }
                }
            }
            return tagList!!.iterator().asSequence().sortedBy {
                it.value
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