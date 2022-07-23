package com.sqooid.vult.fragments.createaccount

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.KeyManager
import java.security.SecureRandom

class CreateAccountViewModel : ViewModel() {
    fun createAccount(context: Context) {
        KeyManager.createMasterKey(context, masterPassword)
        val key = KeyManager.getMasterKey()!!
        val result = Crypto.encrypt(key, "shit")
        Log.d("auth", result)
    }

    var masterPassword: String = ""
}