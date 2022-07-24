package com.sqooid.vult.fragments.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.sqooid.vult.Vals
import org.mindrot.jbcrypt.BCrypt

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var password = ""
    val hash = (getApplication() as Context).getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).getString(Vals.HASH_KEY, "")

    fun passwordLogin(): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}