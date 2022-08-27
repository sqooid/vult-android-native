package com.sqooid.vult.util

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.forceRefresh() {
    this.value = this.value
}

fun <T> List<T>.toPrettyString(): String {
    val builder = StringBuilder()
    var prefix = ""
    builder.append("[")
    for (element in this) {
        builder.append(prefix)
        builder.append(element.toString())
        prefix = ", "
    }
    builder.append("]")
    return builder.toString()
}