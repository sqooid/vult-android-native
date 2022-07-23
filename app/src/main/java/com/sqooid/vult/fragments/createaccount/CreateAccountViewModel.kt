package com.sqooid.vult.fragments.createaccount

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.from
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.sqooid.vult.App
import com.sqooid.vult.R
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.auth.PasswordValidator
import java.net.Authenticator
import java.security.SecureRandom

class CreateAccountViewModel(application: Application) : AndroidViewModel(application) {
    val passwordTooShort: MutableLiveData<PasswordValidator.PasswordWeakness> by lazy {
        MutableLiveData<PasswordValidator.PasswordWeakness>()
    }

    val successfullyCreated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    var masterPassword: String = ""

    fun createAccount(context: Context) {
        passwordTooShort.value = PasswordValidator.validate(masterPassword)
        if (passwordTooShort.value != PasswordValidator.PasswordWeakness.None) {
            return
        }
        KeyManager.createMasterKey(context, masterPassword)
    }

    fun promptBiometrics(fragment: Fragment) {
        if (from(getApplication()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            val dialog = EnableBiometricsDialog()
            dialog.show(fragment.parentFragmentManager, "bioprompt")
        }
    }

    fun enableBiometrics(enable: Boolean) {
        if (enable) {
            PreferenceManager.getDefaultSharedPreferences(getApplication()).edit()
                .putBoolean((getApplication() as Context).getString(R.string.bio_key), true)
        }
        successfullyCreated.value = true
    }
}