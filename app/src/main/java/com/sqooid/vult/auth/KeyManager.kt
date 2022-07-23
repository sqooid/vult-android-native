package com.sqooid.vult.auth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import android.util.Log
import com.sqooid.vult.Vals
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class KeyManager {
    companion object {
        private const val LOCAL_KEY_ALIAS = "vult_local_key"
        private const val KEY_STORE_TYPE = "AndroidKeyStore"

        fun createMasterKey(context: Context, seed: String, fixedSalt: ByteArray? = null) {
            val salt = fixedSalt ?: ByteArray(32)
            if (fixedSalt == null) {
                val random = SecureRandom()
                random.nextBytes(salt)
            }
            val pbeSpec = PBEKeySpec(seed.toCharArray(), salt, 200000, 256)
            val innerKey =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeSpec)
            val masterKey = SecretKeySpec(innerKey.encoded, "AES")
            val keyStore = KeyStore.getInstance(KeyManager.KEY_STORE_TYPE).apply { load(null) }
            keyStore.setEntry(
                KeyManager.LOCAL_KEY_ALIAS,
                KeyStore.SecretKeyEntry(masterKey),
                KeyProtection.Builder(
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
            )
            val saltString = Base64.encode(salt, Base64.NO_PADDING or Base64.NO_WRAP).toString(
                Charset.defaultCharset())
            Log.d("auth", saltString)
            context.getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit().putString(Vals.KEY_SALT_KEY, saltString).apply()
        }

        fun getMasterKey(): SecretKey? {
            val keyStore = KeyStore.getInstance(KeyManager.KEY_STORE_TYPE).apply { load(null) }
            val entry = keyStore.getEntry(KeyManager.LOCAL_KEY_ALIAS, null)
            if (entry !is KeyStore.SecretKeyEntry) {
                Log.d("auth", "Key not a secret key")
                return null
            }
            return entry.secretKey
        }

        fun deleteMasterKey() {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(KeyManager.LOCAL_KEY_ALIAS)
        }
    }
}