package com.sqooid.vult.repository

import androidx.lifecycle.LiveData
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.Mutation

interface CredentialRepository {
    fun getCredentialsLive(): LiveData<List<Credential>>

    fun getCredentialsStatic(): List<Credential>

    fun getTagsByUsage(): List<String>

    suspend fun updateCredential(credential: Credential): Int

    suspend fun addCredential(credential: Credential)

    suspend fun deleteCredential(id: String)

    suspend fun getCredentialById(id: String): Credential?

    suspend fun getCache(): List<Mutation>

    suspend fun clearCache()

    suspend fun deleteAllCredentials()

    suspend fun addCredentialBulk(credentials: List<Credential>)
}