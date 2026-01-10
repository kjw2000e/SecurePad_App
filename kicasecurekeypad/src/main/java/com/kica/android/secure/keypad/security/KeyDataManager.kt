package com.kica.android.secure.keypad.security

import android.content.Context
import com.kica.android.secure.keypad.utils.StringUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom

/**
 * 키 입력 데이터 관리자 (UTF-8 다중 바이트 문자 지원)
 *
 * ## 암호화 블록 포맷 (32 bytes per character)
 * ```
 * | Length (1 byte) | Data (max 15 bytes) | Padding (16-Length bytes) | Random (16 bytes) |
 * ```
 *
 * - Length: 실제 데이터 바이트 수 (1~15)
 * - Data: UTF-8 인코딩된 문자 데이터
 * - Padding: 0x00으로 채움
 * - Random: SecureRandom으로 생성된 패딩 (패턴 분석 방지)
 *
 * ## 지원 문자
 * - ASCII (1 byte): 영문, 숫자, 기호
 * - Korean (3 bytes): 한글 완성형
 * - Emoji (4 bytes): 이모지 일부
 */
class KeyDataManager private constructor() {

    private val symmetricKey = ByteArray(LEN_SYMMETRIC_KEY)
    private val encryptedSymmetricKey: ByteArray = ByteArray(LEN_ASYMMETRIC_KEY)
    private val secureRandom = SecureRandom()

    // 암호화된 문자 블록 리스트 (각 문자별 32바이트 블록)
    private val encryptedBlocks = mutableListOf<ByteArray>()

    // 원문 문자 리스트 (복호화 없이 빠른 접근용, 메모리 내에서만 유지)
    private val plainCharacters = mutableListOf<String>()

    /**
     * 입력 개수 리턴
     */
    val inputCount: Int
        get() = encryptedBlocks.size

    /**
     * 헥사 텍스트 타입 암호화된 입력 값 리턴 (로컬)
     */
    val encryptedDataHex: String
        get() {
            if (encryptedBlocks.isEmpty()) return ""
            val combined = ByteArrayOutputStream()
            encryptedBlocks.forEach { combined.write(it) }
            return StringUtil.hexEncode(combined.toByteArray())
        }

    /**
     * 초기화
     *
     * 변수 초기화, 대칭키 생성, 대칭키 암호화
     *
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun initialize() {
        // 변수 초기화
        encryptedBlocks.clear()
        plainCharacters.clear()

        // 대칭키 생성
        val newKey = KeyManager.generateKey(LEN_SYMMETRIC_KEY)
        System.arraycopy(newKey, 0, symmetricKey, 0, LEN_SYMMETRIC_KEY)

        // RSA로 대칭키 암호화
        encryptSymmetricKey()
    }

    /**
     * 문자 입력 암호화 (UTF-8 다중 바이트 지원)
     *
     * @param character 입력 문자 (한 글자)
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun appendCharacter(character: String) {
        if (character.isEmpty()) return

        val charBytes = character.toByteArray(Charsets.UTF_8)

        // 최대 지원 바이트 수 체크 (15바이트까지 지원)
        if (charBytes.size > MAX_CHAR_BYTES) {
            throw KeyDataException("Character too large: ${charBytes.size} bytes (max: $MAX_CHAR_BYTES)")
        }

        try {
            // 블록 생성: | Length (1) | Data (15) | Random (16) | = 32 bytes
            val block = ByteArray(LEN_BLOCK)

            // 1. Length 바이트 설정
            block[0] = charBytes.size.toByte()

            // 2. 데이터 복사 (최대 15바이트)
            System.arraycopy(charBytes, 0, block, 1, charBytes.size)

            // 3. 나머지는 랜덤 패딩으로 채움 (패턴 분석 방지)
            val randomPadding = ByteArray(LEN_BLOCK - 1 - charBytes.size)
            secureRandom.nextBytes(randomPadding)
            System.arraycopy(randomPadding, 0, block, 1 + charBytes.size, randomPadding.size)

            // AES 암호화 (2개의 16바이트 블록)
            val encryptedBlock1 = AESHelper.encrypt(symmetricKey, block.copyOfRange(0, 16))
            val encryptedBlock2 = AESHelper.encrypt(symmetricKey, block.copyOfRange(16, 32))

            if (encryptedBlock1 == null || encryptedBlock2 == null) {
                throw KeyDataException("Failed to encrypt character data.")
            }

            // 암호화된 블록 결합
            val encryptedFull = ByteArray(LEN_BLOCK)
            System.arraycopy(encryptedBlock1, 0, encryptedFull, 0, 16)
            System.arraycopy(encryptedBlock2, 0, encryptedFull, 16, 16)

            encryptedBlocks.add(encryptedFull)
            plainCharacters.add(character)

        } catch (e: KeyDataException) {
            throw e
        } catch (e: Exception) {
            throw KeyDataException("Failed to encrypt key data: ${e.message}")
        }
    }

    /**
     * 입력 키 암호화 (하위 호환성 - 단일 바이트)
     *
     * @param keyData 입력 데이터 (첫 번째 바이트만 사용됨 - deprecated)
     * @throws KeyDataException
     * @deprecated Use appendCharacter(String) instead for multi-byte support
     */
    @Throws(KeyDataException::class)
    @Deprecated("Use appendCharacter(String) instead", ReplaceWith("appendCharacter(String(keyData, Charsets.UTF_8))"))
    fun appendKeyData(keyData: ByteArray?) {
        if (keyData == null || keyData.isEmpty()) return

        // UTF-8 문자열로 변환하여 새 API 사용
        val character = String(keyData, Charsets.UTF_8)
        appendCharacter(character)
    }

    /**
     * 마지막 입력 문자 삭제
     */
    fun removeKeyData() {
        if (encryptedBlocks.isEmpty()) return

        encryptedBlocks.removeAt(encryptedBlocks.lastIndex)
        if (plainCharacters.isNotEmpty()) {
            plainCharacters.removeAt(plainCharacters.lastIndex)
        }
    }

    /**
     * 모든 입력 데이터 삭제
     */
    fun removeAllKeyData() {
        // 보안을 위해 데이터 제로화
        encryptedBlocks.forEach { block -> block.fill(0) }
        encryptedBlocks.clear()
        plainCharacters.clear()
    }

    /**
     * 대칭키 리턴
     *
     * @return 대칭키 복사본 (원본 보호를 위해 복사본 반환)
     */
    fun getSymmetricKey(): ByteArray {
        return symmetricKey.copyOf()
    }

    /**
     * 대칭키 설정
     *
     * @param key 대칭키
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun setSymmetricKey(key: ByteArray?) {
        if (key == null || key.size != LEN_SYMMETRIC_KEY) return

        System.arraycopy(key, 0, symmetricKey, 0, LEN_SYMMETRIC_KEY)
        encryptSymmetricKey()
    }

    /**
     * 평문 텍스트 리턴 (복호화 없이 메모리에서 직접 반환)
     *
     * @return 평문 문자열
     */
    val plainText: ByteArray?
        get() {
            if (plainCharacters.isEmpty()) return null
            return plainCharacters.joinToString("").toByteArray(Charsets.UTF_8)
        }

    /**
     * 평문 문자열 리턴
     *
     * @return 평문 문자열
     */
    val plainString: String
        get() = plainCharacters.joinToString("")

    /**
     * 바이트 배열 타입 암호화된 입력 값 리턴 (로컬)
     */
    val encryptedData: ByteArray
        get() {
            if (encryptedBlocks.isEmpty()) return ByteArray(0)
            val combined = ByteArrayOutputStream()
            encryptedBlocks.forEach { combined.write(it) }
            return combined.toByteArray()
        }

    /**
     * 암호화된 입력 데이터를 복호화 (로컬)
     *
     * @param encryptedKeyData 암호화된 데이터
     * @return 복호화된 문자열
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun getDecryptedString(encryptedKeyData: ByteArray?): String {
        if (encryptedKeyData == null || encryptedKeyData.isEmpty()) return ""

        if (encryptedKeyData.size % LEN_BLOCK != 0) {
            throw KeyDataException("Invalid encrypted data length: ${encryptedKeyData.size}")
        }

        val result = StringBuilder()
        var offset = 0

        while (offset < encryptedKeyData.size) {
            try {
                // 32바이트 블록 추출
                val encryptedBlock = encryptedKeyData.copyOfRange(offset, offset + LEN_BLOCK)

                // AES 복호화 (2개의 16바이트 블록)
                val decryptedBlock1 = AESHelper.decrypt(symmetricKey, encryptedBlock.copyOfRange(0, 16))
                val decryptedBlock2 = AESHelper.decrypt(symmetricKey, encryptedBlock.copyOfRange(16, 32))

                if (decryptedBlock1 == null || decryptedBlock2 == null) {
                    throw KeyDataException("Failed to decrypt block at offset $offset")
                }

                // 블록 결합
                val decryptedFull = ByteArray(LEN_BLOCK)
                System.arraycopy(decryptedBlock1, 0, decryptedFull, 0, 16)
                System.arraycopy(decryptedBlock2, 0, decryptedFull, 16, 16)

                // Length 읽기
                val length = decryptedFull[0].toInt() and 0xFF
                if (length > MAX_CHAR_BYTES) {
                    throw KeyDataException("Invalid character length: $length")
                }

                // 데이터 추출
                val charBytes = decryptedFull.copyOfRange(1, 1 + length)
                result.append(String(charBytes, Charsets.UTF_8))

            } catch (e: KeyDataException) {
                throw e
            } catch (e: Exception) {
                throw KeyDataException("Failed to decrypt at offset $offset: ${e.message}")
            }

            offset += LEN_BLOCK
        }

        return result.toString()
    }

    /**
     * 하위 호환성 - 바이트 배열로 복호화 결과 반환
     */
    @Throws(KeyDataException::class)
    fun getDecryptedData(encryptedKeyData: ByteArray?): ByteArray? {
        val decrypted = getDecryptedString(encryptedKeyData)
        return if (decrypted.isEmpty()) null else decrypted.toByteArray(Charsets.UTF_8)
    }

    /**
     * 바이트 배열 타입 암호화된 입력 값 리턴 (서버 E2E)
     *
     * Data Format:
     * | RSA Encrypted symmetric key (256 Bytes) | Encrypted key data (32 bytes per character) | MAC (20 bytes, HmacSha1) |
     */
    @get:Throws(KeyDataException::class)
    val encryptedE2eData: ByteArray?
        get() {
            if (encryptedBlocks.isEmpty()) return null

            val encryptedDataBytes = encryptedData
            val totalLength = LEN_ASYMMETRIC_KEY + encryptedDataBytes.size + LEN_MAC
            val result = ByteArray(totalLength)
            var pos = 0

            // 1. RSA 암호화된 대칭키
            System.arraycopy(encryptedSymmetricKey, 0, result, pos, LEN_ASYMMETRIC_KEY)
            pos += LEN_ASYMMETRIC_KEY

            // 2. 암호화된 키 데이터
            System.arraycopy(encryptedDataBytes, 0, result, pos, encryptedDataBytes.size)
            pos += encryptedDataBytes.size

            // 3. HMAC-SHA1 (대칭키 + 암호화 데이터에 대한 MAC)
            try {
                val dataForMac = result.copyOfRange(0, pos)
                val mac = MACHelper.hmacSha1(symmetricKey, dataForMac)
                    ?: throw KeyDataException("Failed to generate MAC")
                System.arraycopy(mac, 0, result, pos, LEN_MAC)
            } catch (e: KeyDataException) {
                throw e
            } catch (e: Exception) {
                throw KeyDataException("Failed to generate MAC data: ${e.message}")
            }

            return result
        }

    /**
     * 헥사 텍스트 타입 암호화된 입력 값 리턴 (서버)
     */
    @get:Throws(KeyDataException::class)
    val encryptedE2eDataHex: String
        get() {
            val data = encryptedE2eData
            return if (data == null) "" else StringUtil.hexEncode(data)
        }

    /**
     * 입력 필드에 표기할 텍스트 리턴 (마스킹용)
     */
    val inputText: String
        get() = if (encryptedBlocks.isEmpty()) "" else StringUtil.randomString(encryptedBlocks.size)

    @Throws(KeyDataException::class)
    private fun encryptSymmetricKey() {
        try {
            val assetMgr = mContext!!.assets
            val inputStream = assetMgr.open("vkeypad_public.pem")

            val publicKey = RSAHelper.loadPublicKey(inputStream)
            val encrypted = RSAHelper.encrypt(publicKey, symmetricKey)
                ?: throw KeyDataException("RSA encryption returned null")

            System.arraycopy(encrypted, 0, encryptedSymmetricKey, 0, LEN_ASYMMETRIC_KEY)

            inputStream.close()
        } catch (e: IOException) {
            throw KeyDataException("Couldn't find RSA public key file: ${e.message}")
        } catch (e: KeyDataException) {
            throw e
        } catch (e: Exception) {
            throw KeyDataException("Failed to encrypt symmetric key: ${e.message}")
        }
    }

    companion object {
        private const val LEN_SYMMETRIC_KEY = 16      // AES-128 키 길이
        private const val LEN_ASYMMETRIC_KEY = 256    // RSA-2048 암호문 길이
        private const val LEN_BLOCK = 32              // 문자당 암호화 블록 크기
        private const val MAX_CHAR_BYTES = 15         // 단일 문자 최대 바이트 (UTF-8)
        private const val LEN_MAC = 20                // HMAC-SHA1 길이

        private var mContext: Context? = null
        private val instance = KeyDataManager()

        /**
         * KeyDataManager 싱글톤 인스턴스 반환
         *
         * @param context Context (자동으로 applicationContext로 변환되어 메모리 누수 방지)
         * @return KeyDataManager 인스턴스
         */
        fun getInstance(context: Context): KeyDataManager {
            mContext = context.applicationContext
            return instance
        }
    }
}
