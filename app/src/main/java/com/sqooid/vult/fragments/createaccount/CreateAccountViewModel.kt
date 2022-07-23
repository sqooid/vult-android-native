package com.sqooid.vult.fragments.createaccount

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.auth.PasswordValidator

class CreateAccountViewModel(application: Application) : AndroidViewModel(application) {
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