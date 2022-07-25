package com.sqooid.vult.fragments.vault

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.DatabaseManager
import com.sqooid.vult.databinding.FragmentVaultBinding
import com.sqooid.vult.fragments.vault.recyclerview.MainAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Vault : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Vault()
    }

    private lateinit var viewModel: VaultViewModel

    private lateinit var adapter: MainAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MainAdapter(listOf(), binding.recyclerView)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(VaultViewModel::class.java)
        binding.viewmodel = viewModel

        Log.d("app", viewModel.credentialList.value.toString())

        binding.fabAdd.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                DatabaseManager.storeDao(requireContext()).insert(
                    Credential(
                        System.currentTimeMillis().toString(),
                        "Credential",
                        listOf("hobo", "homo", "chicken"),
                        listOf(
                            CredentialField("Email", "bob@gmail.com"),
                            CredentialField("Username", "BigFoot69")
                        ),
                        "VeryNicePassword"
                    )
                )
            }
        }

        viewModel.credentialList.observe(viewLifecycleOwner) {
            Log.d("app", it.toString())
            adapter.data = it
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

}