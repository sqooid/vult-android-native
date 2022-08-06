package com.sqooid.vult.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import android.util.Log
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class KeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : IKeyManager {
    private val SYNC_KEY_ALIAS = "vult_sync_key"
    private val LOCAL_KEY_ALIAS = "vult_local_key"
    private val KEY_STORE_TYPE = "AndroidKeyStore"


    /**
     * Create the key used to sync credentials between devices.
     * Must produce the same key given the same seed and salt
     * returns the salt
     */
    override fun createSyncKey(seed: String, fixedSalt: ByteArray?): String {
        val (salt, innerKey) = Crypto.generateSeededKey(seed, fixedSalt)
        val masterKey = SecretKeySpec(innerKey, "AES")
        val keyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }
        keyStore.setEntry(
            SYNC_KEY_ALIAS,
            KeyStore.SecretKeyEntry(masterKey),
            KeyProtection.Builder(
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
        )

        return Base64.encode(salt, Base64.NO_PADDING or Base64.NO_WRAP).toString(
            Charset.defaultCharset()
        )
    }

    /**
     * Get the key used encrypt/decrypt for syncing
     */
    override fun getSyncKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }
        val entry = keyStore.getEntry(SYNC_KEY_ALIAS, null)
        if (entry !is KeyStore.SecretKeyEntry) {
            Log.d("auth", "Key not a secret key")
            return null
        }
        return entry.secretKey
    }

    /**
     * Get local key used to encrypt other secure values
     */
    override fun getLocalKey(): MasterKey {
        val spec = KeyGenParameterSpec.Builder(
            LOCAL_KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return MasterKey.Builder(context, LOCAL_KEY_ALIAS).setKeyGenParameterSpec(spec).build()
    }
}