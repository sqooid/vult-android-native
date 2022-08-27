package com.sqooid.vult

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.client.RequestResult
import com.sqooid.vult.client.SyncClient
import com.sqooid.vult.databinding.SyncDialogBinding
import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.rawimport.IRawData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@AndroidEntryPoint
class Settings : PreferenceFragmentCompat() {
    @Inject
    lateinit var rawData: IRawData

    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var syncClient: ISyncClient

    @Inject
    lateinit var keyManager: IKeyManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Importing
        val getContentImport =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                lifecycleScope.launch(Dispatchers.IO) {
                    runCatching {
                        rawData.importFromUri(uri)
                    }.onFailure {
                        launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Failed to import", Toast.LENGTH_SHORT)
                                .show()
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
        val createDocumentExport =
            registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                lifecycleScope.launch(Dispatchers.IO) {
                    runCatching {
                        rawData.exportToUri(uri)
                    }.onFailure {
                        launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Failed to export", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

        val exportButton = findPreference<Preference>(getString(R.string.export_raw))
        exportButton?.setOnPreferenceClickListener {
            createDocumentExport.launch("vult-export.json")
            true
        }

        val defaultPasswordLength =
            findPreference<EditTextPreference>(getString(R.string.gen_def_length))
        defaultPasswordLength?.onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                    val newDefault = try {
                        (newValue as String).toInt()
                    } catch (e: Exception) {
                        return false
                    }
                    return newDefault >= 6
                }
            }

        val syncButton = findPreference<Preference>(getString(R.string.pref_sync))
        syncButton?.setOnPreferenceClickListener {
            val syncDialogBinding = SyncDialogBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(requireActivity()).setTitle("Enable sync")
                .setPositiveButton("Enable", null)
                .setNegativeButton("Cancel", null)
                .setView(syncDialogBinding.root)
                .create()
            syncDialogBinding.dialogDescription.movementMethod = LinkMovementMethod.getInstance()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    syncClient.initializeClient(
                        SyncClient.ClientParams(
                            syncDialogBinding.syncUrl.text.toString(),
                            syncDialogBinding.syncKey.text.toString()
                        )
                    )

                    val password = syncDialogBinding.password.text.toString()
                    if (!BCrypt.checkpw(password, preferences.loginHash)) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            toast("Incorrect password")
                        }
                        return@launch
                    }
                    val salt = keyManager.createSyncKey(password)
                    preferences.syncSalt = salt

                    var result = syncClient.initializeUser()
                    lifecycleScope.launch(Dispatchers.Main) {
                        when (result) {
                            RequestResult.Success -> {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    result = syncClient.doInitialUpload()
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        when (result) {
                                            RequestResult.Success -> {
                                                preferences.syncEnabled = true
                                                toast("Successfully enabled sync")
                                                dialog.dismiss()
                                            }
                                            RequestResult.Failed -> toast("Failed to upload data. Try again later")
                                            RequestResult.Conflict -> toast("Failed to enable sync. User data already exists for this key")
                                        }
                                    }
                                }
                            }
                            RequestResult.Failed -> toast("Failed to connect to server or key does not exist")
                            RequestResult.Conflict -> toast("Failed to enable sync. User has already been initialized before")
                        }
                    }
                }
            }
            true
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}