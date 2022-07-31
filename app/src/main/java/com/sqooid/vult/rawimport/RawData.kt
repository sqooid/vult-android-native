package com.sqooid.vult.rawimport

import android.content.Context
import android.net.Uri
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.database.Credential
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.database.CredentialRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class RawData {
    companion object {
        suspend fun importFromUri(context: Context, uri: Uri) {
            val resolver = context.contentResolver
            val builder = StringBuilder()
            resolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        builder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val content = builder.toString()
            val json = Json.parseToJsonElement(content).jsonArray
            val importedCredentials: ArrayList<Credential> = ArrayList()
            json.forEach { jsonElement ->
                val obj = jsonElement.jsonObject
                val newCredential = Credential(
                    obj["id"]?.jsonPrimitive?.content ?: Crypto.generateId(24),
                    obj["description"]?.jsonPrimitive?.content
                        ?: obj["name"]!!.jsonPrimitive.content,
                    obj["tags"]!!.jsonArray.map { it.jsonPrimitive.content }.toMutableSet(),
                    if (obj["fields"]!! is JsonObject) {
                        val fieldArray: ArrayList<CredentialField> = ArrayList()
                        val fields = obj["fields"]!!.jsonObject
                        fields.keys.asSequence().associateWith { key ->
                            val value = fields[key]!!.jsonPrimitive.content
                            if (value.isNotEmpty())
                                fieldArray.add(CredentialField(key, value))
                        }
                        fieldArray
                    } else {
                        obj["fields"]!!.jsonArray.map { fieldElement ->
                            CredentialField(
                                fieldElement.jsonObject["key"]!!.jsonPrimitive.content,
                                fieldElement.jsonObject["value"]!!.jsonPrimitive.content
                            )
                        }.toMutableList()
                    },
                    obj["password"]!!.jsonPrimitive.content
                )
                importedCredentials.add(newCredential)
            }

            for (credential in importedCredentials) {
                CredentialRepository.addCredential(context, credential)
            }
        }

        private val json = Json { prettyPrint = true }

        suspend fun exportToUri(context: Context, uri: Uri) {
            val resolver = context.contentResolver

            val credentialString = json.encodeToString(
                CredentialRepository.getCredentials(context).value
            )

            resolver.openOutputStream(uri).use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(credentialString)
                }
            }
        }
    }
}