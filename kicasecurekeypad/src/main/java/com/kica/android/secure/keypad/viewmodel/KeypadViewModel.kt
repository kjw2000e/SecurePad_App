package com.kica.android.secure.keypad.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kica.android.secure.keypad.data.layout.EnglishLayout
import com.kica.android.secure.keypad.data.layout.KoreanLayout
import com.kica.android.secure.keypad.data.layout.NumericLayout
import com.kica.android.secure.keypad.data.layout.SpecialCharLayout
import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadType
import com.kica.android.secure.keypad.security.KeyDataManager
import com.kica.android.secure.keypad.utils.HangulAssembler
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
    private val context: Context,
    initialConfig: KeypadConfig
) : ViewModel() {

    // Config (변경 가능)
    private var config: KeypadConfig = initialConfig

    // KeyDataManager (암호화 활성화 시 사용)
    private val keyDataManager: KeyDataManager? = if (config.enableEncryption) {
        KeyDataManager.getInstance(context)
    } else {
        null
    }

    // 실제 입력값 (평문) - 내부에서만 사용 (암호화 비활성화 시)
    private val inputBuffer = StringBuilder()

    // 한글 조립기
    private val hangulAssembler = HangulAssembler()

    // 마스킹된 입력값 (UI에 표시)
    private val _maskedInput = MutableStateFlow("")
    val maskedInput: StateFlow<String> = _maskedInput.asStateFlow()

    // 현재 언어 (ENGLISH/KOREAN 전환용)
    private val _currentLanguage = MutableStateFlow(
        when (config.type) {
            KeypadType.ENGLISH -> KeypadType.ENGLISH
            KeypadType.KOREAN -> KeypadType.KOREAN
            else -> KeypadType.ENGLISH
        }
    )
    val currentLanguage: StateFlow<KeypadType> = _currentLanguage.asStateFlow()

    // Shift 상태 (대문자/쌍자음)
    private val _isShifted = MutableStateFlow(false)
    val isShifted: StateFlow<Boolean> = _isShifted.asStateFlow()

    // 특수문자 모드 상태
    private val _isSpecialCharMode = MutableStateFlow(false)
    val isSpecialCharMode: StateFlow<Boolean> = _isSpecialCharMode.asStateFlow()


    // 키 목록
    private val _keys = MutableStateFlow<List<Key>>(emptyList())
    val keys: StateFlow<List<Key>> = _keys.asStateFlow()

    // 진동 피드백 트리거
    private val _shouldVibrate = MutableStateFlow(false)
    val shouldVibrate: StateFlow<Boolean> = _shouldVibrate.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 숫자 섞기 상태 (NUMERIC 타입용)

    private var shuffledNumbers: List<Int>? = null

    init {
        // KeyDataManager 초기화 (암호화 활성화 시)
        if (config.enableEncryption) {
            try {
                keyDataManager?.initialize()
            } catch (e: Exception) {
                _errorMessage.value = "암호화 초기화 실패: ${e.message}"
            }
        }

        // 초기 키 목록 로드
        loadKeys()
    }

    /**
     * 키 입력 처리
     *
     * @param key 입력된 키
     */
    fun handleKeyPress(key: Key) {
        viewModelScope.launch {
            when (key.type) {
                KeyType.NORMAL -> {
                    val char = key.value[0]

                    // 한글인 경우 조립 처리
                    val isKorean = config.type == KeypadType.KOREAN ||
                            (config.type == KeypadType.ALPHANUMERIC && _currentLanguage.value == KeypadType.KOREAN)

                    if (config.enableEncryption) {
                        // 암호화 모드
                        handleNormalKeyEncrypted(char, isKorean)
                    } else {
                        // 평문 모드 (기존 로직)
                        handleNormalKeyPlaintext(char, isKorean)
                    }

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

                KeyType.SPACE -> {
                    if (config.enableEncryption) {
                        handleSpaceEncrypted()
                    } else {
                        handleSpacePlaintext()
                    }

                    updateMaskedDisplay()

                    // 진동 피드백
                    if (config.enableHapticFeedback) {
                        triggerVibration()
                    }
                }

                KeyType.SWITCH -> {
                    handleLanguageSwitch()
                }

                KeyType.SHIFT -> {
                    handleShift()
                }

                KeyType.SHUFFLE -> {
                    handleShuffle()
                }

                KeyType.SPECIAL_TOGGLE -> {
                    handleSpecialToggle()
                }

                else -> {
                    // 처리되지 않은 타입
                }
            }
        }
    }

    /**
     * NORMAL 키 입력 처리 (평문 모드)
     */
    private fun handleNormalKeyPlaintext(char: Char, isKorean: Boolean) {
        if (isKorean) {
            // append() 호출 전에 조립 상태 저장
            val wasComposing = hangulAssembler.isComposing()

            // 한글 조립
            val assembled = hangulAssembler.append(char)

            // 최대 길이 체크
            val expectedLength = if (wasComposing) {
                inputBuffer.length - 1 + assembled.length
            } else {
                inputBuffer.length + assembled.length
            }

            val maxLength = config.maxLength
            if (maxLength != null && expectedLength > maxLength) {
                _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                triggerErrorFeedback()
                return
            }

            // 조립된 문자열 처리
            if (assembled.isNotEmpty()) {
                if (wasComposing) {
                    // 이전에 조립 중이던 글자를 제거하고 새로운 결과로 대체
                    if (inputBuffer.isNotEmpty()) {
                        inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                    }
                }
                inputBuffer.append(assembled)
            }
        } else {
            // 한글이 아닌 경우 (영문, 숫자 등)
            val maxLength = config.maxLength
            if (maxLength != null && inputBuffer.length >= maxLength) {
                _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                triggerErrorFeedback()
                return
            }

            // 조립 중인 한글이 있으면 완성
            if (hangulAssembler.isComposing()) {
                val completed = hangulAssembler.commit()
                if (completed.isNotEmpty() && inputBuffer.isNotEmpty()) {
                    inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                    inputBuffer.append(completed)
                }
            }

            inputBuffer.append(char)
        }
    }

    /**
     * NORMAL 키 입력 처리 (암호화 모드)
     */
    private fun handleNormalKeyEncrypted(char: Char, isKorean: Boolean) {
        try {
            if (isKorean) {
                // 한글 조립 처리
                val wasComposing = hangulAssembler.isComposing()
                val assembled = hangulAssembler.append(char)

                // 최대 길이 체크 (KeyDataManager의 inputCount 기준)
                val currentCount = keyDataManager?.inputCount ?: 0
                val expectedCount = if (wasComposing) {
                    currentCount  // 조립 중이면 카운트는 그대로
                } else {
                    currentCount + 1  // 새 글자 시작이면 카운트 증가
                }

                val maxLength = config.maxLength
                if (maxLength != null && expectedCount > maxLength) {
                    _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                    triggerErrorFeedback()
                    return
                }

                // 조립된 문자열 처리
                if (assembled.isNotEmpty()) {
                    if (wasComposing) {
                        // 조립 중이던 글자 업데이트 (마지막 데이터 제거 후 재추가)
                        if (inputBuffer.isNotEmpty()) {
                            inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                        }
                        keyDataManager?.removeKeyData()
                    }
                    inputBuffer.append(assembled)
                    // 암호화하여 추가
                    keyDataManager?.appendKeyData(assembled.toByteArray(Charsets.UTF_8))
                }
            } else {
                // 한글이 아닌 경우 (영문, 숫자 등)
                val currentCount = keyDataManager?.inputCount ?: 0
                val maxLength = config.maxLength
                if (maxLength != null && currentCount >= maxLength) {
                    _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                    triggerErrorFeedback()
                    return
                }

                // 조립 중인 한글이 있으면 완성
                if (hangulAssembler.isComposing()) {
                    val completed = hangulAssembler.commit()
                    if (completed.isNotEmpty() && inputBuffer.isNotEmpty()) {
                        inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                        inputBuffer.append(completed)
                        // 마지막 한글 데이터 업데이트
                        keyDataManager?.removeKeyData()
                        keyDataManager?.appendKeyData(completed.toByteArray(Charsets.UTF_8))
                    }
                }

                inputBuffer.append(char)
                // 암호화하여 추가
                keyDataManager?.appendKeyData(char.toString().toByteArray(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            _errorMessage.value = "암호화 오류: ${e.message}"
        }
    }

    /**
     * 백스페이스 처리
     */
    fun handleBackspace() {
        viewModelScope.launch {
            if (config.enableEncryption) {
                handleBackspaceEncrypted()
            } else {
                handleBackspacePlaintext()
            }

            updateMaskedDisplay()

            // 진동 피드백
            if (config.enableHapticFeedback) {
                triggerVibration()
            }
        }
    }

    /**
     * 백스페이스 처리 (평문 모드)
     */
    private fun handleBackspacePlaintext() {
        if (inputBuffer.isEmpty()) return

        // 한글 조립 중이면 조립 상태 되돌리기
        val (handled, result) = hangulAssembler.backspace()

        if (handled) {
            // 조립 중인 글자 수정
            if (inputBuffer.isNotEmpty()) {
                inputBuffer.deleteCharAt(inputBuffer.lastIndex)
            }
            if (result.isNotEmpty()) {
                inputBuffer.append(result)
            }
        } else {
            // 이전 글자 삭제
            if (inputBuffer.isNotEmpty()) {
                inputBuffer.deleteCharAt(inputBuffer.lastIndex)
            }
        }
    }

    /**
     * 백스페이스 처리 (암호화 모드)
     */
    private fun handleBackspaceEncrypted() {
        if (inputBuffer.isEmpty()) return

        try {
            // 한글 조립 중이면 조립 상태 되돌리기
            val (handled, result) = hangulAssembler.backspace()

            if (handled) {
                // 조립 중인 글자 수정
                if (inputBuffer.isNotEmpty()) {
                    inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                }
                if (result.isNotEmpty()) {
                    inputBuffer.append(result)
                }
                // KeyDataManager 업데이트
                keyDataManager?.removeKeyData()
                if (result.isNotEmpty()) {
                    keyDataManager?.appendKeyData(result.toByteArray(Charsets.UTF_8))
                }
            } else {
                // 이전 글자 삭제
                if (inputBuffer.isNotEmpty()) {
                    inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                }
                keyDataManager?.removeKeyData()
            }
        } catch (e: Exception) {
            _errorMessage.value = "백스페이스 처리 오류: ${e.message}"
        }
    }

    /**
     * SPACE 키 처리 (평문 모드)
     */
    private fun handleSpacePlaintext() {
        // 최대 길이 체크
        val maxLength = config.maxLength
        if (maxLength != null && inputBuffer.length >= maxLength) {
            _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
            triggerErrorFeedback()
            return
        }

        // 조립 중인 한글이 있으면 완성
        if (hangulAssembler.isComposing()) {
            val completed = hangulAssembler.commit()
            if (completed.isNotEmpty() && inputBuffer.isNotEmpty()) {
                inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                inputBuffer.append(completed)
            }
        }

        // 공백 추가
        inputBuffer.append(' ')
    }

    /**
     * SPACE 키 처리 (암호화 모드)
     */
    private fun handleSpaceEncrypted() {
        try {
            // 최대 길이 체크
            val currentCount = keyDataManager?.inputCount ?: 0
            val maxLength = config.maxLength
            if (maxLength != null && currentCount >= maxLength) {
                _errorMessage.value = "최대 ${maxLength}자리까지 입력 가능합니다"
                triggerErrorFeedback()
                return
            }

            // 조립 중인 한글이 있으면 완성
            if (hangulAssembler.isComposing()) {
                val completed = hangulAssembler.commit()
                if (completed.isNotEmpty() && inputBuffer.isNotEmpty()) {
                    inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                    inputBuffer.append(completed)
                    // 마지막 한글 데이터 업데이트
                    keyDataManager?.removeKeyData()
                    keyDataManager?.appendKeyData(completed.toByteArray(Charsets.UTF_8))
                }
            }

            // 공백 추가
            inputBuffer.append(' ')
            keyDataManager?.appendKeyData(" ".toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            _errorMessage.value = "공백 입력 오류: ${e.message}"
        }
    }

    /**
     * 마스킹된 표시 업데이트
     */
    private fun updateMaskedDisplay() {
        if (config.enableEncryption) {
            // 암호화 모드: KeyDataManager의 inputCount 기준
            val count = keyDataManager?.inputCount ?: 0
            _maskedInput.value = if (config.showMasking) {
                config.maskingChar.toString().repeat(count)
            } else {
                inputBuffer.toString()  // 조립 중인 한글 포함한 임시 표시
            }
        } else {
            // 평문 모드: inputBuffer 기준
            _maskedInput.value = if (config.showMasking) {
                config.maskingChar.toString().repeat(inputBuffer.length)
            } else {
                inputBuffer.toString()
            }
        }
    }

    /**
     * 입력값 가져오기
     *
     * 암호화 모드: 암호화된 데이터(E2E Hex)
     * 평문 모드: 평문 문자열
     *
     * @return 입력된 데이터
     */
    fun getInputValue(): String {
        if (config.enableEncryption) {
            // 암호화 모드: 암호화된 E2E 데이터 반환
            return try {
                // 조립 중인 한글 완성
                if (hangulAssembler.isComposing()) {
                    val completed = hangulAssembler.commit()
                    if (completed.isNotEmpty() && inputBuffer.isNotEmpty()) {
                        inputBuffer.deleteCharAt(inputBuffer.lastIndex)
                        inputBuffer.append(completed)
                        // KeyDataManager 업데이트
                        keyDataManager?.removeKeyData()
                        keyDataManager?.appendKeyData(completed.toByteArray(Charsets.UTF_8))
                    }
                }
                keyDataManager?.encryptedE2eDataHex ?: ""
            } catch (e: Exception) {
                _errorMessage.value = "암호화 데이터 가져오기 오류: ${e.message}"
                ""
            }
        } else {
            // 평문 모드: 평문 반환
            var result = inputBuffer.toString()
            if (hangulAssembler.isComposing()) {
                val completed = hangulAssembler.commit()
                if (result.isNotEmpty() && completed.isNotEmpty()) {
                    result = result.dropLast(1) + completed
                }
                // 조립 상태는 유지 (다시 조립 가능하도록)
            }
            return result
        }
    }

    /**
     * 입력 초기화
     */
    fun clearInput() {
        viewModelScope.launch {
            inputBuffer.clear()
            hangulAssembler.clear()

            // 암호화 모드면 KeyDataManager도 초기화
            if (config.enableEncryption) {
                try {
                    keyDataManager?.removeAllKeyData()
                } catch (e: Exception) {
                    _errorMessage.value = "입력 초기화 오류: ${e.message}"
                }
            }

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
    /*
     * 현재 설정(타입, 언어, Shift 상태)에 따라 키 목록 생성
     */
    private fun loadKeys() {
        viewModelScope.launch {
            // 특수문자 모드이면 특수문자 레이아웃 로드
            if (_isSpecialCharMode.value && config.type == KeypadType.ALPHANUMERIC) {
                _keys.value = SpecialCharLayout.getKeys()
                return@launch
            }

            _keys.value = when (config.type) {
                KeypadType.NUMERIC -> {
                    NumericLayout.getKeys(shuffledNumbers)
                }

                KeypadType.ENGLISH -> {
                    EnglishLayout.getKeys(
                        uppercase = _isShifted.value,
                        randomize = config.randomizeLayout
                    )
                }

                KeypadType.KOREAN -> {
                    KoreanLayout.getKeys(
                        shifted = _isShifted.value,
                        randomize = config.randomizeLayout
                    )
                }

                KeypadType.ALPHANUMERIC -> {
                    // 현재 언어에 따라 분기
                    when (_currentLanguage.value) {
                        KeypadType.ENGLISH -> EnglishLayout.getKeys(
                            uppercase = _isShifted.value,
                            randomize = config.randomizeLayout
                        )

                        KeypadType.KOREAN -> KoreanLayout.getKeys(
                            shifted = _isShifted.value,
                            randomize = config.randomizeLayout
                        )

                        else -> NumericLayout.getKeys(shuffledNumbers)
                    }
                }
            }
        }
    }

    /**
     * 숫자 재배열 처리
     */
    private fun handleShuffle() {
        viewModelScope.launch {
            if (config.type == KeypadType.NUMERIC) {
                // 0-9 숫자를 랜덤하게 섞기
                shuffledNumbers = (0..9).shuffled()

                // 키 목록 다시 로드
                loadKeys()

                // 진동 피드백
                if (config.enableHapticFeedback) {
                    triggerVibration()
                }
            }
        }
    }

    /**
     * 특수문자 모드 토글 처리
     */
    private fun handleSpecialToggle() {
        viewModelScope.launch {
            _isSpecialCharMode.value = !_isSpecialCharMode.value

            // 키 목록 다시 로드
            loadKeys()

            // 진동 피드백
            if (config.enableHapticFeedback) {
                triggerVibration()
            }
        }
    }

    /**
     * 언어 전환 처리 (한/영)
     */
    private fun handleLanguageSwitch() {
        viewModelScope.launch {
            // ALPHANUMERIC 타입만 언어 전환 가능
            if (config.type == KeypadType.ALPHANUMERIC) {
                _currentLanguage.value = when (_currentLanguage.value) {
                    KeypadType.ENGLISH -> KeypadType.KOREAN
                    KeypadType.KOREAN -> KeypadType.ENGLISH
                    else -> KeypadType.ENGLISH
                }

                // Shift 상태 초기화
                _isShifted.value = false

                // 키 목록 다시 로드
                loadKeys()

                // 진동 피드백
                if (config.enableHapticFeedback) {
                    triggerVibration()
                }
            }
        }
    }

    /**
     * Shift 토글 처리
     */
    private fun handleShift() {
        viewModelScope.launch {
            _isShifted.value = !_isShifted.value

            // 키 목록 다시 로드
            loadKeys()

            // 진동 피드백
            if (config.enableHapticFeedback) {
                triggerVibration()
            }
        }
    }

    /**
     * Config 업데이트
     *
     * 키패드 타입이 변경되면 입력 초기화 및 키 목록 재로드
     */
    fun updateConfig(newConfig: KeypadConfig) {
        val typeChanged = config.type != newConfig.type
        config = newConfig

        if (typeChanged) {
            // 타입이 변경되면 입력 초기화
            inputBuffer.clear()
            hangulAssembler.clear()
            updateMaskedDisplay()

            // 언어 상태 초기화
            _currentLanguage.value = when (config.type) {
                KeypadType.ENGLISH -> KeypadType.ENGLISH
                KeypadType.KOREAN -> KeypadType.KOREAN
                else -> KeypadType.ENGLISH
            }

            // Shift 상태 초기화
            _isShifted.value = false

            // 숫자 섞기 상태 초기화
            shuffledNumbers = null

            // 키 목록 재로드
            loadKeys()
        }
    }

    /**
     * ViewModel 정리
     */
    override fun onCleared() {
        super.onCleared()

        // 메모리 정리
        inputBuffer.clear()
        hangulAssembler.clear()

        // 암호화 모드면 KeyDataManager도 정리
        if (config.enableEncryption) {
            try {
                keyDataManager?.removeAllKeyData()
            } catch (e: Exception) {
                // 정리 시 에러는 무시
            }
        }
    }
}
