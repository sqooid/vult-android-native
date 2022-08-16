package com.sqooid.vult

import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.repository.RepositoryModule
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakePreferences : IPreferences {
    override var databaseKey: String = ""
    override var syncSalt: String = ""
    override var loginHash: String = ""
    override var stateId: String = ""
    override var syncEnabled: Boolean = true
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakePreferencesModule {
//    @Singleton
    @Binds
    abstract fun bindPreferences(
        preferences: FakePreferences
    ): IPreferences
}