package com.sqooid.vult.fragments.createaccount

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.auth.PasswordValidator
import com.sqooid.vult.preferences.IPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val preferences: IPreferences,
) : ViewModel() {
    val passwordError: MutableLiveData<PasswordValidator.PasswordWeakness> by lazy {
        MutableLiveData<PasswordValidator.PasswordWeakness>()
    }

    var masterPassword: String = ""

    fun createAccount() {
        passwordError.value = PasswordValidator.validate(masterPassword)
        if (passwordError.value == PasswordValidator.PasswordWeakness.None) {
            // Set login hash
            preferences.loginHash = Crypto.createHash(masterPassword)
            // Set database key
            preferences.databaseKey = Crypto.generateKey()
        }
    }
}