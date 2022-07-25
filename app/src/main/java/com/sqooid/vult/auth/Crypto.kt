package com.sqooid.vult.auth

import android.util.Base64
import com.sqooid.vult.fragments.credential.PasswordGeneratorSettings
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Crypto {
    companion object {
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
            val builder = StringBuilder()
            val random = SecureRandom()
            for (i in 1..settings.length) {
                val step = i % 10
                if (step == 1 && settings.useUppercase) {
                    builder.append(upper[random.nextInt(upper.length)])
                } else if (step == 2 && settings.useNumbers) {
                    builder.append(nums[random.nextInt(nums.length)])
                } else if (step == 3 && settings.useSymbols) {
                    builder.append(syms[random.nextInt(syms.length)])
                } else {
                    builder.append(lower[random.nextInt(lower.length)])
                }
            }
            return builder.toString()
        }
    }
}