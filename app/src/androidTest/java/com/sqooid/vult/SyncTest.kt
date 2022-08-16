package com.sqooid.vult

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.client.RequestResult
import com.sqooid.vult.client.SyncClient
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.IDatabase
import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.repository.ICredentialRepository
import com.sqooid.vult.repository.Repository
import dagger.hilt.android.testing.CustomTestApplication
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class SyncTest {

    val database: IDatabase = FakeDatabase()
    val keyManager: IKeyManager = FakeKeyManager()
    val preferences: IPreferences = FakePreferences()

    val syncClient: ISyncClient = SyncClient(database, keyManager, preferences)
    val repository: ICredentialRepository = Repository(database, preferences)


    private val salt = "somesalt"
    private val seed = "someseed"
    private val host = "http://192.168.0.26:8000"
    private val key = "androidtest"

    @Before
    fun setup() {
        runBlocking {
            syncClient.initializeClient(SyncClient.ClientParams(host, key))
            syncClient.initializeUser(salt)
        }
        keyManager.createSyncKey(seed, salt.toByteArray())
        database.cacheDao().clear()
        database.storeDao().clear()
        runBlocking {
            repository.addCredential(
                Credential(
                    "cred1",
                    "cred1",
                    mutableSetOf("tag1"),
                    mutableListOf(CredentialField("field1", "value1")),
                    "password1"
                )
            )
            repository.addCredential(
                Credential(
                    "cred2",
                    "cred2",
                    mutableSetOf("tag2"),
                    mutableListOf(CredentialField("field2", "value2")),
                    "password2"
                )
            )
        }
    }

    @Test
    fun initialUpload() {
        var result: RequestResult
        runBlocking {
            result = syncClient.doInitialUpload()
        }
        assert(result == RequestResult.Success)
        assert(preferences.stateId.isNotEmpty())
        runBlocking {
            result = syncClient.doInitialUpload()
        }
        assert(result == RequestResult.Conflict)
    }
}