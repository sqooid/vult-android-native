package com.sqooid.vult.database

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseManagerModule {
    @Singleton
    @Binds
    abstract fun bindDatabaseManager(
        databaseManager: DatabaseManager
    ): IDatabase
}