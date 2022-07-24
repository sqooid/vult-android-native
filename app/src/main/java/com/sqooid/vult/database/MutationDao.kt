package com.sqooid.vult.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MutationDao {
    @Query("select * from Cache order by time")
    fun getAll(): List<Mutation>

    @Insert
    fun insert(mut: Mutation)

    @Query("delete from Cache where id = :id")
    fun deleteWithId(id: String)

    @Query("delete from Cache")
    fun clear()
}