package com.sqooid.vult.util

import androidx.lifecycle.MutableLiveData

class extensions {
}

fun <T> MutableLiveData<T>.forceRefresh() {
    this.value = this.value
}