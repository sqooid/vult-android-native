package com.sqooid.vult.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StoreDao {
    @Query("Select * FROM Store order by name")
    fun getAll(): LiveData<List<Credential>>

    @Insert
    fun insert(cred: Credential)

    @Update
    fun update(cred: Credential)

    @Query("select * from store where id = :id")
    fun getById(id: String): Credential?
}