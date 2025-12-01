package com.kica.android.secure.keypad.domain.model

/**
 * 키패드 타입
 */
enum class KeypadType {
    /** 숫자만 (0-9) */
    NUMERIC,

    /** 영문 QWERTY */
    ENGLISH,

    /** 한글 (천지인) */
    KOREAN,

    /** 영문 + 숫자 */
    ALPHANUMERIC
}