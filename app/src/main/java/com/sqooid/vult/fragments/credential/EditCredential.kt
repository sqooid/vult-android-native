package com.sqooid.vult.fragments.credential

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldEditBinding
import com.sqooid.vult.databinding.FragmentCredentialBinding

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
        for (field in credential.fields)  {
            Log.d("app", "$field")
            val newField = FieldEditBinding.inflate(layoutInflater, binding.fieldEditBlock, false).apply {
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
    }

}