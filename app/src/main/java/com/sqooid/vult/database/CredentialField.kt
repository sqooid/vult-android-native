package com.sqooid.vult.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CredentialField(
    val name: String,
    var value: String
) : Parcelable