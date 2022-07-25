package com.sqooid.vult.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(entities = [Credential::class, Mutation::class], version = 1)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun cacheDao(): MutationDao
}

class Converters {
    @TypeConverter
    fun setToString(list: MutableSet<String>): String {
        return Json.encodeToString(list)
    }
    @TypeConverter
    fun stringToSet(str: String): MutableSet<String> {
        return Json.decodeFromString(str)
    }
    @TypeConverter
    fun listCredFieldToString(list: ArrayList<CredentialField>): String {
        return Json.encodeToString(list)
    }
    @TypeConverter
    fun stringToListCredField(str: String): ArrayList<CredentialField> {
        return Json.decodeFromString(str)
    }
    @TypeConverter
    fun mutTypeToString(mutType: MutationType): String {
        return when (mutType){
            MutationType.Add -> "0"
            MutationType.Modify -> "1"
            MutationType.Delete -> "2"
        }
    }
    @TypeConverter
    fun stringToMutType(str: String): MutationType {
        return when (str) {
            "0" -> MutationType.Add
            "1" -> MutationType.Delete
            else -> MutationType.Modify
        }
    }
}
