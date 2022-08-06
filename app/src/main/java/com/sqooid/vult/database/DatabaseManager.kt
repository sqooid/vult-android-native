package com.sqooid.vult.database

import android.content.Context
import android.util.Base64
import androidx.room.Room
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.KeyManager
import com.sqooid.vult.preferences.IPreferences
import com.sqooid.vult.preferences.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject

class DatabaseManager @Inject constructor(
    @ApplicationContext val context: Context,
    preferences: IPreferences
): IDatabase {
    private val db: Database
    init {
        val dataKeyString = preferences.databaseKey
        if (dataKeyString.isEmpty()) {
            throw Error("Missing database encryption key")
        }
        val supportFactory = SupportFactory(
            Base64.decode(
                dataKeyString,
                Base64.NO_WRAP or Base64.NO_PADDING
            )
        )
        db = Room.databaseBuilder(context, Database::class.java, "main-database")
            .openHelperFactory(supportFactory).build()
    }

    override fun storeDao(): StoreDao {
        return db.storeDao()
    }

    override fun cacheDao(): MutationDao {
        return db.cacheDao()
    }
}