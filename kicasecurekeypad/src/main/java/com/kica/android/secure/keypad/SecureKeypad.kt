package com.kica.android.secure.keypad

import android.content.Context
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kica.android.secure.keypad.data.layout.EnglishLayout
import com.kica.android.secure.keypad.data.layout.KoreanLayout
import com.kica.android.secure.keypad.data.layout.SpecialCharLayout
import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadType
import com.kica.android.secure.keypad.ui.InputDisplay
import com.kica.android.secure.keypad.ui.KeypadButton
import com.kica.android.secure.keypad.viewmodel.KeypadViewModel

/**
 * 보안 키패드 Composable
 *
 * @param modifier Modifier
 * @param config 키패드 설정
 * @param onKeyPressed 키 입력 콜백 (마스킹된 값 전달)
 * @param onComplete 완료 버튼 클릭 콜백 (평문 입력값 전달)
 * @param onError 에러 발생 콜백
 */
@Composable
fun SecureKeypad(
    modifier: Modifier = Modifier,
    config: KeypadConfig = KeypadConfig(),
    onKeyPressed: (maskedValue: String) -> Unit = {},
    onComplete: (inputValue: String) -> Unit = {},
    onError: (errorMessage: String) -> Unit = {}
) {
    // Context 가져오기
    val context = LocalContext.current

    // 다크 모드 감지 및 설정 적용
    val isSystemDark = isSystemInDarkTheme()
    Log.d("SecureKeypad", "다크 모드 감지: isSystemDark = $isSystemDark")

    // 최종 설정 결정: 사용자가 기본 색상을 사용하고 있고, 시스템이 다크 모드이면 다크 테마 적용
    val effectiveConfig = remember(config, isSystemDark) {
        val isDefaultColors = config.colors == KeypadColors.default()
        Log.d("SecureKeypad", "설정 확인: isDefaultColors = $isDefaultColors, isSystemDark = $isSystemDark")

        if (isDefaultColors && isSystemDark) {
            Log.d("SecureKeypad", "다크 테마 적용")
            config.copy(colors = KeypadColors.dark())
        } else {
            Log.d("SecureKeypad", "기존 설정 유지 (사용자 커스텀 컬러 또는 라이트 모드)")
            config
        }
    }

    // ViewModel 생성
    val viewModel: KeypadViewModel = viewModel(
        factory = KeypadViewModelFactory(context, effectiveConfig)
    )

    // Config 변경 감지 및 ViewModel 업데이트
    LaunchedEffect(config.type) {
        viewModel.updateConfig(config)
    }

    // 상태 관찰
    val maskedInput by viewModel.maskedInput.collectAsState()
    val shouldVibrate by viewModel.shouldVibrate.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val keys by viewModel.keys.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isSpecialCharMode by viewModel.isSpecialCharMode.collectAsState()

    // View 참조 (진동 피드백용)
    val view = LocalView.current

    // 진동 피드백 처리
    LaunchedEffect(shouldVibrate) {
        if (shouldVibrate) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onVibrationHandled()
        }
    }

    // 에러 메시지 처리
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            onError(it)
            viewModel.clearErrorMessage()
        }
    }

    // 마스킹된 입력값 전달
    LaunchedEffect(maskedInput) {
        onKeyPressed(maskedInput)
    }

    // 정리
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearInput()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 네비게이션 바 패딩
    ) {
        // 입력 표시 영역 (배경색 없음)
        InputDisplay(
            maskedText = maskedInput,
            colors = effectiveConfig.colors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp) // 좌우 패딩
                .padding(top = 12.dp, bottom = 12.dp) // 상하 패딩 (간격 포함)
        )

        // 키패드 레이아웃 (배경색 적용)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(effectiveConfig.colors.backgroundColor) // 배경색 적용
                .padding(horizontal = 8.dp, vertical = 12.dp) // 내부 여백
        ) {
            when (config.type) {
                KeypadType.NUMERIC -> {
                    // 숫자 키패드: 확인 버튼 + 3열 고정 그리드
                    NumericKeypadWithConfirm(
                        keys = keys,
                        colors = effectiveConfig.colors,
                        config = effectiveConfig,
                        viewModel = viewModel,
                        onComplete = onComplete
                    )
                }

                KeypadType.ENGLISH, KeypadType.KOREAN, KeypadType.ALPHANUMERIC -> {
                    // 영문/한글 키패드: 각 행마다 다른 열 수
                    AlphabeticKeypadLayout(
                        keys = keys,
                        colors = effectiveConfig.colors,
                        config = effectiveConfig,
                        keypadType = if (config.type == KeypadType.ALPHANUMERIC) currentLanguage else config.type,
                        isSpecialCharMode = isSpecialCharMode,
                        viewModel = viewModel,
                        onComplete = onComplete
                    )
                }
            }
        }
    }
}

/**
 * 숫자 키패드와 확인 버튼
 */
@Composable
private fun NumericKeypadWithConfirm(
    keys: List<Key>,
    colors: KeypadColors,
    config: KeypadConfig,
    viewModel: KeypadViewModel,
    onComplete: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        // 숫자 키패드
        NumericKeypadLayout(
            keys = keys,
            colors = colors,
            config = config,
            viewModel = viewModel
        )

        // 확인 버튼
        KeypadButton(
            key = Key(
                value = "",
                displayText = "확인",
                type = KeyType.COMPLETE
            ),
            colors = colors,
            onClick = {
                val inputValue = viewModel.getInputValue()
                onComplete(inputValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(config.buttonHeight),
            useAspectRatio = false
        )

    }
}

/**
 * 숫자 키패드 레이아웃 (3열 고정 그리드)
 */
@Composable
private fun NumericKeypadLayout(
    keys: List<Key>,
    colors: KeypadColors,
    config: KeypadConfig,
    viewModel: KeypadViewModel
) {
    // 키를 3개씩 묶어서 행으로 분할
    val rows = keys.chunked(3)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { rowKeys ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowKeys.forEach { key ->
                    KeypadButton(
                        key = key,
                        colors = colors,
                        onClick = {
                            viewModel.handleKeyPress(key)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(config.buttonHeight),
                        useAspectRatio = false
                    )
                }
            }
        }
    }
}

/**
 * 영문/한글 키패드 레이아웃 (행마다 다른 열 수)
 */
@Composable
private fun AlphabeticKeypadLayout(
    keys: List<Key>,
    colors: KeypadColors,
    config: KeypadConfig,
    keypadType: KeypadType,
    isSpecialCharMode: Boolean,
    viewModel: KeypadViewModel,
    onComplete: (String) -> Unit
) {
    // 각 행의 키 개수
    val rowSizes = if (isSpecialCharMode) {
        SpecialCharLayout.rowSizes
    } else {
        when (keypadType) {
            KeypadType.ENGLISH -> EnglishLayout.rowSizes
            KeypadType.KOREAN -> KoreanLayout.rowSizes
            else -> listOf(10, 9, 9, 3)
        }
    }

    // 키를 행별로 분할
    val rows = mutableListOf<List<Key>>()
    var startIndex = 0
    rowSizes.forEach { size ->
        if (startIndex + size <= keys.size) {
            rows.add(keys.subList(startIndex, startIndex + size))
            startIndex += size
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp), // 행 간격 줄임
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { rowKeys ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp), // 버튼 간격 줄임
                modifier = Modifier.fillMaxWidth()
            ) {
                rowKeys.forEach { key ->
                    KeypadButton(
                        key = key,
                        colors = colors,
                        onClick = {
                            when (key.type) {
                                KeyType.COMPLETE -> {
                                    val inputValue = viewModel.getInputValue()
                                    onComplete(inputValue)
                                }
                                else -> {
                                    viewModel.handleKeyPress(key)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(config.buttonHeight), // config에서 높이 가져오기
                        useAspectRatio = false // 영문/한글 키패드는 1:1 비율 사용 안함
                    )
                }
            }
        }
    }
}

/**
 * ViewModel Factory
 */
private class KeypadViewModelFactory(
    private val context: Context,
    private val config: KeypadConfig
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeypadViewModel::class.java)) {
            return KeypadViewModel(context, config) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
