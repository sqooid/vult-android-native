package com.sqooid.vult.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StoreDao {
    @Query("Select * FROM Store order by name")
    fun getAll(): LiveData<List<Credential>>

    @Insert
    fun insert(cred: Credential)
}