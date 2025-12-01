package com.kica.android.secure.keypad.security

import com.kica.android.secure.keypad.utils.StringUtil
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESHelper {
    @Throws(Exception::class)
    fun encrypt(key: ByteArray?, content: ByteArray?): ByteArray? {
        if (key == null || content == null) return null

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        val keyBytes = ByteArray(16)
        var length = key.size
        if (length > keyBytes.size) length = keyBytes.size
        System.arraycopy(key, 0, keyBytes, 0, length)

        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(keyBytes)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(content)
    }

    @Throws(Exception::class)
    fun decrypt(key: ByteArray?, content: ByteArray?): ByteArray? {
        if (key == null || content == null) return null

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        val keyBytes = ByteArray(16)
        var length = key.size
        if (length > keyBytes.size) length = keyBytes.size
        System.arraycopy(key, 0, keyBytes, 0, length)

        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(keyBytes)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(content)
    }

    @Throws(Exception::class)
    fun encryptHex(key: ByteArray?, content: ByteArray?): String? {
        val encrypt = encrypt(key, content) ?: return null

        return StringUtil.hexEncode(encrypt)
    }
}
