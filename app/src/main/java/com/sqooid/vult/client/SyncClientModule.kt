package com.sqooid.vult.client

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SyncClientModule {
    @Singleton
    @Provides
    fun bindSyncClient(
        syncClient: SyncClient
    ): SyncClient
}