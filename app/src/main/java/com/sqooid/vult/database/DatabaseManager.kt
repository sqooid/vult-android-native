package com.sqooid.vult.database

import android.content.Context
import android.util.Base64
import androidx.room.Room
import com.sqooid.vult.Vals
import com.sqooid.vult.auth.KeyManager
import net.sqlcipher.database.SupportFactory

class DatabaseManager {
    companion object {
        private var db: Database? = null
        private fun getDb(context: Context): Database {
            if (db == null) {
                val prefs = KeyManager.getSecurePrefs(context)
                val dataKeyString = prefs.getString(Vals.DATA_KEY_KEY, "")
                if (dataKeyString!!.isEmpty()) {
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
            return db as Database
        }

        fun storeDao(context: Context): StoreDao {
            return getDb(context).storeDao()
        }
    }
}