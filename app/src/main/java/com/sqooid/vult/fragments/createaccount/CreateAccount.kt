package com.sqooid.vult.fragments.createaccount

import android.animation.LayoutTransition
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.sqooid.vult.auth.PasswordValidator
import com.sqooid.vult.databinding.FragmentCreateAccountBinding

class CreateAccount : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = CreateAccount()
    }

    private lateinit var viewModel: CreateAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateAccountViewModel::class.java)
        binding.viewmodel = viewModel

        binding.createAccountLayout.layoutTransition =
            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

//        binding.

        binding.buttonImportPage.setOnClickListener {
            it.findNavController()
                .navigate(CreateAccountDirections.actionCreateAccountToImportAccount())
        }

        binding.buttonCreateAccount.setOnClickListener {
            viewModel.createAccount(requireContext())
        }

        viewModel.passwordTooShort.observe(viewLifecycleOwner, Observer<PasswordValidator.PasswordWeakness> {weakness->
            binding.editTextMasterPasswordWrapper.error = when (weakness) {
                null -> null
                PasswordValidator.PasswordWeakness.None -> null
                PasswordValidator.PasswordWeakness.TooShort -> "Password must be at least 8 characters long"
                PasswordValidator.PasswordWeakness.NotEnoughVariety -> "Password must contain at least one lowercase and uppercase letter, number and symbol"
            }
        })
    }

}