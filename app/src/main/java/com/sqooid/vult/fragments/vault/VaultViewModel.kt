package com.sqooid.vult.fragments.vault

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.database.Credential
import com.sqooid.vult.repository.CredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    val repository: CredentialRepository
) : ViewModel() {

    val credentialList: LiveData<List<Credential>> =
        repository.getCredentialsLive()

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