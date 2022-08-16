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


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SyncTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var syncClient: ISyncClient

    @Inject
    lateinit var database: IDatabase

    @Inject
    lateinit var keyManager: IKeyManager

    @Inject
    lateinit var repository: ICredentialRepository

    @Inject
    lateinit var preferences: IPreferences

    private val salt = "somesalt"
    private val seed = "someseed"
    private val host = "192.168.0.26:8000"
    private val key = "androidtest"

    @BeforeClass
    fun setUpClients() {
        hiltRule.inject()
        runBlocking {
            syncClient.initializeClient(SyncClient.ClientParams(host, key))
            syncClient.initializeUser(salt)
        }
        keyManager.createSyncKey(seed, salt.toByteArray())
    }

    @Before
    fun clearDatabase() {
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