package com.sqooid.vult.auth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sqooid.vult.Vals
import org.mindrot.jbcrypt.BCrypt
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class KeyManager {
    companion object {
        private const val SYNC_KEY_ALIAS = "vult_sync_key"
        private const val LOCAL_KEY_ALIAS = "vult_local_key"
        private const val KEY_STORE_TYPE = "AndroidKeyStore"

        fun generateInnerKey(
            seed: String,
            fixedSalt: ByteArray? = null
        ): Pair<ByteArray, ByteArray> {
            val salt = fixedSalt ?: ByteArray(32)
            if (fixedSalt == null) {
                val random = SecureRandom()
                random.nextBytes(salt)
            }
            val pbeSpec = PBEKeySpec(seed.toCharArray(), salt, 200000, 256)
            val innerKey =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeSpec)
            return salt to innerKey.encoded
        }

        /**
         * Create the key used to sync credentials between devices.
         * Must produce the same key given the same seed and salt
         */
        fun createSyncKey(context: Context, seed: String, fixedSalt: ByteArray? = null) {
            val (salt, innerKey) = generateInnerKey(seed, fixedSalt)
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
            val saltString = Base64.encode(salt, Base64.NO_PADDING or Base64.NO_WRAP).toString(
                Charset.defaultCharset()
            )

            // Do bcrypt hash
            val hash = BCrypt.hashpw(seed, BCrypt.gensalt())

            context.getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit().apply {
                putString(Vals.SYNC_SALT_KEY, saltString)
                putString(Vals.HASH_KEY, hash)
                apply()
            }
        }

        /**
         * Get the key used encrypt/decrypt for syncing
         */
        fun getSyncKey(): SecretKey? {
            val keyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }
            val entry = keyStore.getEntry(SYNC_KEY_ALIAS, null)
            if (entry !is KeyStore.SecretKeyEntry) {
                Log.d("auth", "Key not a secret key")
                return null
            }
            return entry.secretKey
        }

        /**
         * Create key used to encrypt/decrypt database
         */
        fun createDataKey(): ByteArray {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            return keyBytes
        }

        /**
         * Get local key used to encrypt other secure values
         */
        fun getLocalKey(context: Context): MasterKey {
            val spec = KeyGenParameterSpec.Builder(
                LOCAL_KEY_ALIAS,
                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            return MasterKey.Builder(context).setKeyGenParameterSpec(spec).build()
        }

        /**
         * Get encrypted shared preferences
         */
        fun getSecurePrefs(context: Context): SharedPreferences {
            return EncryptedSharedPreferences(
                context,
                Vals.ENCRYPTED_SHARED_PREF_FILE,
                getLocalKey(context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}