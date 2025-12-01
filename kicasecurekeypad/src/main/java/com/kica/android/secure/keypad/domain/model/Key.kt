package com.kica.android.secure.keypad.domain.model

/**
 * 키 데이터 모델
 *
 * @property value 실제 입력값 (숫자, 문자 등)
 * @property displayText 화면에 표시될 텍스트
 * @property type 키 타입
 */
data class Key(
    val value: String,
    val displayText: String,
    val type: KeyType = KeyType.NORMAL
)