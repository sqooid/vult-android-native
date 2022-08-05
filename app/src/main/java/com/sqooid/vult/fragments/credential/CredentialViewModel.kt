package com.sqooid.vult.fragments.credential

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.repository.CredentialRepository
import com.sqooid.vult.repository.Repository
import javax.inject.Inject

class CredentialViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var repository: CredentialRepository
    lateinit var credential: Credential
    var passwordGeneratorSettings: PasswordGeneratorSettings? = null

    private val existingTags: List<String> =
        repository.getTagsByUsage()

    private lateinit var filteredExistingTags: ArrayList<String>

    val passwordInput: MutableLiveData<String> by lazy {
        MutableLiveData(credential.password)
    }
    val passwordLength: MutableLiveData<String> by lazy {
        MutableLiveData("8")
    }

    val newAddedTags: MutableLiveData<DataUpdateInfo<String>> by lazy {
        MutableLiveData()
    }

    val newFilteredExistingTags: MutableLiveData<DataUpdateInfo<String>> by lazy {
        MutableLiveData()
    }

    var newTagValue: String = ""

    fun addField(fieldName: String): Int {
        credential.fields.add(CredentialField(fieldName, ""))
        return credential.fields.size - 1
    }

    fun addTypedTag() {
        credential.tags.add(newTagValue)
        val list = credential.tags.toList()
        newAddedTags.value = DataUpdateInfo(list, DataChangeType.Add, list.indexOf(newTagValue))
    }

    fun addClickedTag(idx: Int) {
        val newTag = filteredExistingTags[idx]
        credential.tags.add(newTag)
        filteredExistingTags.removeAt(idx)
        val list = credential.tags.toList()
        newAddedTags.value = DataUpdateInfo(list, DataChangeType.Add, list.indexOf(newTag))
        newFilteredExistingTags.value =
            DataUpdateInfo(filteredExistingTags, DataChangeType.Delete, idx)
    }

    fun removeClickedTag(idx: Int) {
        val list = credential.tags.toMutableList()
        val newTag = list[idx]
        credential.tags.remove(newTag)
        list.removeAt(idx)
        newAddedTags.value = DataUpdateInfo(list, DataChangeType.Delete, idx)
        val newFiltered = existingTags.filter {
            !credential.tags.contains(it) && it.contains(newTagValue)
        }
        if (newFiltered.size > filteredExistingTags.size) {
            newFilteredExistingTags.value =
                DataUpdateInfo(newFiltered, DataChangeType.Add, newFiltered.indexOf(newTag))
            filteredExistingTags = ArrayList(newFiltered)
        }
    }

    fun filterExistingTags(filter: String = "") {
        val filtered = existingTags.filter {
            !credential.tags.contains(it) && it.contains(filter)
        }
        filteredExistingTags = ArrayList(filtered)
        newFilteredExistingTags.value = DataUpdateInfo(filtered, DataChangeType.None, -1)
    }

    fun increaseLength() {
        passwordGeneratorSettings?.let {
            it.length += 1
            passwordLength.value = it.length.toString()
        }
    }

    fun decreaseLength() {
        passwordGeneratorSettings?.let {
            it.length = if (it.length > 6) it.length - 1 else 6
            passwordLength.value = it.length.toString()
        }
    }

    fun setPasswordLength(length: Int) {
        passwordGeneratorSettings?.let {
            it.length = length
        }
    }

    fun generatePassword() {
        if (passwordGeneratorSettings != null) {
            credential.password = Crypto.generatePassword(passwordGeneratorSettings!!)
            passwordInput.value = credential.password
        }
    }

    fun swapFields(from: Int, to: Int) {
        val field = credential.fields[from]
        credential.fields[from] = credential.fields[to]
        credential.fields[to] = field
    }

    fun removeField(index: Int) {
        credential.fields.removeAt(index)
    }

    fun cleanCredential() {
        credential.fields.retainAll {
            it.value.isNotEmpty()
        }
    }

    fun updateField(index: Int, value: String) {
        credential.fields[index].value = value
    }

    suspend fun saveCredential(new: Boolean) {
        if (new)
            repository.addCredential(credential)
        else
            repository.updateCredential(credential)
    }

    suspend fun deleteCredential() {
        repository.deleteCredential(credential.id)
    }
}