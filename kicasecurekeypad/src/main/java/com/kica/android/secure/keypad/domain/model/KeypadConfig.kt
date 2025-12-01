package com.kica.android.secure.keypad.domain.model

/**
 * 키패드 설정
 */
data class KeypadConfig(
    /** 키패드 타입 */
    val type: KeypadType = KeypadType.NUMERIC,

    /** 색상 테마 */
    val colors: KeypadColors = KeypadColors.default(),

    /** 마스킹 문자 */
    val maskingChar: Char = '●',

    /** 최대 입력 길이 (null이면 무제한) */
    val maxLength: Int? = null,

    /** 키패드 레이아웃 랜덤화 */
    val randomizeLayout: Boolean = false,

    /** 자동 초기화 시간 (ms, null이면 비활성화) */
    val autoResetTimeout: Long? = null,

    /** 진동 피드백 활성화 */
    val enableHapticFeedback: Boolean = true,

    /** 사운드 피드백 활성화 */
    val enableSoundFeedback: Boolean = false,

    /** 접근성 활성화 */
    val enableAccessibility: Boolean = true
)