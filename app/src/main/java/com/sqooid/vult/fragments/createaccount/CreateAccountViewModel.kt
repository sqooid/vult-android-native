package com.sqooid.vult.fragments.createaccount

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.auth.PasswordValidator
import java.security.SecureRandom

class CreateAccountViewModel : ViewModel() {
    val passwordTooShort: MutableLiveData<PasswordValidator.PasswordWeakness> by lazy {
        MutableLiveData<PasswordValidator.PasswordWeakness>()
    }

    var masterPassword: String = ""

    fun createAccount(context: Context) {
        passwordTooShort.value = PasswordValidator.validate(masterPassword)
        if (passwordTooShort.value != PasswordValidator.PasswordWeakness.None) {
            return
        }
        KeyManager.createMasterKey(context, masterPassword)
    }
}