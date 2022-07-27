package com.sqooid.vult.fragments.credential

import android.animation.LayoutTransition
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sqooid.vult.R
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.CredentialRepository
import com.sqooid.vult.databinding.FieldEditBinding
import com.sqooid.vult.databinding.FragmentCredentialBinding
import com.sqooid.vult.databinding.NewFieldDialogBinding
import com.sqooid.vult.fragments.vault.recyclerview.TagAdapter
import com.sqooid.vult.util.forceRefresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Exception

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
        binding.lifecycleOwner = this

        binding.credRoot.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }

        // Add field
        binding.buttonNewField.setOnClickListener {
            showAddFieldDialog()
        }

        // Existing tags
        binding.existingTagsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.existingTagsRecycler.adapter = TagAdapter(listOf()) {
            viewModel.addClickedTag(binding.existingTagsRecycler.getChildLayoutPosition(it))
        }
        viewModel.newFilteredExistingTags.observe(viewLifecycleOwner) {
            Log.d("app","filtered: ${it.toString()}")
            val adapter = binding.existingTagsRecycler.adapter
            (adapter as TagAdapter).tags = it.newData
            when (it.changeType) {
                DataChangeType.Add -> adapter.notifyItemInserted(it.index)
                DataChangeType.Delete -> adapter.notifyItemRemoved(it.index)
                else -> adapter.notifyDataSetChanged()
            }
            binding.attachedTagsRecycler.scheduleLayoutAnimation()
        }
        viewModel.filterExistingTags("")

        // Added tags
        binding.attachedTagsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.attachedTagsRecycler.adapter = TagAdapter(credential.tags.toList()) {
            viewModel.removeClickedTag(binding.attachedTagsRecycler.getChildLayoutPosition(it))
        }
        viewModel.newAddedTags.observe(viewLifecycleOwner) {
            Log.d("app","added: ${it.toString()}")
            val adapter = binding.attachedTagsRecycler.adapter
            (adapter as TagAdapter).tags = it.newData
            when (it.changeType) {
                DataChangeType.Add -> adapter.notifyItemInserted(it.index)
                DataChangeType.Delete -> adapter.notifyItemRemoved(it.index)
                else -> adapter.notifyDataSetChanged()
            }
            binding.attachedTagsRecycler.scheduleLayoutAnimation()
        }

        // Filter tags
        binding.tagInput.addTextChangedListener {
            viewModel.filterExistingTags(it.toString())
        }

        // Add tag button
        binding.buttonAddTag.setOnClickListener {
            if (binding.tagInput.text.toString().isNotEmpty()) {
                viewModel.addTypedTag()
                binding.tagInput.clearFocus()
                binding.tagInput.text?.clear()
            }
        }

        // Generator settings
        // Defaults
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        viewModel.passwordGeneratorSettings = PasswordGeneratorSettings(
            try { prefs.getString(getString(R.string.gen_def_length), "8")!!.toInt() } catch (e: Exception) { 8 },
            prefs.getBoolean(getString(R.string.gen_def_upper), true),
            prefs.getBoolean(getString(R.string.gen_def_num), true),
            prefs.getBoolean(getString(R.string.gen_def_sym), true),
        )
        binding.buttonAddLength.setOnClickListener {
            viewModel.increaseLength()
        }
        binding.buttonRemoveLength.setOnClickListener {
            viewModel.decreaseLength()
        }
        binding.buttonGeneratePassword.setOnClickListener {
            viewModel.generatePassword()
        }

        // Done button
        binding.fabDone.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                CredentialRepository.addCredential(requireContext(), viewModel.credential)
                launch(Dispatchers.Main) {
                    findNavController().navigate(EditCredentialDirections.actionCredentialToVault())
                }
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