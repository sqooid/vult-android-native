package com.sqooid.vult.fragments.credential

import androidx.room.Index

enum class DataChangeType {
    Change,
    Add,
    Delete,
    None
}

data class DataUpdateInfo<T>(val newData: List<T>, val changeType: DataChangeType, val index: Int)
