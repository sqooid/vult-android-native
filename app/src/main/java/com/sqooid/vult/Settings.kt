package com.sqooid.vult

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val defaultPasswordLength = findPreference<EditTextPreference>(getString(R.string.gen_def_length))
        defaultPasswordLength?.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                val newDefault = try {
                    (newValue as String).toInt()
                } catch (e: Exception) {
                    return false
                }
                return newDefault >= 6
            }
        }
    }
}