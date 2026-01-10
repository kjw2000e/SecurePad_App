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

    /** 마스킹 문자 (채워진 상태) */
    val maskingChar: Char = '●',

    /** 미입력 마스킹 문자 (비어있는 상태) */
    val emptyMaskingChar: Char = '○',

    /** 입력 상태 표시 스타일 */
    val inputIndicatorStyle: InputIndicatorStyle = InputIndicatorStyle.DOT,

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
    val enableEncryption: Boolean = false,

    /**
     * 화면 캡처 방지 (FLAG_SECURE)
     *
     * true로 설정하면 키패드가 표시되는 동안 스크린샷, 화면 녹화가 차단됩니다.
     * 키패드가 닫히면 자동으로 해제됩니다.
     */
    val preventScreenCapture: Boolean = true
)