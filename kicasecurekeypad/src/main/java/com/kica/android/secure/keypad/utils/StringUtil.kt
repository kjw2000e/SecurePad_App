package com.kica.android.secure.keypad.utils

import android.util.Base64
import java.util.Random

object StringUtil {
    fun hexEncode(content: ByteArray): String {
        val sb = StringBuilder()
        for (b in content) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    fun hexDecode(content: String): ByteArray {
        val len = content.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((content.get(i).digitToIntOrNull(16) ?: -1 shl 4) + content.get(i + 1).digitToIntOrNull(16)!!
                ?: -1).toByte()
            i += 2
        }
        return data
    }

    /**
     * Base64 인코딩
     */
    fun base64Encode(content: ByteArray): String {
        return Base64.encodeToString(content, Base64.DEFAULT)
    }

    /**
     * Base64 디코딩
     */
    fun base64Decode(content: String): ByteArray {
        return Base64.decode(content, Base64.DEFAULT)
    }

    /**
     * 랜덤 문자열 생성
     */
    fun randomString(length: Int): String {
        val buffer = StringBuffer()
        val random = Random()

        val chars: Array<String?>? = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in 0..<length) {
            buffer.append(chars!![random.nextInt(chars.size)])
        }

        return buffer.toString()
    }
}
