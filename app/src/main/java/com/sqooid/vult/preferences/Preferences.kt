package com.sqooid.vult.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import com.sqooid.vult.R
import com.sqooid.vult.auth.IKeyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Preferences @Inject constructor(
    @ApplicationContext val context: Context,
    keyManager: IKeyManager
) : IPreferences {
    companion object {
        const val ENCRYPTED_SHARED_PREFERENCES_FILE = "encrypted_shared_preferences"
        const val SHARED_PREFERENCES_FILE = "shared_preferences"
        const val DATABASE_KEY = "databaseKey"
        const val SYNC_SALT = "syncSalt"
        const val LOGIN_HASH = "loginHash"
        const val STATE_ID = "stateId"
        const val SYNC_ENABLED = "syncEnabled"
        const val SYNC_SERVER = "syncServer"
        const val SYNC_KEY = "syncKey"
    }

    private val encryptedSharedPreferences = EncryptedSharedPreferences(
        context,
        ENCRYPTED_SHARED_PREFERENCES_FILE,
        keyManager.getLocalKey(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override var databaseKey: String
        get() = encryptedSharedPreferences.getString(DATABASE_KEY, "")!!
        set(value) = encryptedSharedPreferences.edit().putString(DATABASE_KEY, value).apply()

    override var loginHash: String
        get() = encryptedSharedPreferences.getString(LOGIN_HASH, "")!!
        set(value) = encryptedSharedPreferences.edit().putString(LOGIN_HASH, value).apply()

    override var syncSalt: String
        get() = encryptedSharedPreferences.getString(SYNC_SALT, "")!!
        set(value) = encryptedSharedPreferences.edit().putString(SYNC_SALT, value).apply()

    override var stateId: String
        get() = sharedPreferences.getString(STATE_ID, "")!!
        set(value) = sharedPreferences.edit().putString(STATE_ID, value).apply()

    override var syncEnabled: Boolean
        get() = sharedPreferences.getBoolean(SYNC_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(SYNC_ENABLED, value).apply()

    override var bioEnabled: Boolean
        get() = defaultPreferences.getBoolean(context.getString(R.string.bio_key), false)
        set(value) {
            defaultPreferences.edit().putBoolean(context.getString(R.string.bio_key), value).apply()
        }
    override var autoSyncEnabled: Boolean
        get() = defaultPreferences.getBoolean(context.getString(R.string.pref_auto_sync), true)
        set(value) {
            defaultPreferences.edit().putBoolean(context.getString(R.string.pref_auto_sync), value)
                .apply()
        }

    override var syncKey: String
        get() = encryptedSharedPreferences.getString(SYNC_KEY, "")!!
        set(value) {
            encryptedSharedPreferences.edit().putString(SYNC_KEY, value).commit()
        }


    override var syncServer: String
        get() = encryptedSharedPreferences.getString(SYNC_SERVER, "")!!
        set(value) {
            encryptedSharedPreferences.edit().putString(SYNC_SERVER, value).commit()
        }
}