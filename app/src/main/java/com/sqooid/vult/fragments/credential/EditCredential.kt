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
import androidx.recyclerview.widget.LinearLayoutManager
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldEditBinding
import com.sqooid.vult.databinding.FragmentCredentialBinding
import com.sqooid.vult.databinding.NewFieldDialogBinding
import com.sqooid.vult.fragments.vault.recyclerview.TagAdapter

class EditCredential : Fragment() {
    private var _binding: FragmentCredentialBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CredentialViewModel

    private val args: EditCredentialArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCredentialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val credential = args.credential ?: Credential(
            Crypto.generateId(24), "", mutableSetOf(), arrayListOf(
                CredentialField("Username", ""),
                CredentialField("Email", "")
            ), ""
        )
        for (field in credential.fields) {
            addFieldInput(field)
        }

        viewModel = ViewModelProvider(this).get(CredentialViewModel::class.java)
        viewModel.credential = credential
        binding.viewmodel = viewModel

        binding.buttonNewField.setOnClickListener {
            showAddFieldDialog()
        }

        binding.existingTagsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.existingTagsRecycler.adapter = TagAdapter(listOf())
        viewModel.existingTags.observe(viewLifecycleOwner) {
            (binding.existingTagsRecycler.adapter as TagAdapter).tags = it
            binding.existingTagsRecycler.adapter!!.notifyDataSetChanged()
        }

        binding.attachedTagsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.attachedTagsRecycler.adapter = TagAdapter(credential.tags.toList())
        viewModel.addedTags.observe(viewLifecycleOwner) {
            (binding.attachedTagsRecycler.adapter as TagAdapter).tags = it
            binding.attachedTagsRecycler.adapter!!.notifyDataSetChanged()
        }

        binding.buttonAddTag.setOnClickListener {
            if (binding.tagInput.text.toString().isNotEmpty()) {
                viewModel.addTypedTag()
                binding.tagInput.clearFocus()
                binding.tagInput.text?.clear()
            }
        }

    }

    private fun addFieldInput(field: CredentialField) {
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

    fun showAddFieldDialog() {
        val textInputView = NewFieldDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireActivity()).setTitle("Add new field")
            .setPositiveButton("Add") { _, _ ->
                val fieldName = textInputView.textInputNewField.text.toString()
                viewModel.addField(fieldName)
                addFieldInput(viewModel.credential.fields.last())
            }
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