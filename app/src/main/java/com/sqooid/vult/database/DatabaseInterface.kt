package com.sqooid.vult.database

interface DatabaseInterface {
    fun storeDao(): StoreDao
    fun cacheDao(): MutationDao
}