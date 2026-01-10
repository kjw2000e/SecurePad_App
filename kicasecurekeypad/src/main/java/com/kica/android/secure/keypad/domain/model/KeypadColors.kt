package com.kica.android.secure.keypad.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 키패드 색상 설정
 */
data class KeypadColors(
    val backgroundColor: Color,
    val keyBackgroundColor: Color,
    val keyTextColor: Color,
    val specialKeyBackgroundColor: Color,
    val specialKeyTextColor: Color,
    val inputDisplayBackgroundColor: Color,
    val inputDisplayTextColor: Color,
    /** 제목 텍스트 색상 */
    val titleColor: Color = Color(0xFF191F28),
    /** 부제목 텍스트 색상 */
    val subtitleColor: Color = Color(0xFF6B7684),
    /** 취소 버튼 텍스트 색상 */
    val cancelButtonColor: Color = Color(0xFF6B7684)
) {
    companion object {
        /**
         * 기본 라이트 테마
         */
        fun default() = KeypadColors(
            backgroundColor = Color(0xff000000),
            keyBackgroundColor = Color.White,
            keyTextColor = Color(0xFF212121),
            specialKeyBackgroundColor = Color(0xFFABB0BC),
            specialKeyTextColor = Color(0xFF212121),
            inputDisplayBackgroundColor = Color(0xD2D3D8),
            inputDisplayTextColor = Color(0xFF212121),
            titleColor = Color.White,
            subtitleColor = Color(0xFFB0B8C1),
            cancelButtonColor = Color(0xFFB0B8C1)
        )

        /**
         * 다크 테마
         */
        fun dark() = KeypadColors(
            backgroundColor = Color(0xFF121212),
            keyBackgroundColor = Color(0xFF1E1E1E),
            keyTextColor = Color(0xFFE0E0E0),
            specialKeyBackgroundColor = Color(0xFF2196F3),
            specialKeyTextColor = Color.White,
            inputDisplayBackgroundColor = Color(0xFF1E1E1E),
            inputDisplayTextColor = Color(0xFFE0E0E0),
            titleColor = Color(0xFFE0E0E0),
            subtitleColor = Color(0xFF9E9E9E),
            cancelButtonColor = Color(0xFF9E9E9E)
        )

        /**
         * KICA 스타일 테마
         * - 심플하고 모던한 느낌
         * - 밝은 배경에 흰색 버튼
         * - 미니멀한 디자인
         */
        fun kica() = KeypadColors(
            backgroundColor = Color(0xFFF9FAFB),        // 밝은 회색 배경
            keyBackgroundColor = Color(0xFFFFFFFF),     // 흰색 버튼
            keyTextColor = Color(0xFF191F28),           // 거의 검정 텍스트
            specialKeyBackgroundColor = Color(0xFFF2F4F6), // 밝은 회색 (특수 키)
            specialKeyTextColor = Color(0xFF191F28),    // 거의 검정 (특수 키)
            inputDisplayBackgroundColor = Color(0xFFFFFFFF), // 흰색 카드
            inputDisplayTextColor = Color(0xFF191F28),  // 거의 검정
            titleColor = Color(0xFF191F28),
            subtitleColor = Color(0xFF6B7684),
            cancelButtonColor = Color(0xFF6B7684)
        )

        /**
         * 연보라 스타일 테마 (사용자 이미지 기반)
         */
        fun lavender() = KeypadColors(
            backgroundColor = Color(0xFFF4F0F6),        // 연보라 배경
            keyBackgroundColor = Color(0xFFFFFFFF),     // 흰색 버튼
            keyTextColor = Color(0xFF191F28),           // 검정 텍스트
            specialKeyBackgroundColor = Color(0xFFB0B8C1), // 진한 회색 (특수 키: Shift, Backspace 등)
            specialKeyTextColor = Color(0xFF191F28),    // 검정 (특수 키)
            inputDisplayBackgroundColor = Color(0xFFFFFFFF),
            inputDisplayTextColor = Color(0xFF191F28),
            titleColor = Color(0xFF191F28),
            subtitleColor = Color(0xFF6B7684),
            cancelButtonColor = Color(0xFF6B7684)
        )
    }
}
