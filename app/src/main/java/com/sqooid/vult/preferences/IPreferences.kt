package com.sqooid.vult.preferences

import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent

interface IPreferences {
    var databaseKey: String
    var syncSalt: String
    var loginHash: String
    var stateId: String
    var syncEnabled: Boolean
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    abstract fun bindPreferences(
        preferences: Preferences
    ): IPreferences
}