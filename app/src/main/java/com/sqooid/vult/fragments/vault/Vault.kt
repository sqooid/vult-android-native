package com.sqooid.vult.fragments.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sqooid.vult.R
import com.sqooid.vult.databinding.FragmentVaultBinding

class Vault : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Vault()
    }

    private lateinit var viewModel: VaultViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(VaultViewModel::class.java)
        binding.viewmodel = viewModel
    }

}