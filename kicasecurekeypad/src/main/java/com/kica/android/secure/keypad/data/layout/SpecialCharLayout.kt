package com.kica.android.secure.keypad.data.layout

import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType

/**
 * 특수문자 키패드 레이아웃
 * 
 * 3줄의 특수문자 + 하단 기능키
 */
object SpecialCharLayout {

    /**
     * 특수문자 키 목록 가져오기
     */
    fun getKeys(): List<Key> {
        val keys = mutableListOf<Key>()

        // 첫 번째 줄 (10개): ! @ # $ % ^ & * ( )
        val row1 = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
        row1.forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }

        // 두 번째 줄 (10개): - _ = + [ ] { } \ |
        val row2 = listOf("-", "_", "=", "+", "[", "]", "{", "}", "\\", "|")
        row2.forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }

        // 세 번째 줄 (9개): ` ~ ; : ' " , . ?
        val row3 = listOf("`", "~", ";", ":", "'", "\"", ",", ".", "?")
        row3.forEach { char ->
            keys.add(Key(char, char, KeyType.NORMAL))
        }

        // 네 번째 줄 (9개): Shift(Empty) + / < > + Empties + Backspace
        // EnglishLayout 구조: [Shift] [7개 키] [Backspace]
        // 여기서는: [Empty] [ / < > ] [Empty...] [Backspace]
        
        // 1. Shift 자리 Empty
        keys.add(Key("", "", KeyType.EMPTY))
        
        // 2. 문자들 (/ < >)
        val row4Chars = listOf("/", "<", ">")
        row4Chars.forEach { char ->
             keys.add(Key(char, char, KeyType.NORMAL))
        }
        
        // 3. 나머지 빈 공간 채우기 (EnglishLayout은 7개 문자 사용. 우리는 3개 사용. 4개 남음)
        repeat(4) {
            keys.add(Key("", "", KeyType.EMPTY))
        }

        // 4. Backspace
        keys.add(
            Key(
                value = "",
                displayText = "⌫",
                type = KeyType.BACKSPACE
            )
        )

        // 다섯 번째 줄 (4개): [abc] [Empty] [Space] [Complete]
        // EnglishLayout: [Special] [En/Ko] [Space] [Complete]
        
        // 1. 돌아가기 (abc) -> Special Toggle 위치
        keys.add(
            Key(
                value = "SPECIAL_TOGGLE", 
                displayText = "abc", 
                type = KeyType.SPECIAL_TOGGLE
            )
        )
        
        // 2. 한/영 키 위치 -> Empty
        keys.add(Key("", "", KeyType.EMPTY))
        
        // 3. Space
        keys.add(
            Key(
                value = " ",
                displayText = "Space",
                type = KeyType.SPACE
            )
        )
        
        // 4. Complete
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
     * EnglishLayout/KoreanLayout 과 동일한 구조 사용 (높이 일치)
     */
    val rowSizes = listOf(10, 10, 9, 9, 4)
}
