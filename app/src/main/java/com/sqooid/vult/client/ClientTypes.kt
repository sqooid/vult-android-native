package com.sqooid.vult.client

import kotlinx.serialization.Serializable

@Serializable
data class UserImportResponse(
    val status: String,
    val salt: String
)

@Serializable
data class InitializeUserRequest(
    val salt: String
)

@Serializable
data class InitiializeUserResponse(
    val status: String
)

enum class InitializeUserResult {
    Success,
    Failed,
    Existing,
}