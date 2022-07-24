package com.sqooid.vult.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StoreDao {
    @Query("Select * FROM Store")
    fun getAll(): List<Credential>

    @Insert
    fun insert(cred: Credential)

    @Delete
    fun delete(cred: Credential)
}