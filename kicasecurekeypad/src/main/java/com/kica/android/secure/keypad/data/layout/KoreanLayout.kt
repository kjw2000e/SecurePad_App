package com.kica.android.secure.keypad.data.layout

import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType

/**
 * 한글 2벌식 키패드 레이아웃
 *
 * 모바일 최적화 3줄 레이아웃:
 * ┌─────────────────────────────┐
 * │ ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ   │
 * │  ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ     │
 * │ ⇧ ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ ⌫       │
 * │ 한/영    Space    ✓          │
 * └─────────────────────────────┘
 */
object KoreanLayout {

    /**
     * 한글 키패드 키 목록 가져오기
     *
     * @param shifted Shift 상태 여부 (쌍자음 표시)
     * @param randomize 레이아웃 랜덤화 여부 (한글은 2벌식 고정이므로 무시)
     * @return 키 목록
     */
    fun getKeys(shifted: Boolean = false, randomize: Boolean = false): List<Key> {
        // 기본 자모음 (Shift 미적용)
        val basicChars = listOf(
            // 첫 번째 줄 (10개)
            "ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ",
            // 두 번째 줄 (9개)
            "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ",
            // 세 번째 줄 (7개 - Shift, Backspace 제외)
            "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ"
        )

        // 쌍자음 및 복합 모음 (Shift 적용 시)
        val shiftedChars = listOf(
            // 첫 번째 줄 (10개)
            "ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅛ", "ㅕ", "ㅑ", "ㅒ", "ㅖ",
            // 두 번째 줄 (9개)
            "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ",
            // 세 번째 줄 (7개)
            "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ"
        )

        val chars = if (shifted) shiftedChars else basicChars
        val keys = mutableListOf<Key>()

        // 첫 번째 줄: ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ (10개)
        chars.subList(0, 10).forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }

        // 두 번째 줄: ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ (9개)
        chars.subList(10, 19).forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }

        // 세 번째 줄: Shift, ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ, Backspace (9개)
        keys.add(
            Key(
                value = "SHIFT",
                displayText = "⇧",
                type = KeyType.SHIFT
            )
        )
        chars.subList(19, 26).forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }
        keys.add(
            Key(
                value = "",
                displayText = "⌫",
                type = KeyType.BACKSPACE
            )
        )

        // 네 번째 줄: 한/영, Space, 완료 (3개)
        keys.add(
            Key(
                value = "SWITCH",
                displayText = "한/영",
                type = KeyType.SWITCH
            )
        )
        keys.add(
            Key(
                value = " ",
                displayText = "Space",
                type = KeyType.SPACE
            )
        )
        keys.add(
            Key(
                value = "",
                displayText = "✓",
                type = KeyType.COMPLETE
            )
        )

        return keys
    }

    /**
     * 각 행의 키 개수
     * UI에서 GridCells.Fixed() 사용 시 참고
     */
    val rowSizes = listOf(10, 9, 9, 3)
}
