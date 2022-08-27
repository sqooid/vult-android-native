package com.sqooid.vult.preferences

import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface IPreferences {
    var databaseKey: String
    var syncSalt: String
    var loginHash: String
    var stateId: String
    var syncEnabled: Boolean
    var bioEnabled: Boolean
    var autoSyncEnabled: Boolean
    var syncServer: String
    var syncKey: String
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    @Singleton
    abstract fun bindPreferences(
        preferences: Preferences
    ): IPreferences
}