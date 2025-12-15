package com.kica.android.secure.keypad.utils

/**
 * 한글 자모 조합 유틸리티
 *
 * 자음과 모음을 조합하여 완성형 한글을 생성합니다.
 */
object HangulComposer {

    // 초성 (19개)
    private val CHOSUNG = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // 중성 (21개)
    private val JUNGSUNG = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    // 종성 (28개, 첫번째는 종성 없음)
    private val JONGSUNG = listOf(
        '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // 복합 모음 조합 규칙
    private val JUNGSUNG_COMBINE = mapOf(
        "ㅗㅏ" to 'ㅘ', "ㅗㅐ" to 'ㅙ', "ㅗㅣ" to 'ㅚ',
        "ㅜㅓ" to 'ㅝ', "ㅜㅔ" to 'ㅞ', "ㅜㅣ" to 'ㅟ',
        "ㅡㅣ" to 'ㅢ'
    )

    // 복합 종성 조합 규칙
    private val JONGSUNG_COMBINE = mapOf(
        "ㄱㅅ" to 'ㄳ', "ㄴㅈ" to 'ㄵ', "ㄴㅎ" to 'ㄶ',
        "ㄹㄱ" to 'ㄺ', "ㄹㅁ" to 'ㄻ', "ㄹㅂ" to 'ㄼ', "ㄹㅅ" to 'ㄽ',
        "ㄹㅌ" to 'ㄾ', "ㄹㅍ" to 'ㄿ', "ㄹㅎ" to 'ㅀ',
        "ㅂㅅ" to 'ㅄ'
    )

    /**
     * 자음인지 확인
     */
    fun isChosung(ch: Char): Boolean = ch in CHOSUNG

    /**
     * 모음인지 확인
     */
    fun isJungsung(ch: Char): Boolean = ch in JUNGSUNG

    /**
     * 종성 가능 자음인지 확인
     */
    fun isJongsung(ch: Char): Boolean = ch in JONGSUNG && ch != '\u0000'

    /**
     * 완성형 한글인지 확인
     */
    fun isHangul(ch: Char): Boolean = ch in '가'..'힣'

    /**
     * 완성형 한글을 초성/중성/종성으로 분해
     */
    fun decompose(hangul: Char): Triple<Char?, Char?, Char?> {
        if (!isHangul(hangul)) return Triple(null, null, null)

        val code = hangul.code - 0xAC00
        val chosungIndex = code / 588
        val jungsungIndex = (code % 588) / 28
        val jongsungIndex = code % 28

        return Triple(
            CHOSUNG[chosungIndex],
            JUNGSUNG[jungsungIndex],
            if (jongsungIndex > 0) JONGSUNG[jongsungIndex] else null
        )
    }

    /**
     * 초성/중성/종성을 조합하여 완성형 한글 생성
     */
    fun compose(chosung: Char?, jungsung: Char?, jongsung: Char? = null): Char? {
        if (chosung == null || jungsung == null) return null
        if (!isChosung(chosung) || !isJungsung(jungsung)) return null

        val chosungIndex = CHOSUNG.indexOf(chosung)
        val jungsungIndex = JUNGSUNG.indexOf(jungsung)
        val jongsungIndex = if (jongsung != null && isJongsung(jongsung)) {
            JONGSUNG.indexOf(jongsung)
        } else {
            0
        }

        if (chosungIndex < 0 || jungsungIndex < 0 || jongsungIndex < 0) return null

        val code = 0xAC00 + (chosungIndex * 588) + (jungsungIndex * 28) + jongsungIndex
        return code.toChar()
    }

    /**
     * 두 모음을 조합 (예: ㅗ + ㅏ = ㅘ)
     */
    fun combineJungsung(first: Char, second: Char): Char? {
        return JUNGSUNG_COMBINE["$first$second"]
    }

    /**
     * 두 자음을 조합하여 복합 종성 생성 (예: ㄱ + ㅅ = ㄳ)
     */
    fun combineJongsung(first: Char, second: Char): Char? {
        return JONGSUNG_COMBINE["$first$second"]
    }
}