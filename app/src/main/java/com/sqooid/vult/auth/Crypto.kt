package com.sqooid.vult.auth

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Crypto {
    fun encrypt(key: SecretKey, message: String): String {

        val salt = byteArrayOf(32)
        val random = SecureRandom()
        random.nextBytes(salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(message.toByteArray())
        val iv = cipher.iv

        val result = String(Base64.getEncoder().encode(iv))
        result.plus(":")
        result.plus(Base64.getEncoder().encode(cipherText))
        return result
    }
    fun decrypt(key: SecretKey, text: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val split = text.split(":")
        val iv = Base64.getDecoder().decode(split[0])
        val cipherText = Base64.getDecoder().decode(split[1])
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(96, iv))
        return String(cipher.doFinal(cipherText))
    }
}