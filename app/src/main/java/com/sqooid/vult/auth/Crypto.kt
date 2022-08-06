package com.sqooid.vult.auth

import android.util.Base64
import com.sqooid.vult.fragments.credential.PasswordGeneratorSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

class Crypto {
    companion object {
        fun generateSeededKey(
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
         * Create key used to encrypt/decrypt database
         */
        fun generateKey(): String {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            return Base64.encode(keyBytes, Base64.NO_PADDING or Base64.NO_WRAP).toString(Charset.defaultCharset())
        }

        fun createHash(seed: String): String {
            return BCrypt.hashpw(seed, BCrypt.gensalt())
        }


        fun encrypt(key: SecretKey, message: String): String {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cipherBytes = cipher.doFinal(message.toByteArray())
            val iv = cipher.iv

            val ivText = String(Base64.encode(iv, Base64.NO_PADDING or Base64.NO_WRAP))
            val cipherText = String(Base64.encode(cipherBytes, Base64.NO_PADDING or Base64.NO_WRAP))
            return "$ivText:$cipherText"
        }

        fun decrypt(key: SecretKey, text: String): String {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val split = text.split(":")
            val iv = Base64.decode(split[0], Base64.NO_PADDING or Base64.NO_WRAP)
            val cipherText = Base64.decode(split[1], Base64.NO_PADDING or Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            return cipher.doFinal(cipherText).toString(Charset.defaultCharset())
        }

        inline fun <reified T> encryptObj(key: SecretKey, obj: T): String? {
            return try {
                val objStr = Json.encodeToString(obj)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val cipherBytes = cipher.doFinal(objStr.toByteArray())
                val iv = cipher.iv

                val ivText = String(Base64.encode(iv, Base64.NO_PADDING or Base64.NO_WRAP))
                val cipherText =
                    String(Base64.encode(cipherBytes, Base64.NO_PADDING or Base64.NO_WRAP))
                "$ivText:$cipherText"
            } catch (e: Exception) {
                null
            }
        }

        inline fun <reified T> decryptObj(key: SecretKey, text: String): T? {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val split = text.split(":")
            val iv = Base64.decode(split[0], Base64.NO_PADDING or Base64.NO_WRAP)
            val cipherText = Base64.decode(split[1], Base64.NO_PADDING or Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val clearBytes = cipher.doFinal(cipherText)

            return try {
                Json.decodeFromString<T>(clearBytes.toString(Charset.defaultCharset()))
            } catch (e: Exception) {
                null
            }
        }

        fun generateId(bytes: Int): String {
            val array = ByteArray(bytes)
            SecureRandom().nextBytes(array)
            return Base64.encode(array, Base64.NO_WRAP or Base64.NO_PADDING)
                .toString(Charset.defaultCharset())
        }

        fun generatePassword(
            settings: PasswordGeneratorSettings
        ): String {
            val lower = "qwertyuiopasdfghjklzxcvbnm"
            val upper = "QWERTYUIOPASDFGHJKLZXCVBNM"
            val nums = "1234567890"
            val syms = "!@#$%^&*+=-()`~;:?."
            // Start chucking in other chars every 10 chars
            val builder = ArrayList<Char>()
            val random = SecureRandom()
            for (i in 1..settings.length) {
                val step = i % 10
                if (step == 1 && settings.useUppercase) {
                    builder.add(upper[random.nextInt(upper.length)])
                } else if (step == 2 && settings.useNumbers) {
                    builder.add(nums[random.nextInt(nums.length)])
                } else if (step == 3 && settings.useSymbols) {
                    builder.add(syms[random.nextInt(syms.length)])
                } else {
                    builder.add(lower[random.nextInt(lower.length)])
                }
            }
            builder.shuffle(random)
            return builder.joinToString("")
        }
    }
}