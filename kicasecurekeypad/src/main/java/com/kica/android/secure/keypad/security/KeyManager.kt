package com.kica.android.secure.keypad.security

import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyManager {
    /**
     * AES-256 키 생성 (표준 KeyGenerator 사용)
     *
     * @return SecretKey AES-256 키 (32바이트)
     */
    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)  // AES-256
        return keyGen.generateKey()
    }

    /**
     * IV(Initialization Vector) 생성
     *
     * @return ByteArray 16바이트 랜덤 IV
     */
    fun generateIV(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * 키 + IV 결합
     *
     * 서버 전송을 위해 32바이트 키와 16바이트 IV를 48바이트로 결합합니다.
     *
     * @param key AES-256 SecretKey
     * @param iv 16바이트 IV
     * @return ByteArray 48바이트 결합 데이터 (키 32 + IV 16)
     */
    fun combineKeyAndIV(key: SecretKey, iv: ByteArray): ByteArray {
        require(key.encoded.size == 32) { "AES-256 key must be 32 bytes, got ${key.encoded.size}" }
        require(iv.size == 16) { "IV must be 16 bytes, got ${iv.size}" }

        val combined = ByteArray(48)
        System.arraycopy(key.encoded, 0, combined, 0, 32)
        System.arraycopy(iv, 0, combined, 32, 16)
        return combined
    }

    /**
     * 결합된 데이터에서 키 추출
     *
     * @param combined 48바이트 결합 데이터
     * @return ByteArray 32바이트 AES-256 키
     */
    fun extractKey(combined: ByteArray): ByteArray {
        require(combined.size == 48) { "Combined data must be 48 bytes, got ${combined.size}" }
        return combined.copyOfRange(0, 32)
    }

    /**
     * 결합된 데이터에서 IV 추출
     *
     * @param combined 48바이트 결합 데이터
     * @return ByteArray 16바이트 IV
     */
    fun extractIV(combined: ByteArray): ByteArray {
        require(combined.size == 48) { "Combined data must be 48 bytes, got ${combined.size}" }
        return combined.copyOfRange(32, 48)
    }
}
