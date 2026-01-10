package com.kica.android.secure.keypad

import android.content.Context
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.kica.android.secure.keypad.domain.model.KeypadDisplayMode
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadType
import com.kica.android.secure.keypad.ui.InputDisplay
import com.kica.android.secure.keypad.ui.KeypadButton
import com.kica.android.secure.keypad.ui.KeypadHeader
import com.kica.android.secure.keypad.utils.findActivity
import com.kica.android.secure.keypad.viewmodel.KeypadViewModel

/**
 * 보안 키패드 Composable
 *
 * @param modifier Modifier
 * @param config 키패드 설정 (제목, 부제목, 취소 버튼 등 포함)
 * @param onKeyPressed 키 입력 콜백 (마스킹된 값 전달)
 * @param onComplete 완료 버튼 클릭 콜백 (평문 입력값 전달)
 * @param onCancel 취소 버튼 클릭 콜백
 * @param onError 에러 발생 콜백
 */
@Composable
fun SecureKeypad(
    modifier: Modifier = Modifier,
    config: KeypadConfig = KeypadConfig(),
    onKeyPressed: (maskedValue: String) -> Unit = {},
    onComplete: (inputValue: String) -> Unit = {},
    onCancel: () -> Unit = {},
    onError: (errorMessage: String) -> Unit = {},
    // 확장 콜백
    onShow: () -> Unit = {},
    onHide: () -> Unit = {},
    onClear: () -> Unit = {},
    onBackspace: () -> Unit = {}
) {
    // Context 가져오기
    val context = LocalContext.current

    // ... (중략)

    // 마스킹된 입력값 전달
    LaunchedEffect(maskedInput) {
        onKeyPressed(maskedInput)
    }

    // 초기 표시 콜백
    LaunchedEffect(Unit) {
        onShow()
    }

    // 화면 캡처 방지 및 정리
    DisposableEffect(effectiveConfig.preventScreenCapture) {
        // FLAG_SECURE 적용
        if (effectiveConfig.preventScreenCapture) {
            val activity = context.findActivity()
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            Log.d("SecureKeypad", "화면 캡처 방지 활성화 (FLAG_SECURE)")
        }

        onDispose {
            // FLAG_SECURE 해제
            if (effectiveConfig.preventScreenCapture) {
                val activity = context.findActivity()
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                Log.d("SecureKeypad", "화면 캡처 방지 해제")
            }
            viewModel.clearInput()
            onHide()
        }
    }

    // ... (중략)

        // 키패드 레이아웃
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val handleComplete = {
                if (validationResult.isValid) {
                    onComplete(viewModel.getInputValue())
                } else {
                    // 유효하지 않으면 피드백 (진동 등)
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }

            when (config.type) {
                KeypadType.NUMERIC -> {
                    // 숫자 키패드: 확인 버튼 + 3열 고정 그리드
                    NumericKeypadWithConfirm(
                        keys = keys,
                        colors = effectiveConfig.colors,
                        config = effectiveConfig,
                        viewModel = viewModel,
                        onComplete = { handleComplete() },
                        onBackspace = onBackspace,
                        onClear = onClear
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
                        onComplete = { handleComplete() },
                        onBackspace = onBackspace,
                        onClear = onClear
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
    onComplete: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
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
            viewModel = viewModel,
            onBackspace = onBackspace,
            onClear = onClear
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
    viewModel: KeypadViewModel,
    onBackspace: () -> Unit,
    onClear: () -> Unit
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
                            if (key.type == KeyType.DELETE) {
                                onBackspace()
                            }
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
    onComplete: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
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
                                KeyType.DELETE -> {
                                    viewModel.handleKeyPress(key)
                                    onBackspace()
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
