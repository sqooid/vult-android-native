package com.sqooid.vult.fragments.login

import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.sqooid.vult.R
import com.sqooid.vult.databinding.FragmentLoginBinding

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Login()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.viewmodel = viewModel

        //todo add layout animations

        // Biometrics
        val bioEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(getString(R.string.bio_key), false)
        binding.buttonUseBiometrics.visibility = if (bioEnabled) View.VISIBLE else View.INVISIBLE
        if (bioEnabled) {
            showBiometricPrompt()
        }

        binding.buttonLogin.setOnClickListener {
            if (viewModel.passwordLogin()) {
                binding.textLayoutPassword.error = null
                navToVault()
            } else {
                binding.textLayoutPassword.error = "Incorrect password"
            }
        }

        binding.buttonUseBiometrics.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun navToVault() {
        this.findNavController().navigate(LoginDirections.actionLoginToVault())
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireActivity())
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navToVault()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT ->
                            Toast.makeText(
                                requireContext(),
                                "Failed too many times, try again in 30 seconds",
                                Toast.LENGTH_LONG
                            ).show()
                        BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT -> {
                            Toast.makeText(
                                requireContext(),
                                "Failed too many times, must use password",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.buttonUseBiometrics.visibility = View.INVISIBLE
                        }
                    }
                }
            })
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometrics login")
            .setSubtitle("Log in to Vult with biometrics")
            .setNegativeButtonText("Use password")
            .build()
        biometricPrompt.authenticate(promptInfo)

    }
}