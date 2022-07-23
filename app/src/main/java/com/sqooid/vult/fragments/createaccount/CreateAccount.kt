package com.sqooid.vult.fragments.createaccount

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
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

        binding.buttonImportPage.setOnClickListener {
            it.findNavController().navigate(CreateAccountDirections.actionCreateAccountToImportAccount())
        }

        binding.buttonCreateAccount.setOnClickListener {
            viewModel.createAccount(requireContext())
        }
    }

}