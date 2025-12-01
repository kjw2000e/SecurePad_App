package com.kica.android.secure.keypad.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HMAC (Hash-based Message Authentication Code) 헬퍼
 *
 * SHA-1, SHA-256 알고리즘을 사용한 HMAC 생성 유틸리티
 */
object MACHelper {

    /**
     * HMAC-SHA1 생성
     *
     * @param key 비밀 키
     * @param content 해시할 데이터
     * @return HMAC 결과
     * @throws Exception 암호화 과정에서 발생하는 예외
     */
    @Throws(Exception::class)
    fun hmacSha1(key: ByteArray?, content: ByteArray?): ByteArray? {
        return hmacSha("HmacSHA1", key, content)
    }

    /**
     * HMAC-SHA256 생성
     *
     * @param key 비밀 키
     * @param content 해시할 데이터
     * @return HMAC 결과
     * @throws Exception 암호화 과정에서 발생하는 예외
     */
    @Throws(Exception::class)
    fun hmacSha2(key: ByteArray?, content: ByteArray?): ByteArray? {
        return hmacSha("HmacSHA256", key, content)
    }

    /**
     * HMAC 생성 (내부 구현)
     *
     * @param algorithm HMAC 알고리즘 (예: "HmacSHA1", "HmacSHA256")
     * @param key 비밀 키
     * @param content 해시할 데이터
     * @return HMAC 결과
     */
    @Throws(Exception::class)
    private fun hmacSha(
        algorithm: String?,
        key: ByteArray?,
        content: ByteArray?
    ): ByteArray? {
        val mac = Mac.getInstance(algorithm)
        val secret = SecretKeySpec(key, mac.algorithm)
        mac.init(secret)
        return mac.doFinal(content)
    }
}