package com.sqooid.vult.fragments.credential

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.CredentialRepository
import com.sqooid.vult.util.forceRefresh

class CredentialViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var credential: Credential

    val existingTags: List<String> =
        CredentialRepository.getTagsByUsage(getApplication() as Context)

    val filteredExistingTags: MutableLiveData<List<String>> by lazy {
        MutableLiveData(existingTags.filter {
            !credential.tags.contains(it)
        })
    }

    val addedTags: MutableLiveData<List<String>> by lazy {
        MutableLiveData(credential.tags.toList())
    }

    var newTagValue: String = ""

    fun addField(fieldName: String) {
        credential.fields.add(CredentialField(fieldName, ""))
    }

    fun addTypedTag() {
        credential.tags.add(newTagValue)
        addedTags.value = credential.tags.toList()
    }

    fun addClickedTag(idx: Int) {
        credential.tags.add(filteredExistingTags.value!![idx])
        addedTags.value = credential.tags.toList()
        filterExistingTags()
    }

    fun removeClickedTag(idx: Int) {
        credential.tags.remove(addedTags.value!![idx])
        addedTags.value = credential.tags.toList()
        filterExistingTags()
    }

    fun filterExistingTags(filter: String = "") {
        filteredExistingTags.value = existingTags.filter {
            !credential.tags.contains(it) && it.contains(filter)
        }
    }
}