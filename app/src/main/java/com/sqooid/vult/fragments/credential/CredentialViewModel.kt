package com.sqooid.vult.fragments.credential

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.CredentialRepository

class CredentialViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var credential: Credential
    val existingTags: MutableLiveData<List<String>> by lazy {
        MutableLiveData(CredentialRepository.getTagsByUsage(getApplication() as Context))
    }

    val addedTags: MutableLiveData<List<String>> by lazy {
        MutableLiveData(credential.tags.toList())
    }

    var newTagValue: String = ""

    fun addField(fieldName: String) {
        credential.fields.add(CredentialField(fieldName,""))
    }

    fun addTypedTag() {
        Log.d("app", "added tag: $newTagValue")
        credential.tags.add(newTagValue)
        addedTags.value = credential.tags.toList()
    }
}