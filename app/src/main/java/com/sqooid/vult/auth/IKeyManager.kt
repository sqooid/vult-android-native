package com.sqooid.vult.auth

import android.content.Context
import androidx.security.crypto.MasterKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.crypto.SecretKey

interface IKeyManager {
    /**
     * Create the key used to sync credentials between devices.
     * Must produce the same key given the same seed and salt
     * returns the salt
     */
    fun createSyncKey(seed: String, fixedSalt: ByteArray? = null): String

    /**
     * Get the key used encrypt/decrypt for syncing
     */
    fun getSyncKey(): SecretKey?

    /**
     * Get local key used to encrypt other secure values
     */
    fun getLocalKey(): MasterKey
}

@Module
@InstallIn(SingletonComponent::class)
abstract class KeyManagerModule {
    @Binds
    abstract fun bindKeyManager(
        keyManager: KeyManager
    ): IKeyManager
}