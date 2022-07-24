package com.sqooid.vult.fragments.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.sqooid.vult.Vals
import org.mindrot.jbcrypt.BCrypt

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var password = ""

    fun passwordLogin(): Boolean {
        val hash = (getApplication() as Context).getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).getString(Vals.HASH, "") //todo add check
        return BCrypt.checkpw(password, hash)
    }
}