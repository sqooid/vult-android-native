package com.sqooid.vult.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class SyncClient {
    data class ClientParams(
        val host: String,
        val key: String?,
    ) {}
    companion object {
        private var client: HttpClient? = null

        fun initializeClient(params: ClientParams) {
            client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
                defaultRequest {
                    url(params.host)
                    header("Authorization", params.key)
                }
            }
        }

        suspend fun importUser(): String? {
            val response: UserImportResponse = client?.get("user/import")?.body() ?: return null
            return response.salt
        }

        suspend fun initializeUser(salt: String): InitializeUserResult {
            val response: InitiializeUserResponse = client?.post("user/init") {
                contentType(ContentType.Application.Json)
                setBody(InitializeUserRequest(salt))
            }?.body() ?: return InitializeUserResult.Failed
            return when(response.status) {
                "success" -> InitializeUserResult.Success
                "existing" -> InitializeUserResult.Existing
                else -> InitializeUserResult.Failed
            }
        }
    }
}