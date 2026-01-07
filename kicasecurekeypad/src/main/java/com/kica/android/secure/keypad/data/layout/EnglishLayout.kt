package com.kica.android.secure.keypad.data.layout

import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType

/**
 * 영문 QWERTY 키패드 레이아웃
 *
 * 모바일 최적화 3줄 레이아웃:
 * ┌─────────────────────────────┐
 * │ Q W E R T Y U I O P         │
 * │  A S D F G H J K L          │
 * │ ⇧ Z X C V B N M ⌫           │
 * │ 한/영    Space    ✓          │
 * └─────────────────────────────┘
 */
object EnglishLayout {

    /**
     * 영문 키패드 키 목록 가져오기
     *
     * @param uppercase 대문자 여부
     * @param randomize 레이아웃 랜덤화 여부 (영문은 QWERTY 고정이므로 무시)
     * @return 키 목록
     */
    fun getKeys(uppercase: Boolean = false, randomize: Boolean = false): List<Key> {
        val letters = if (uppercase) {
            listOf(
                "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
                "A", "S", "D", "F", "G", "H", "J", "K", "L",
                "Z", "X", "C", "V", "B", "N", "M"
            )
        } else {
            listOf(
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                "a", "s", "d", "f", "g", "h", "j", "k", "l",
                "z", "x", "c", "v", "b", "n", "m"
            )
        }

        val keys = mutableListOf<Key>()

        // 첫 번째 줄: 숫자 키 (1 ~ 0) - [NEW]
        val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        numbers.forEach { num ->
             keys.add(Key(num, num, KeyType.NORMAL))
        }

        // 두 번째 줄(기존 첫 줄): Q W E R T Y U I O P (10개)
        letters.subList(0, 10).forEach { letter ->
            keys.add(Key(letter, letter, KeyType.NORMAL))
        }

        // 세 번째 줄: A S D F G H J K L (9개)
        letters.subList(10, 19).forEach { letter ->
            keys.add(Key(letter, letter, KeyType.NORMAL))
        }

        // 네 번째 줄: Shift, Z X C V B N M, Backspace (9개)
        keys.add(
            Key(
                value = "SHIFT",
                displayText = "⇧",
                type = KeyType.SHIFT
            )
        )
        letters.subList(19, 26).forEach { letter ->
            keys.add(Key(letter, letter, KeyType.NORMAL))
        }
        keys.add(
            Key(
                value = "",
                displayText = "⌫",
                type = KeyType.BACKSPACE
            )
        )

        // 다섯 번째 줄: 특수문자, 한/영, Space, 완료 (4개) - [NEW]
        keys.add(
            Key(
                value = "SPECIAL_TOGGLE",
                displayText = "!#1", // 특수문자 토글 아이콘 대체 텍스트
                type = KeyType.SPECIAL_TOGGLE
            )
        )
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
     * 1열: 10 (숫자)
     * 2열: 10 (QWERTY)
     * 3열: 9 (ASDF)
     * 4열: 9 (ZXCV)
     * 5열: 4 (Special, En/Ko, Space, Done)
     */
    val rowSizes = listOf(10, 10, 9, 9, 4)
}
