package com.sqooid.vult.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Credential::class, Mutation::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun cacheDao(): MutationDao
}