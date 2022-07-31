package com.sqooid.vult.database

import androidx.room.*

@Dao
interface MutationDao {
    @Query("select * from Cache")
    fun getAll(): List<Mutation>

    @Query("select * from Cache where id = :id")
    fun getById(id: String): Mutation?

    @Update
    fun update(mut: Mutation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mut: Mutation)

    @Query("delete from Cache where id = :id")
    fun deleteWithId(id: String)

    @Transaction
    fun addMutation(mut: Mutation) {
        deleteWithId(mut.id)
        insert(mut)
    }

    @Query("delete from Cache")
    fun clear()
}