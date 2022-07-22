package com.example.vult

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}