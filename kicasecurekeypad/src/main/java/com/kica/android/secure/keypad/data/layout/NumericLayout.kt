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
 * │  ⌫  │  0  │  ✓  │
 * └─────┴─────┴─────┘
 */
object NumericLayout {

    /**
     * 숫자 키패드 키 목록 가져오기
     *
     * @param randomize 레이아웃 랜덤화 여부
     * @return 키 목록 (3x4 = 12개)
     */
    fun getKeys(randomize: Boolean = false): List<Key> {
        val numberKeys = if (randomize) {
            // 0-9 숫자 랜덤화
            (0..9).shuffled().map { num ->
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

        // 1-9 배치
        val result = numberKeys.filter { it.value != "0" }.toMutableList()

        // 마지막 줄: 백스페이스, 0, 완료
        result.add(
            Key(
                value = "",
                displayText = "⌫",
                type = KeyType.BACKSPACE
            )
        )
        result.add(numberKeys.first { it.value == "0" })
        result.add(
            Key(
                value = "",
                displayText = "✓",
                type = KeyType.COMPLETE
            )
        )

        return result
    }
}