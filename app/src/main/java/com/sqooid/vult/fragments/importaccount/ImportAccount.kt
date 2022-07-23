package com.sqooid.vult.fragments.importaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sqooid.vult.R

class ImportAccount : Fragment() {

    companion object {
        fun newInstance() = ImportAccount()
    }

    private lateinit var viewModel: ImportAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ImportAccountViewModel::class.java)
        // TODO: Use the ViewModel
    }

}