package com.sqooid.vult

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.sqooid.vult.databinding.FragmentAppBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class App : Fragment() {
    private var _binding: FragmentAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAppBinding.inflate(inflater, container, false)

        binding.appToolbar.inflateMenu(R.menu.app_menu)
//        binding.appToolbar.updatePadding()
        val searchView = binding.appToolbar.menu.findItem(R.id.filter).actionView as SearchView
//        searchView.maxWidth = Int.MAX_VALUE
        val searchBar = searchView.findViewById(androidx.appcompat.R.id.search_bar) as LinearLayout
        val transition = LayoutTransition().apply {
            setDuration(200)
            disableTransitionType(LayoutTransition.DISAPPEARING)
            disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            disableTransitionType(LayoutTransition.APPEARING)
        }
        searchBar.layoutTransition = transition

        binding.appToolbar.setupWithNavController(findNavController())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sync -> {
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> false
            }
        }

        findNavController().addOnDestinationChangedListener { controller, destination, arguments ->
            binding.appToolbar.title = when(destination.id) {
                R.id.vault -> "Vault"
                R.id.credential -> "Edit"
                else -> ""
            }
        }
    }
}