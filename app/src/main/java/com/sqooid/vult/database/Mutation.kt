package com.sqooid.vult.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cache")
data class Mutation(
    @PrimaryKey val id: String,
    @ColumnInfo val type: MutationType
)

enum class MutationType {
    Add,
    Modify,
    Delete
}