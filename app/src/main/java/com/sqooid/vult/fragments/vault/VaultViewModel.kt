package com.sqooid.vult.fragments.vault

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialRepository

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    val credentialList: LiveData<List<Credential>> =
        CredentialRepository.getCredentials(getApplication() as Context)

    val filterCredentialList: MutableLiveData<List<Credential>> by lazy {
        MutableLiveData(listOf())
    }

    val searchText: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    fun filterCredentials(filter: String) {
        val tokens = filter.split(" ")
        filterCredentialList.value = credentialList.value?.filter {
            tokens.any { token ->
                it.name.contains(token) || it.tags.any { tag ->
                    tag.contains(
                        token
                    )
                } || it.fields.any { field -> field.value.contains(token) }
            }
        }
    }
}