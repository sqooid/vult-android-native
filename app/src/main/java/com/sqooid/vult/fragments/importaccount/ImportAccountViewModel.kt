package com.sqooid.vult.fragments.importaccount

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.client.RequestResult
import com.sqooid.vult.client.SyncClient
import com.sqooid.vult.preferences.IPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@HiltViewModel
class ImportAccountViewModel @Inject constructor(
    private val syncClient: ISyncClient,
    private val preferences: IPreferences
) : ViewModel() {
    var serverEndpoint: String = ""
    var userKey: String = ""
    var password: String = ""

    enum class ImportError {
        URL,
        KEY,
        PASSWORD,
    }

    suspend fun importAccount(): ImportError? {
        Log.d("app","$serverEndpoint $userKey $password")
        syncClient.initializeClient(SyncClient.ClientParams(serverEndpoint, userKey))
        return when (syncClient.importUser(password)) {
            RequestResult.Failed -> {
                undoAttempt()
                ImportError.URL
            }
            RequestResult.Conflict -> {
                undoAttempt()
                ImportError.KEY
            }
            else -> {
                if (BCrypt.checkpw(password, preferences.loginHash)) null else {
                    undoAttempt()
                    ImportError.PASSWORD
                }
            }
        }
    }

    private fun undoAttempt() {
        preferences.loginHash = ""
    }

    suspend fun importStore() {
        preferences.databaseKey = Crypto.generateKey()
        syncClient.doSync()
    }
}