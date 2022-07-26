package com.sqooid.vult.fragments.vault

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.CredentialRepository
import com.sqooid.vult.databinding.FragmentVaultBinding
import com.sqooid.vult.fragments.vault.recyclerview.MainAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
//            findNavController().navigate(VaultDirections.actionVaultToCredential(null))
            lifecycleScope.launch(Dispatchers.IO) {
                CredentialRepository.addCredential(requireContext(), Credential("same","Thing",
                    mutableSetOf("hello","work","secondary","shit"), arrayListOf(CredentialField("Email","chieck@super.das"),
                        CredentialField("Username","dasauto")
                    ), "nothing"
                )
                )
            }
        }

        binding.swipeDownSync.setOnRefreshListener {
            Log.d("app","swiped down to sync")
            binding.swipeDownSync.isRefreshing = false
        }

        binding.fabSettings.setOnClickListener {
            findNavController().navigate(VaultDirections.actionVaultToSettings())
        }

        binding.fabSearch.setOnClickListener {
            ObjectAnimator.ofFloat(binding.searchBarWrapperCard, "translationY", binding.searchBarWrapperCard.height.toFloat() * 1.5f).apply {
                duration = 200
                interpolator = OvershootInterpolator()
                start()
                doOnEnd {
                    binding.searchBar.requestFocus()
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 || dx != 0)
                closeSearch()
            }
        })

        viewModel.credentialList.observe(viewLifecycleOwner) {
            Log.d("app", it.toString())
            adapter.data = it
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun closeSearch() {
        binding.searchBar.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}