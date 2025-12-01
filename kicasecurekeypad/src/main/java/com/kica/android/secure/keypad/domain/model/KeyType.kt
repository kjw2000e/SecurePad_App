package com.kica.android.secure.keypad.domain.model

/**
 * 키 타입
 */
enum class KeyType {
    /** 일반 키 (숫자, 문자) */
    NORMAL,

    /** 백스페이스 (삭제) */
    BACKSPACE,

    /** 완료 버튼 */
    COMPLETE,

    /** 스페이스 */
    SPACE,

    /** 언어 전환 (한/영) */
    SWITCH
}