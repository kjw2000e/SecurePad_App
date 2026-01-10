package com.kica.android.secure.keypad.domain.model

/**
 * 키패드 표시 모드
 */
enum class KeypadDisplayMode {
    /**
     * 전체 화면 모드
     * 화면 전체를 차지하며, 헤더/입력창/키패드가 세로로 배치됨
     */
    FULL,

    /**
     * 하프 화면 모드 (BottomSheet 스타일)
     * 화면 하단 절반 정도를 차지
     */
    HALF,

    /**
     * 컴팩트 모드
     * 최소 높이로 키패드만 표시 (인라인 사용)
     */
    COMPACT
}
