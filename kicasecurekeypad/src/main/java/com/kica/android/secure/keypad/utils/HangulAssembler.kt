package com.kica.android.secure.keypad.utils

/**
 * 한글 조립기
 *
 * 자모를 입력받아 실시간으로 한글을 조립합니다.
 */
class HangulAssembler {

    // 현재 조립 중인 한글
    private var currentChosung: Char? = null
    private var currentJungsung: Char? = null
    private var currentJongsung: Char? = null

    /**
     * 문자 입력 처리
     *
     * @param ch 입력된 문자
     * @return 완성된 문자열 (조립 중인 글자 포함)
     */
    fun append(ch: Char): String {
        return when {
            // 한글 자음 입력
            HangulComposer.isChosung(ch) -> appendChosung(ch)
            // 한글 모음 입력
            HangulComposer.isJungsung(ch) -> appendJungsung(ch)
            // 기타 문자 (영문, 숫자, 공백 등)
            else -> {
                val result = commit()
                result + ch
            }
        }
    }

    /**
     * 자음 입력 처리
     */
    private fun appendChosung(ch: Char): String {
        return when {
            // 조립 중인 글자가 없으면 초성으로 시작
            currentChosung == null -> {
                currentChosung = ch
                getCurrentChar().toString()
            }

            // 중성이 없으면 이전 자음은 단독 자음, 새로운 글자 시작
            currentJungsung == null -> {
                val prev = currentChosung!!
                currentChosung = ch
                currentJungsung = null
                currentJongsung = null
                prev.toString() + getCurrentChar()
            }

            // 중성이 있고 종성이 없으면 종성으로 추가
            currentJongsung == null -> {
                currentJongsung = ch
                getCurrentChar().toString()
            }

            // 종성이 이미 있으면 복합 종성 시도
            else -> {
                val combined = HangulComposer.combineJongsung(currentJongsung!!, ch)
                if (combined != null) {
                    // 복합 종성 성공
                    currentJongsung = combined
                    getCurrentChar().toString()
                } else {
                    // 복합 종성 실패 → 현재 글자 완성하고 새 글자 시작
                    val completed = commit()
                    currentChosung = ch
                    completed + getCurrentChar()
                }
            }
        }
    }

    /**
     * 모음 입력 처리
     */
    private fun appendJungsung(ch: Char): String {
        return when {
            // 조립 중인 글자가 없으면 단독 모음
            currentChosung == null -> {
                ch.toString()
            }

            // 초성만 있으면 중성으로 추가
            currentJungsung == null -> {
                currentJungsung = ch
                getCurrentChar().toString()
            }

            // 중성이 있고 종성이 없으면 복합 모음 시도
            currentJongsung == null -> {
                val combined = HangulComposer.combineJungsung(currentJungsung!!, ch)
                if (combined != null) {
                    // 복합 모음 성공
                    currentJungsung = combined
                    getCurrentChar().toString()
                } else {
                    // 복합 모음 실패 → 현재 글자 완성하고 모음 추가
                    val completed = commit()
                    completed + ch
                }
            }

            // 종성이 있으면 종성을 떼서 새 글자의 초성으로
            else -> {
                val jong = currentJongsung!!
                currentJongsung = null
                val completed = commit()

                // 종성을 초성으로 사용
                currentChosung = jong
                currentJungsung = ch
                completed + getCurrentChar()
            }
        }
    }

    /**
     * 현재 조립 중인 문자 반환
     */
    private fun getCurrentChar(): String {
        return when {
            currentChosung == null -> ""
            currentJungsung == null -> currentChosung.toString()
            else -> {
                val composed = HangulComposer.compose(currentChosung, currentJungsung, currentJongsung)
                composed?.toString() ?: (currentChosung.toString() + currentJungsung.toString())
            }
        }
    }

    /**
     * 현재 조립 중인 글자 완성
     */
    fun commit(): String {
        val result = getCurrentChar()
        clear()
        return result
    }

    /**
     * 조립 상태 초기화
     */
    fun clear() {
        currentChosung = null
        currentJungsung = null
        currentJongsung = null
    }

    /**
     * 마지막 문자 삭제 처리
     *
     * @return true면 조립 중인 글자 수정, false면 이전 글자 삭제 필요
     */
    fun backspace(): Pair<Boolean, String> {
        return when {
            // 종성 삭제
            currentJongsung != null -> {
                currentJongsung = null
                Pair(true, getCurrentChar())
            }

            // 중성 삭제
            currentJungsung != null -> {
                currentJungsung = null
                Pair(true, getCurrentChar())
            }

            // 초성 삭제
            currentChosung != null -> {
                currentChosung = null
                Pair(true, "")
            }

            // 조립 중인 글자 없음
            else -> {
                Pair(false, "")
            }
        }
    }

    /**
     * 조립 중인 글자가 있는지 확인
     */
    fun isComposing(): Boolean {
        return currentChosung != null
    }
}