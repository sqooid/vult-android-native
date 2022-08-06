package com.sqooid.vult.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
): IPreferenceManager {
    override fun getPrefs(): SharedPreferences {
        TODO("Not yet implemented")
    }
}