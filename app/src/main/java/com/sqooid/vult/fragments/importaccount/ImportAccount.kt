package com.sqooid.vult.fragments.importaccount

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.sqooid.vult.R
import com.sqooid.vult.databinding.FragmentImportAccountBinding
import com.sqooid.vult.fragments.createaccount.CreateAccountDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportAccount : Fragment() {

    companion object {
        fun newInstance() = ImportAccount()
    }

    private lateinit var viewModel: ImportAccountViewModel
    private var _binding: FragmentImportAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ImportAccountViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        binding.layout.layoutTransition =
            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

        binding.importButton.setOnClickListener {
            if (viewModel.password.isEmpty()) {
                binding.importPasswordLayout.error = "Password cannot be empty"
                return@setOnClickListener
            } else {
                binding.importPasswordLayout.error = null
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val result = viewModel.importAccount()
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.endpointLayout.error = null
                    binding.keyLayout.error = null
                    binding.importPasswordLayout.error = null
                    when (result) {
                        ImportAccountViewModel.ImportError.URL -> {
                            binding.endpointLayout.error =
                                "Could not connect to server or error with server"
                        }
                        ImportAccountViewModel.ImportError.KEY -> {
                            binding.keyLayout.error =
                                "User with this key does not exist on the server"
                        }
                        ImportAccountViewModel.ImportError.PASSWORD -> {
                            binding.importPasswordLayout.error = "Incorrect password"
                        }
                        null -> {
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.importStore()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    promptBiometrics()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun promptBiometrics() {
        val biometricManager = BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(R.string.biometrics_title)
                .setMessage(R.string.enable_biometrics_dialog)
                .setPositiveButton("Enable") { _, _ ->
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                        .putBoolean(this.getString(R.string.bio_key), true).apply()
                }
                .setNegativeButton("Later", null)
                .setOnDismissListener {
                    this.findNavController()
                        .navigate(CreateAccountDirections.actionCreateAccountToVault())
                }
                .show()
        } else {
            this.findNavController()
                .navigate(CreateAccountDirections.actionCreateAccountToVault())
        }
    }
}