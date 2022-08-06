package com.sqooid.vult.database

interface IDatabase {
    fun storeDao(): StoreDao
    fun cacheDao(): MutationDao
}