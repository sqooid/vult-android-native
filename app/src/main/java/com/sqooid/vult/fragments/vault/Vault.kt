package com.sqooid.vult.fragments.vault

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.client.RequestResult
import com.sqooid.vult.databinding.FragmentVaultBinding
import com.sqooid.vult.fragments.vault.recyclerview.MainAdapter
import com.sqooid.vult.preferences.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class Vault : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var syncClient: ISyncClient

    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Vault()
    }

    private lateinit var viewModel: VaultViewModel

    private lateinit var adapter: MainAdapter

    private var showSearchBar: Boolean = false
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
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MainAdapter(listOf(), binding.recyclerView) {
            val cred = adapter.data[it]
            val clone = cred.copy(tags = cred.tags.toMutableSet(), fields = ArrayList(cred.fields))
            findNavController().navigate(VaultDirections.actionVaultToCredential(clone))
        }
        binding.recyclerView.adapter = adapter


        if (preferences.syncEnabled) {
            val server = preferences.syncServer
            val key = preferences.syncKey
            Log.d("app", "initialize client: $server $key")
            syncClient.initializeClient(server, key)
        }
        binding.swipeDownSync.setOnRefreshListener {
            if (!preferences.syncEnabled) {
                binding.swipeDownSync.isRefreshing = false
                toast("Sync not enabled")
            } else {
                Log.d("app", "Syncing")
                performSync()
            }
        }
        if (preferences.autoSyncEnabled && preferences.syncEnabled) {
            binding.swipeDownSync.isRefreshing = true
            performSync()
        }

        binding.fabSettings.setOnClickListener {
            findNavController().navigate(VaultDirections.actionVaultToSettings())
        }

        binding.fabSearch.setOnClickListener {
            if (showSearchBar)
                hideSearch(true)
            else
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
                if ((dy != 0 || dx != 0) && binding.searchBar.isFocused && showSearchBar)
                    hideSearch(true)
                if (dy > 0 && showSearchBar) {
                    hideSearch(false)
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

    private fun performSync() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = syncClient.doSync()
            lifecycleScope.launch(Dispatchers.Main) {
                when (result) {
                    RequestResult.Success -> toast("Sync successful")
                    else -> toast("Sync failed")
                }
                binding.swipeDownSync.isRefreshing = false
            }
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

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}