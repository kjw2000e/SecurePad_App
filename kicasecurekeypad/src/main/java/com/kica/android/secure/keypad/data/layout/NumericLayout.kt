package com.kica.android.secure.keypad.data.layout

import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType

/**
 * 숫자 키패드 레이아웃
 *
 * 3x4 그리드:
 * ┌─────┬─────┬─────┐
 * │  1  │  2  │  3  │
 * ├─────┼─────┼─────┤
 * │  4  │  5  │  6  │
 * ├─────┼─────┼─────┤
 * │  7  │  8  │  9  │
 * ├─────┼─────┼─────┤
 * │  🔀 │  0  │  ⌫  │
 * └─────┴─────┴─────┘
 */
object NumericLayout {

    /**
     * 숫자 키패드 키 목록 가져오기
     *
     * @param shuffledNumbers 섞인 숫자 목록 (null이면 기본 순서)
     * @return 키 목록 (3x4 = 12개)
     */
    fun getKeys(shuffledNumbers: List<Int>? = null): List<Key> {
        val numberKeys = if (shuffledNumbers != null) {
            // 전달받은 순서로 배치 (0-9 모두 섞임)
            shuffledNumbers.map { num ->
                Key(
                    value = num.toString(),
                    displayText = num.toString(),
                    type = KeyType.NORMAL
                )
            }
        } else {
            // 기본 순서 (1-9, 0)
            listOf(
                Key("1", "1", KeyType.NORMAL),
                Key("2", "2", KeyType.NORMAL),
                Key("3", "3", KeyType.NORMAL),
                Key("4", "4", KeyType.NORMAL),
                Key("5", "5", KeyType.NORMAL),
                Key("6", "6", KeyType.NORMAL),
                Key("7", "7", KeyType.NORMAL),
                Key("8", "8", KeyType.NORMAL),
                Key("9", "9", KeyType.NORMAL),
                Key("0", "0", KeyType.NORMAL)
            )
        }

        // 0-9 모두 배치 (처음 9개는 1~3행에 배치됨)
        val result = numberKeys.take(9).toMutableList()

        // 마지막 줄: 재배열, 마지막 숫자(10번째), 백스페이스₩
        result.add(
            Key(
                value = "",
                displayText = "🔀",
                type = KeyType.SHUFFLE
            )
        )
        result.add(numberKeys[9])  // 10번째 숫자 (재배열 시 바뀜)
        result.add(
            Key(
                value = "",
                displayText = "⌫",
                type = KeyType.BACKSPACE
            )
        )

        return result
    }
}
