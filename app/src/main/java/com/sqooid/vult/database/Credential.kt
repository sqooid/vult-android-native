package com.sqooid.vult.database

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "Store")
data class Credential(
    @PrimaryKey val id: String,
    @ColumnInfo val name: String,
    @ColumnInfo val tags: List<String>,
    @ColumnInfo val fields: List<CredentialField>,
) {
    var expanded = false
    fun getVisibleFields(): List<CredentialField> {
        return fields
        if (expanded || fields.isEmpty()) {
            return fields
        }
        return listOf(fields[0])
    }
}

@Serializable
data class CredentialField(
    val name: String,
    val value: String
)
