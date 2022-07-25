package com.sqooid.vult.fragments.credential

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldEditBinding
import com.sqooid.vult.databinding.FragmentCredentialBinding
import com.sqooid.vult.databinding.NewFieldDialogBinding

class EditCredential : Fragment() {
    private var _binding: FragmentCredentialBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CredentialViewModel

    private val args: EditCredentialArgs by navArgs()
    lateinit var credential: Credential

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCredentialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        credential = args.credential ?: Credential(
            Crypto.generateId(24), "", listOf(), listOf(
                CredentialField("Username", ""),
                CredentialField("Email", "")
            ), ""
        )
        for (field in credential.fields) {
            Log.d("app", "$field")
            val newField =
                FieldEditBinding.inflate(layoutInflater, binding.fieldEditBlock, false).apply {
                    textWrapper.hint = field.name
                    text.text = text.text?.append(field.value)
                    text.addTextChangedListener {
                        field.value = it.toString()
                    }
                }
            binding.fieldEditBlock.addView(newField.root)
        }

        viewModel = ViewModelProvider(this).get(CredentialViewModel::class.java)
        viewModel.credential = credential
        binding.viewmodel = viewModel

        binding.buttonNewField.setOnClickListener {
            showAddFieldDialog()
        }
    }

    fun showAddFieldDialog() {
        val textInputView = NewFieldDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireActivity()).setTitle("Add new field")
            .setPositiveButton("Add") { _, _ -> }
            .setNegativeButton("Cancel", null)
            .setOnDismissListener {
                hideKeyboard()
            }
            .setView(textInputView.root)
            .create()
        textInputView.textInputNewField.setOnFocusChangeListener { _, b -> if (b) dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        dialog.show()
        textInputView.textInputNewField.requestFocus()
    }

    fun hideKeyboard() {
        activity?.currentFocus?.let {
            val inputManager =
                requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputManager?.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }
}