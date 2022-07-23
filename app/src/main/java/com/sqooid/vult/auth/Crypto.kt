package com.sqooid.vult.auth

import android.util.Base64
import android.util.Log
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
    }
}