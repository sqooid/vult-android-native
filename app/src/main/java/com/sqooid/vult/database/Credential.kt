package com.sqooid.vult.database

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "Store")
@Parcelize
data class Credential(
    @PrimaryKey var id: String,
    @ColumnInfo var name: String,
    @ColumnInfo val tags: MutableSet<String>,
    @ColumnInfo val fields: MutableList<CredentialField>,
    @ColumnInfo var password: String,
) : Parcelable {
    @Ignore
    @IgnoredOnParcel
    @Transient
    var expanded = false
}

