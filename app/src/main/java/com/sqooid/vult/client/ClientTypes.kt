package com.sqooid.vult.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserImportResponse(
    val status: String,
    val salt: String?
)

@Serializable
data class InitializeUserRequest(
    val salt: String
)

@Serializable
data class InitializeUserResponse(
    val status: String
)

enum class RequestResult {
    Success,
    Failed,
    Conflict,
}

@Serializable
data class InitialUploadResponse(
    @SerialName("state_id")
    val stateId: String?,
    val status: String
)

@Serializable
data class SyncCredential(
    val id: String,
    val value: String
)

@Serializable
data class SyncRequest(
    @SerialName("state_id")
    val stateId: String?,
    val mutations: List<SyncMutation>
)

@Serializable
data class SyncResponse(
    val status: String,
    @SerialName("state_id")
    val stateId: String?,
    val mutations: List<SyncMutation>?,
    val store: List<SyncCredential>?,
    @SerialName("id_changes")
    val idChanges: List<List<String>>?,
)

@Serializable
class SyncMutation(val type: String, val credential: SyncCredential) {
    companion object {
        fun Add(credential: SyncCredential): SyncMutation = SyncMutation("add", credential)
        fun Modify(credential: SyncCredential): SyncMutation = SyncMutation("modify", credential)
        fun Delete(credential: SyncCredential): SyncMutation = SyncMutation("delete", credential)
    }
}
