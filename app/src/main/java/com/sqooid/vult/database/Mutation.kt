package com.sqooid.vult.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cache")
data class Mutation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo val time: Int,
    @ColumnInfo val type: MutationType,
    @ColumnInfo val id: String
)

enum class MutationType {
    Add,
    Modify,
    Delete
}