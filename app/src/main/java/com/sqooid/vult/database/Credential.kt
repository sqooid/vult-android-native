package com.sqooid.vult.database

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "Store")
@Parcelize
data class Credential(
    @PrimaryKey var id: String,
    @ColumnInfo var name: String,
    @ColumnInfo val tags: MutableSet<String>,
    @ColumnInfo val fields: ArrayList<CredentialField>,
    @ColumnInfo var password: String,
) : Parcelable {
    @Ignore
    @IgnoredOnParcel
    var expanded = false
}

