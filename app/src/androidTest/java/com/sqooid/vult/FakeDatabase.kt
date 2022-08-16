package com.sqooid.vult

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sqooid.vult.database.*
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakeDatabase : IDatabase {
    private val _db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), Database::class.java).build()
    override fun storeDao(): StoreDao {
        return _db.storeDao()
    }

    override fun cacheDao(): MutationDao {
        return _db.cacheDao()
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseManagerModule::class]
)
abstract class FakeDatabaseModule {
//    @Singleton
    @Binds
    abstract fun bindDatabase(
        database: FakeDatabase
    ): IDatabase
}