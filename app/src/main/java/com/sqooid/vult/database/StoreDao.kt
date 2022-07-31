package com.sqooid.vult.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StoreDao {
    @Query("Select * FROM Store order by lower(name)")
    fun getAll(): LiveData<List<Credential>>

    @Query("select * from Store")
    fun getAllStatic(): List<Credential>?

    @Insert
    fun insert(cred: Credential)

    @Insert
    fun insertBulk(credentials: List<Credential>)

    @Update
    fun update(cred: Credential): Int

    @Query("select * from Store where id = :id")
    fun getById(id: String): Credential?

    @Query("delete from Store where id = :id")
    fun deleteById(id: String)

    @Query("delete from Store")
    fun clear()
}