package com.sqooid.vult.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Store")
data class Credential(
    @PrimaryKey val id: String,
    @ColumnInfo val name: String,
    @ColumnInfo val tags: List<String>,
    @ColumnInfo val fields: List<CredentialField>
)

data class CredentialField(
    val name: String,
    val value: String
)