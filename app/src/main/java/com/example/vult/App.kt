package com.example.vult

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.vult.databinding.FragmentAppBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [App.newInstance] factory method to
 * create an instance of this fragment.
 */
class App : Fragment() {
    private var _binding: FragmentAppBinding? = null
    private val binding get() = _binding!!

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appToolbar.inflateMenu(R.menu.app_menu)
        binding.appToolbar.updatePadding(left = -48)
        val searchView = binding.appToolbar.menu.findItem(R.id.filter).actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        val searchBar = searchView.findViewById(androidx.appcompat.R.id.search_bar) as LinearLayout
        val transition = LayoutTransition().apply {
            setDuration(200)
            disableTransitionType(LayoutTransition.DISAPPEARING)
            disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            disableTransitionType(LayoutTransition.APPEARING)
        }
        searchBar.layoutTransition = transition
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
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment App.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            App().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}