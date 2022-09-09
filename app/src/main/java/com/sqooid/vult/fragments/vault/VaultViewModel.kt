package com.sqooid.vult.fragments.vault

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.database.Credential
import com.sqooid.vult.repository.ICredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    val repository: ICredentialRepository
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
            tokens.all { token ->
                it.name.contains(token, true) || it.tags.any { tag ->
                    tag.contains(
                        token, true
                    )
                } || it.fields.any { field -> field.value.contains(token, true) }
            }
        }
    }
}