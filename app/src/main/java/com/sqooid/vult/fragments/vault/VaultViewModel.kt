package com.sqooid.vult.fragments.vault

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.DatabaseManager

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    val credentialList: MutableLiveData<List<Credential>> = MutableLiveData(DatabaseManager.storeDao(getApplication() as Context).getAll())
}