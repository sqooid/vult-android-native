package com.sqooid.vult.fragments.vault

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.client.SyncClient
import com.sqooid.vult.databinding.FragmentVaultBinding
import com.sqooid.vult.fragments.vault.recyclerview.MainAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Vault : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Vault()
    }

    private lateinit var viewModel: VaultViewModel

    private lateinit var adapter: MainAdapter

    private var showSearchBar: Boolean? = null
    private var searchBarOnCooldown: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(VaultViewModel::class.java)
        binding.viewmodel = viewModel

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(VaultDirections.actionVaultToCredential(null))
//            lifecycleScope.launch(Dispatchers.IO) {
//                CredentialRepository.addCredential(requireContext(), Credential("same","Thing",
//                    mutableSetOf("hello","work","secondary","shit"), arrayListOf(CredentialField("Email","chieck@super.das"),
//                        CredentialField("Username","dasauto")
//                    ), "nothing"
//                )
//                )
//            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MainAdapter(listOf(), binding.recyclerView) {
            val cred = adapter.data[it]
            val clone = cred.copy(tags = cred.tags.toMutableSet(), fields = ArrayList(cred.fields))
            findNavController().navigate(VaultDirections.actionVaultToCredential(clone))
        }
        binding.recyclerView.adapter = adapter


        binding.swipeDownSync.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                SyncClient.testStuff(requireContext())
            }
            binding.swipeDownSync.isRefreshing = false
        }

        binding.fabSettings.setOnClickListener {
            findNavController().navigate(VaultDirections.actionVaultToSettings())
        }

        binding.fabSearch.setOnClickListener {
            showSearch(true)
        }
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideSearch(true)
                true
            } else {
                false
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (showSearchBar == null) {
                    return
                }
                if ((dy != 0 || dx != 0) && binding.searchBar.isFocused && showSearchBar == true)
                    hideSearch(true)
                if (dy > 0 && showSearchBar == true) {
                    hideSearch(false)
                } else if (dy < 0 && showSearchBar == false) {
                    showSearch(false)
                }
            }
        })

        viewModel.credentialList.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.filterCredentials("")
            }
        }

        viewModel.filterCredentialList.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.data = it
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }

        viewModel.searchText.observe(viewLifecycleOwner) {
            if (it != null) viewModel.filterCredentials(it)
        }
    }

    private fun createSearchBarAnimator(reverse: Boolean): ObjectAnimator {
        return ObjectAnimator.ofFloat(
            binding.searchBarWrapperCard,
            "translationY",
            binding.searchBarWrapperCard.height.toFloat() * if (reverse) -1.5f else 1.5f
        ).apply {
            duration = 200
            interpolator = AnticipateOvershootInterpolator()
        }
    }

    private fun showSearch(focus: Boolean) {
        if (searchBarOnCooldown && !focus) return
        putSearchBarOnCooldown()
        showSearchBar = true
        createSearchBarAnimator(false).apply {
            start()
            if (focus)
                doOnEnd {
                    binding.searchBar.requestFocus()
                    val imm =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
                }
        }
    }

    private fun hideSearch(unfocus: Boolean) {
        if (searchBarOnCooldown) return
        putSearchBarOnCooldown()
        if (unfocus) {
            binding.searchBar.clearFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
        showSearchBar = false
        createSearchBarAnimator(true).start()
    }

    private fun putSearchBarOnCooldown() {
        searchBarOnCooldown = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                searchBarOnCooldown = false
            }
        }, 500)
    }
}