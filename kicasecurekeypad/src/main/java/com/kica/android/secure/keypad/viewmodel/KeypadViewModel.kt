package com.kica.android.secure.keypad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 키패드 ViewModel
 *
 * 입력 상태 관리 및 비즈니스 로직 처리
 */
class KeypadViewModel(
    private val config: KeypadConfig
) : ViewModel() {

    // 실제 입력값 (평문) - 내부에서만 사용
    private val inputBuffer = mutableListOf<Char>()

    // 마스킹된 입력값 (UI에 표시)
    private val _maskedInput = MutableStateFlow("")
    val maskedInput: StateFlow<String> = _maskedInput.asStateFlow()

    // 진동 피드백 트리거
    private val _shouldVibrate = MutableStateFlow(false)
    val shouldVibrate: StateFlow<Boolean> = _shouldVibrate.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 키 입력 처리
     *
     * @param key 입력된 키
     */
    fun handleKeyPress(key: Key) {
        viewModelScope.launch {
            when (key.type) {
                KeyType.NORMAL -> {
                    // 최대 길이 체크
                    val maxLength = config.maxLength
                    if (maxLength != null && inputBuffer.size >= maxLength) {
                        _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                        triggerErrorFeedback()
                        return@launch
                    }

                    // 입력 추가
                    inputBuffer.add(key.value[0])
                    updateMaskedDisplay()

                    // 진동 피드백
                    if (config.enableHapticFeedback) {
                        triggerVibration()
                    }
                }

                KeyType.BACKSPACE -> {
                    handleBackspace()
                }

                KeyType.COMPLETE -> {
                    // 완료는 UI에서 처리
                }

                else -> {
                    // 다른 타입 (SPACE, SWITCH 등)은 추후 구현
                }
            }
        }
    }

    /**
     * 백스페이스 처리
     */
    fun handleBackspace() {
        viewModelScope.launch {
            if (inputBuffer.isNotEmpty()) {
                inputBuffer.removeLast()
                updateMaskedDisplay()

                // 진동 피드백
                if (config.enableHapticFeedback) {
                    triggerVibration()
                }
            }
        }
    }

    /**
     * 마스킹된 표시 업데이트
     */
    private fun updateMaskedDisplay() {
        _maskedInput.value = config.maskingChar.toString().repeat(inputBuffer.size)
    }

    /**
     * 입력값 가져오기 (평문)
     *
     * @return 입력된 문자열
     */
    fun getInputValue(): String {
        return inputBuffer.joinToString("")
    }

    /**
     * 입력 초기화
     */
    fun clearInput() {
        viewModelScope.launch {
            inputBuffer.clear()
            updateMaskedDisplay()
            _errorMessage.value = null
        }
    }

    /**
     * 진동 피드백 트리거
     */
    private fun triggerVibration() {
        _shouldVibrate.value = true
    }

    /**
     * 진동 완료 처리
     */
    fun onVibrationHandled() {
        _shouldVibrate.value = false
    }

    /**
     * 에러 피드백 트리거
     */
    private fun triggerErrorFeedback() {
        // 에러 시 다른 진동 패턴 (추후 구현)
        _shouldVibrate.value = true
    }

    /**
     * 에러 메시지 소비
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * ViewModel 정리
     */
    override fun onCleared() {
        super.onCleared()
        // 메모리 제로화 (추후 암호화 구현 시 중요)
        inputBuffer.clear()
    }
}