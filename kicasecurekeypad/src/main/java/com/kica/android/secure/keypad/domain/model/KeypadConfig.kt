package com.kica.android.secure.keypad.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 키패드 설정
 */
data class KeypadConfig(
    /** 키패드 타입 */
    val type: KeypadType = KeypadType.NUMERIC,

    /** 색상 테마 */
    val colors: KeypadColors = KeypadColors.default(),

    /** 상단 제목 (예: "비밀번호 입력") */
    val title: String? = null,

    /** 상단 부제목 (예: "숫자 6자리를 입력해주세요") */
    val subtitle: String? = null,

    /** 취소 버튼 표시 여부 */
    val showCancelButton: Boolean = false,

    /** 취소 버튼 텍스트 */
    val cancelButtonText: String = "취소",

    /** 마스킹 문자 */
    val maskingChar: Char = '●',

    /** 마스킹 표시 여부 (false면 실제 입력 표시) */
    val showMasking: Boolean = true,

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
    val enableAccessibility: Boolean = true,

    /** 버튼 높이 (앱단에서 조정 가능) */
    val buttonHeight: Dp = 48.dp,

    /** 암호화 활성화 (true면 KeyDataManager 사용) */
    val enableEncryption: Boolean = false
)