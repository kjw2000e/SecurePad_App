package com.kica.android.secure.keypad.security

import android.content.Context
import com.kica.android.secure.keypad.utils.StringUtil
import java.io.IOException

class KeyDataManager private constructor() {
    private val symmetricKey = ByteArray(LEN_SYMMETRIC_KEY)
    private val encryptedSymmetricKey: ByteArray? = ByteArray(LEN_ASYMMETRIC_KEY)

    /**
     * 헥사 텍스트 타입 암호화된 입력 값 리턴 (로컬)
     *
     * @return
     */
    var encryptedDataHex: String = ""
        private set

    /**
     * 입력 개수 리턴
     *
     * @return
     */
    var inputCount: Int = 0
        private set

    /**
     * 초기화
     *
     * 변수 초기화, 대칭키 생성, 대칭키 암호화
     *
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun initialize() {
        // initialize variables
        this.encryptedDataHex = ""
        this.inputCount = 0

        // generate symmetric key
        System.arraycopy(KeyManager.generateKey(LEN_SYMMETRIC_KEY), 0, symmetricKey, 0, LEN_SYMMETRIC_KEY)

        // encrypt symmetric key by rsa public key
        encryptSymmetricKey()
    }

    /**
     * 입력 키 암호화
     *
     * @param keyData
     * @throws Exception
     */
    @Throws(KeyDataException::class)
    fun appendKeyData(keyData: ByteArray?) {
        if (keyData == null) {
            return
        }

        // 같은 입력 시 암호 데이터가 동일할 경우 유추가 가능하여 첫번째 바이트를 제외한 나머지는 랜덤 데이터를 넣음.
        val madeKeyData = ByteArray(LEN_KEYDATA)
        System.arraycopy(StringUtil.randomString(LEN_KEYDATA).toByte(), 0, madeKeyData, 0, LEN_KEYDATA)
        System.arraycopy(keyData, 0, madeKeyData, 0, 1)

        try {
            this.encryptedDataHex += AESHelper.encryptHex(symmetricKey, madeKeyData)
        } catch (e: Exception) {
            throw KeyDataException("Failed to encrypt key data.")
        }

        this.inputCount += 1
    }

    /**
     * 입력 키 삭제
     *
     * 마지막 입력 키부터 삭제
     *
     */
    fun removeKeyData() {
        if (this.inputCount == 0) {
            this.encryptedDataHex = ""
            return
        }

        this.encryptedDataHex = encryptedDataHex.substring(0, encryptedDataHex.length - LEN_KEYDATA * 2)
        this.inputCount -= 1
    }

    /**
     * 입력 키 모두 삭제
     */
    fun removeAllKeyData() {
        this.encryptedDataHex = ""
        this.inputCount = 0
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
     * @param key
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun setSymmetricKey(key: ByteArray?) {
        if (key == null || key.size != LEN_SYMMETRIC_KEY) {
            return
        }

        System.arraycopy(key, 0, symmetricKey, 0, LEN_SYMMETRIC_KEY)

        encryptSymmetricKey()
    }

    @get:Throws(KeyDataException::class)
    val plainText: ByteArray?
        /**
         * 바이트 배열 타입 평문 입력 값 리턴 (로컬)
         *
         * @return
         * @throws KeyDataException
         */
        get() {
            if (this.inputCount == 0) {
                return null
            }

            val encryptedKeyData: ByteArray = StringUtil.hexDecode(this.encryptedDataHex)
            val result = ByteArray(this.inputCount)
            val tmpKeyData = ByteArray(LEN_KEYDATA)

            // decryption
            for (i in 0..<this.inputCount) {
                try {
                    System.arraycopy(encryptedKeyData, i * LEN_KEYDATA, tmpKeyData, 0, LEN_KEYDATA)

                    val keyData = AESHelper.decrypt(symmetricKey, tmpKeyData)
                    if (keyData == null) {
                        return null
                    }

                    result[i] = keyData[0]
                } catch (e: Exception) {
                    throw KeyDataException("Failed to decrypt encrypted key data.")
                }
            }

            return result
        }

    val encryptedData: ByteArray
        /**
         * 바이트 배열 타입 암호화된 입력 값 리턴 (로컬)
         *
         * @return
         */
        get() =
            /**
             * Data Format
             *
             * |Encrypted key data (16 bytes per character)|...
             *
             */

            StringUtil.hexDecode(this.encryptedDataHex)

    /**
     * 암호화된 입력 데이터를 복호화 (로컬)
     *
     * > "getEncryptedData" 함수에 의해 리턴된 암호 데이터를 복호화 할 때 사용
     *
     * @param encryptedKeyData
     * @return
     * @throws KeyDataException
     */
    @Throws(KeyDataException::class)
    fun getDecryptedData(encryptedKeyData: ByteArray?): ByteArray? {
        if (encryptedKeyData == null || encryptedKeyData.size == 0) {
            return null
        }

        val length = encryptedKeyData.size

        if (length % LEN_KEYDATA != 0) {
            return null
        }

        val result = ByteArray(length / LEN_KEYDATA)
        val tmpKeyData = ByteArray(LEN_KEYDATA)

        // decryption
        var i = 0
        while (i < length) {
            try {
                System.arraycopy(encryptedKeyData, i, tmpKeyData, 0, LEN_KEYDATA)

                val keyData = AESHelper.decrypt(symmetricKey, tmpKeyData)
                if (keyData == null) {
                    return null
                }

                result[i / LEN_KEYDATA] = keyData[0]
            } catch (e: Exception) {
                throw KeyDataException("Failed to decrypt encrypted key data.")
            }
            i += LEN_KEYDATA
        }

        return result
    }

    @get:Throws(KeyDataException::class)
    val encryptedE2eData: ByteArray?
        /**
         * 바이트 배열 타입 암호화된 입력 값 리턴 (서버)
         *
         * @return
         */
        get() {
            /**
             * Data Format
             *
             * |RSA Encrypted symmetric key (256 Bytes)|Encrypted key data (16 bytes per character)|Mac (20 bytes, HmacSha1)
             *
             */
            val encryptedE2eData = ByteArray(LEN_ASYMMETRIC_KEY + LEN_SYMMETRIC_KEY * this.inputCount + LEN_MAC)
            var pos = 0

            // Encrypted symmetric key
            if (encryptedSymmetricKey == null || encryptedSymmetricKey.size == 0) return null
            System.arraycopy(encryptedSymmetricKey, 0, encryptedE2eData, 0, LEN_ASYMMETRIC_KEY)
            pos += LEN_ASYMMETRIC_KEY

            // Encrypted key data
            if (this.inputCount == 0) {
                return null
            }
            System.arraycopy(StringUtil.hexDecode(this.encryptedDataHex), 0, encryptedE2eData, pos, LEN_SYMMETRIC_KEY * this.inputCount)
            pos += LEN_SYMMETRIC_KEY * this.inputCount

            // MAC
            val encryptedE2eDataWithoutMac = ByteArray(LEN_ASYMMETRIC_KEY + LEN_SYMMETRIC_KEY * this.inputCount)
            System.arraycopy(encryptedE2eData, 0, encryptedE2eDataWithoutMac, 0, LEN_ASYMMETRIC_KEY + LEN_SYMMETRIC_KEY * this.inputCount)
            try {
                System.arraycopy(MACHelper.hmacSha1(symmetricKey, encryptedE2eDataWithoutMac), 0, encryptedE2eData, pos, LEN_MAC)
            } catch (e: Exception) {
                throw KeyDataException("Failed to generate mac data.")
            }

            return encryptedE2eData
        }

    @get:Throws(KeyDataException::class)
    val encryptedE2eDataHex: String
        /**
         * 헥사 텍스트 타입 암호화된 입력 값 리턴 (서버)
         *
         * @return
         */
        get() {
            val data = this.encryptedE2eData
            return if (data == null) "" else StringUtil.hexEncode(data)
        }

    val inputText: String?
        /**
         * 입력 필드에 표기할 텍스트 리턴 (랜덤 값)
         *
         * @return
         */
        get() {
            if (this.inputCount == 0) {
                return ""
            }

            return StringUtil.randomString(this.inputCount)
        }

    @Throws(KeyDataException::class)
    private fun encryptSymmetricKey() {
        try {
            // load rsa public key file
            val assetMgr = mContext!!.getAssets()
            val `is` = assetMgr.open("vkeypad_public.pem")

            // encryption symmetric key
            System.arraycopy(RSAHelper.encrypt(RSAHelper.loadPublicKey(`is`), symmetricKey), 0, encryptedSymmetricKey, 0, LEN_ASYMMETRIC_KEY)
        } catch (e: IOException) {
            throw KeyDataException("Couldn't find rsa public key file.")
        } catch (e: Exception) {
            throw KeyDataException("Failed to encrypt symmetric key.")
        }
    }

    companion object {
        private const val LEN_SYMMETRIC_KEY = 16
        private const val LEN_ASYMMETRIC_KEY = 256
        private const val LEN_KEYDATA = 16
        private const val LEN_MAC = 20

        private var mContext: Context? = null
        private val instance = KeyDataManager()

        /**
         * KeyDataManager 싱글톤 인스턴스 반환
         *
         * @param context Context (자동으로 applicationContext로 변환되어 메모리 누수 방지)
         * @return KeyDataManager 인스턴스
         */
        fun getInstance(context: Context): KeyDataManager {
            // Activity Context가 넘어와도 applicationContext로 변환하여 메모리 누수 방지
            mContext = context.applicationContext
            return instance
        }
    }
}
