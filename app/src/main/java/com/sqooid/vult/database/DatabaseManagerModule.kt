package com.sqooid.vult.database

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseManagerModule {
    @Binds abstract fun bindDatabaseManager(
        databaseManager: DatabaseManager
    ): DatabaseInterface
}