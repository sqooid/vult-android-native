package com.sqooid.vult.database

import android.content.Context
import androidx.lifecycle.LiveData

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
    }
}