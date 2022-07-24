package com.sqooid.vult.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "Store")
data class Credential(
    @PrimaryKey val id: String,
    @ColumnInfo val name: String,
    @ColumnInfo val tags: List<String>,
    @ColumnInfo val fields: List<CredentialField>
)

@Serializable
data class CredentialField(
    val name: String,
    val value: String
)
