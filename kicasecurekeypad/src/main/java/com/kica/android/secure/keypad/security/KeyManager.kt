package com.kica.android.secure.keypad.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 키 관리자 (Android Keystore 지원)
 *
 * ## 키 종류
 * - **세션 키 (E2E용)**: 매 세션마다 생성, RSA로 감싸서 서버 전송
 * - **마스터 키 (로컬용)**: Android Keystore에 저장, TEE로 보호
 *
 * ## 보안 수준
 * - 마스터 키: TEE(하드웨어)에 저장되어 추출 불가
 * - 세션 키: 메모리에 임시 저장, 서버 전송용
 */
object KeyManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "keypad_master_key"
    private const val GCM_TAG_LENGTH = 128  // bits

    // ============================================================
    // Android Keystore - 로컬 암호화용 마스터 키
    // ============================================================

    /**
     * Keystore 마스터 키 조회 또는 생성
     *
     * TEE(Trusted Execution Environment)에 저장되어 앱 메모리에 노출되지 않습니다.
     * 인증 없이 바로 사용 가능하도록 설정됩니다.
     *
     * @return SecretKey Keystore에 저장된 AES-256 키
     */
    fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        // 이미 존재하면 반환
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        }

        // 새로 생성
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)  // AES-256
                // 인증 없이 사용 (setUserAuthenticationRequired 미설정)
                .build()
        )

        return keyGenerator.generateKey()
    }

    /**
     * Keystore 마스터 키로 암호화 (GCM 모드)
     *
     * @param plaintext 암호화할 평문 데이터
     * @return ByteArray IV(12바이트) + 암호문 결합 데이터
     */
    fun encryptWithMasterKey(plaintext: ByteArray): ByteArray {
        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)

        val iv = cipher.iv  // GCM에서 자동 생성된 IV (12바이트)
        val ciphertext = cipher.doFinal(plaintext)

        // IV + 암호문 결합
        return iv + ciphertext
    }

    /**
     * Keystore 마스터 키로 복호화 (GCM 모드)
     *
     * @param encryptedData IV(12바이트) + 암호문 결합 데이터
     * @return ByteArray 복호화된 평문 데이터
     */
    fun decryptWithMasterKey(encryptedData: ByteArray): ByteArray {
        require(encryptedData.size > 12) { "Encrypted data too short" }

        val masterKey = getOrCreateMasterKey()
        val iv = encryptedData.copyOfRange(0, 12)
        val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return cipher.doFinal(ciphertext)
    }

    /**
     * Keystore 마스터 키 존재 여부 확인
     *
     * @return Boolean 키 존재 여부
     */
    fun hasMasterKey(): Boolean {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        return keyStore.containsAlias(MASTER_KEY_ALIAS)
    }

    /**
     * Keystore 마스터 키 삭제
     *
     * 앱 삭제/초기화 시 호출할 수 있습니다.
     */
    fun deleteMasterKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
        }
    }

    // ============================================================
    // 세션 키 - E2E 서버 전송용 (기존 로직)
    // ============================================================

    /**
     * AES-256 세션 키 생성 (표준 KeyGenerator 사용)
     *
     * 서버 E2E 전송용으로 사용됩니다. RSA로 감싸서 서버에 전송합니다.
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
