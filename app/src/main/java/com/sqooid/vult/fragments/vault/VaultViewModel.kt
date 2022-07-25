package com.sqooid.vult.fragments.vault

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialRepository
import com.sqooid.vult.database.DatabaseManager
import kotlinx.coroutines.launch

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    val credentialList: LiveData<List<Credential>> = CredentialRepository.getCredentials(getApplication() as Context)


}