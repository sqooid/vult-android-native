package com.sqooid.vult.fragments.createaccount

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.sqooid.vult.auth.KeyManager

class CreateAccountViewModel : ViewModel() {
    fun createAccount(context: Context) {
        Log.d("auth", masterPassword)
        KeyManager.createMasterKey(context, masterPassword)
        //
        val entry = KeyManager.getMasterKey().toString()
        Log.d("auth", entry)
    }

    var masterPassword: String = ""
}