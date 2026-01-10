package com.kica.android.secure.keypad.domain.model

/**
 * 입력 상태 표시 스타일
 */
enum class InputIndicatorStyle {
    /**
     * 점(DOT) 스타일
     * 입력된 위치: ● (채워진 원)
     * 미입력 위치: ○ (빈 원)
     */
    DOT,

    /**
     * 밑줄(UNDERLINE) 스타일
     * 입력된 위치: ─ (굵은 밑줄)
     * 미입력 위치: _ (얇은 밑줄)
     */
    UNDERLINE,

    /**
     * 박스(BOX) 스타일
     * 입력된 위치: ■ (채워진 사각형)
     * 미입력 위치: □ (빈 사각형)
     */
    BOX,

    /**
     * 텍스트 스타일 (기존 방식)
     * 마스킹 문자만 표시 (개수 제한 없음)
     */
    TEXT
}
