package com.sqooid.vult

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sqooid.vult.rawimport.RawData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class Settings @Inject constructor(
    private val rawData: RawData
) : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Importing
        val getContentImport = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    rawData.importFromUri(uri)
                }.onFailure {
                    launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to import", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val importButton = findPreference<Preference>(getString(R.string.import_raw))
        importButton?.setOnPreferenceClickListener {
            getContentImport.launch("application/json")
            true
        }

        // Exporting
        val createDocumentExport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    rawData.exportToUri(uri)
                }.onFailure {
                    launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to export", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val exportButton = findPreference<Preference>(getString(R.string.export_raw))
        exportButton?.setOnPreferenceClickListener {
            createDocumentExport.launch("vult-export.json")
            true
        }

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