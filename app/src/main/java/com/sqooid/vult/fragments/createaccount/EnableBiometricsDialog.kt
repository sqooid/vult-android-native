package com.sqooid.vult.fragments.createaccount

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.sqooid.vult.R
import kotlin.ClassCastException

class EnableBiometricsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProvider(this).get(CreateAccountViewModel::class.java)
        val builder = AlertDialog.Builder(activity!!)
        builder.setMessage(R.string.enable_biometrics_dialog)
            .setPositiveButton("Enable") { _, _ -> viewModel.enableBiometrics(true) }
            .setNegativeButton("Later") { _, _ -> viewModel.enableBiometrics(false) }
        return builder.create() as Dialog
    }
}