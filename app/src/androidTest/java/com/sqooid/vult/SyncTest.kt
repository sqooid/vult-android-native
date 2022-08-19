package com.sqooid.vult

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.client.RequestResult
import com.sqooid.vult.client.SyncClient
import com.sqooid.vult.database.*
import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.preferences.Preferences
import com.sqooid.vult.repository.ICredentialRepository
import com.sqooid.vult.repository.Repository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class SyncTest {

    var database: IDatabase = FakeDatabase()
    var keyManager: IKeyManager = FakeKeyManager()
    var preferences: IPreferences = FakePreferences()

    var syncClient: ISyncClient = SyncClient(database, keyManager, preferences)
    var repository: ICredentialRepository = Repository(database, preferences)


    private val salt = "somesalt"
    private val seed = "someseed"
    private val host = "http://192.168.0.26:8000"
    private val key = "androidtest"

    @Before
    fun setup() {
        database = FakeDatabase()
        keyManager = FakeKeyManager()
        preferences = FakePreferences()

        syncClient = SyncClient(database, keyManager, preferences)
        repository = Repository(database, preferences)
        keyManager.createSyncKey(seed, salt.toByteArray())
        database.cacheDao().clear()
        database.storeDao().clear()
    }

    fun resetServer() {
        runBlocking {
            syncClient.initializeClient(SyncClient.ClientParams(host, key))
            syncClient.deleteUser()
            syncClient.initializeUser(salt)
        }
    }

    @Test
    fun initialUpload() {
        resetServer()
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
        preferences.syncEnabled = true
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
        assert(database.cacheDao().getAll().isEmpty())
    }

    @Test
    fun singleSyncUpload() {
        resetServer()
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
        preferences.syncEnabled = true
        var result: RequestResult
        runBlocking {
            syncClient.doInitialUpload()
        }
        val initialStateId = preferences.stateId
        runBlocking {
            repository.addCredential(
                Credential(
                    "cred3",
                    "cred3",
                    mutableSetOf("tag3"),
                    mutableListOf(CredentialField("field3", "value3")),
                    "password3"
                )
            )
            assert(database.cacheDao().getAll()[0] == Mutation("cred3",MutationType.Add))
            result = syncClient.doSync()
        }
        assert(preferences.stateId != initialStateId)
        assert(result == RequestResult.Success)
        assert(database.cacheDao().getAll().isEmpty())
        assert(database.storeDao().getAllStatic().size == 3)
    }
}