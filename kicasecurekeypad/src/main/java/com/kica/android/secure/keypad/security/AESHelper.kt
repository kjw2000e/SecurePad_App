package com.kica.android.secure.keypad.security

import com.kica.android.secure.keypad.utils.StringUtil
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESHelper {
    /**
     * AES-256 암호화 (키와 IV 분리)
     *
     * @param key 32바이트 AES-256 키
     * @param iv 16바이트 IV
     * @param content 암호화할 데이터 (16바이트 배수여야 함)
     * @return 암호화된 데이터
     * @throws IllegalArgumentException 키 또는 IV 크기가 잘못된 경우
     * @throws Exception 암호화 실패 시
     */
    @Throws(Exception::class)
    fun encrypt(key: ByteArray, iv: ByteArray, content: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires 32-byte key, got ${key.size}" }
        require(iv.size == 16) { "IV must be 16 bytes, got ${iv.size}" }

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(content)
    }

    /**
     * AES-256 복호화 (키와 IV 분리)
     *
     * @param key 32바이트 AES-256 키
     * @param iv 16바이트 IV
     * @param content 복호화할 데이터
     * @return 복호화된 데이터
     * @throws IllegalArgumentException 키 또는 IV 크기가 잘못된 경우
     * @throws Exception 복호화 실패 시
     */
    @Throws(Exception::class)
    fun decrypt(key: ByteArray, iv: ByteArray, content: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires 32-byte key, got ${key.size}" }
        require(iv.size == 16) { "IV must be 16 bytes, got ${iv.size}" }

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(content)
    }

    /**
     * AES-256 암호화 후 Hex 인코딩
     *
     * @param key 32바이트 AES-256 키
     * @param iv 16바이트 IV
     * @param content 암호화할 데이터
     * @return Hex 인코딩된 암호문
     * @throws Exception 암호화 실패 시
     */
    @Throws(Exception::class)
    fun encryptHex(key: ByteArray, iv: ByteArray, content: ByteArray): String {
        val encrypted = encrypt(key, iv, content)
        return StringUtil.hexEncode(encrypted)
    }
}
