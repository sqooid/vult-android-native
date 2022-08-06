package com.sqooid.vult.client

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncClientModule {
    @Singleton
    @Binds
    abstract fun bindSyncClient(
        syncClient: SyncClient
    ): ISyncClient
}