package com.sqooid.vult.preferences

import android.content.SharedPreferences

interface IPreferenceManager {
    fun getPrefs(): SharedPreferences
}