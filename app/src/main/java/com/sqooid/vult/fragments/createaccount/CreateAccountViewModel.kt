package com.sqooid.vult.fragments.createaccount

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.auth.PasswordValidator
import java.nio.charset.Charset

class CreateAccountViewModel(application: Application) : AndroidViewModel(application) {
    val passwordTooShort: MutableLiveData<PasswordValidator.PasswordWeakness> by lazy {
        MutableLiveData<PasswordValidator.PasswordWeakness>()
    }

    var masterPassword: String = ""

    fun createAccount(context: Context) {
        passwordTooShort.value = PasswordValidator.validate(masterPassword)
        if (passwordTooShort.value == PasswordValidator.PasswordWeakness.None) {
            KeyManager.createHash(context, masterPassword)
        }
    }

    fun createDataKey() {
        val dataKey = KeyManager.createDataKey()
        val dataKeyString = Base64.encode(dataKey, Base64.NO_PADDING or Base64.NO_WRAP).toString(
            Charset.defaultCharset())
        KeyManager.getSecurePrefs(getApplication() as Context).edit().apply {
            putString(Vals.DATA_KEY_KEY, dataKeyString)
            commit()
        }
    }
}