package com.sqooid.vult.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Check if biometrics is enabled
        val bioEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(getString(R.string.bio_key), false)
        binding.buttonUseBiometrics.visibility = if (bioEnabled) View.VISIBLE else View.INVISIBLE

        binding.buttonLogin.setOnClickListener {
            if (viewModel.passwordLogin()) {
                binding.textLayoutPassword.error = null
                navToVault()
            } else {
                binding.textLayoutPassword.error = "Incorrect password"
            }
        }
    }

    private fun navToVault() {
        this.findNavController().navigate(LoginDirections.actionLoginToApp())
    }

}