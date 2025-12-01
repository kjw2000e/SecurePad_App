package com.kica.android.secure.keypad.security

import java.security.SecureRandom

object KeyManager {
    fun generateKey(length: Int): ByteArray {
        val random = SecureRandom()

        val key = ByteArray(length)
        random.nextBytes(key)

        return key
    }
}
