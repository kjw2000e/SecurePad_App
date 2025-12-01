package com.kica.android.secure.keypad.security

import com.kica.android.secure.keypad.utils.StringUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RSAHelper {
    @Throws(Exception::class)
    fun encrypt(key: ByteArray?, content: ByteArray?): ByteArray? {
        val keyFactory = KeyFactory.getInstance("RSA")
        val pubSpec = X509EncodedKeySpec(key)
        val encryptionKey: Key? = keyFactory.generatePublic(pubSpec)

        val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsa.init(Cipher.ENCRYPT_MODE, encryptionKey)

        return rsa.doFinal(content)
    }

    @Throws(Exception::class)
    fun decrypt(key: ByteArray?, content: ByteArray?): ByteArray? {
        val keyFactory = KeyFactory.getInstance("RSA")
        val privSpec = PKCS8EncodedKeySpec(key)
        val decryptionKey: Key? = keyFactory.generatePrivate(privSpec)

        val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsa.init(Cipher.DECRYPT_MODE, decryptionKey)

        return rsa.doFinal(content)
    }

    @Throws(IOException::class)
    fun loadPublicKey(`is`: InputStream?): ByteArray {
        val br = BufferedReader(InputStreamReader(`is`))
        val builder = StringBuilder()

        var inKey = false
        var line = br.readLine()
        while (line != null) {
            if (!inKey) {
                if (line.startsWith("-----BEGIN ") && line.endsWith(" PUBLIC KEY-----")) {
                    inKey = true
                }
                line = br.readLine()
                continue
            } else {
                if (line.startsWith("-----END ") && line.endsWith(" PUBLIC KEY-----")) {
                    inKey = false
                    break
                }
                builder.append(line)
            }
            line = br.readLine()
        }

        return StringUtil.base64Decode(builder.toString())
    }

    @Throws(IOException::class)
    fun loadPrivateKey(`is`: InputStream?): ByteArray {
        val br = BufferedReader(InputStreamReader(`is`))
        val builder = StringBuilder()

        var inKey = false
        var line = br.readLine()
        while (line != null) {
            if (!inKey) {
                if (line.startsWith("-----BEGIN ") && line.endsWith(" PRIVATE KEY-----")) {
                    inKey = true
                }
                line = br.readLine()
                continue
            } else {
                if (line.startsWith("-----END ") && line.endsWith(" PRIVATE KEY-----")) {
                    inKey = false
                    break
                }
                builder.append(line)
            }
            line = br.readLine()
        }

        return StringUtil.base64Decode(builder.toString())
    }
}
