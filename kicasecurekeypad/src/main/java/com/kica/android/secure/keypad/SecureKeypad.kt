package com.kica.android.secure.keypad

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kica.android.secure.keypad.data.layout.NumericLayout
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadConfig
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
    // ViewModel 생성
    val viewModel: KeypadViewModel = viewModel(
        factory = KeypadViewModelFactory(config)
    )

    // 상태 관찰
    val maskedInput by viewModel.maskedInput.collectAsState()
    val shouldVibrate by viewModel.shouldVibrate.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 키 레이아웃 가져오기
    val keys = remember(config.randomizeLayout) {
        NumericLayout.getKeys(randomize = config.randomizeLayout)
    }

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
            .background(config.colors.backgroundColor)
            .padding(20.dp) // 토스 스타일: 넉넉한 외부 패딩
    ) {
        // 입력 표시 영역
        InputDisplay(
            maskedText = maskedInput,
            colors = config.colors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp) // 토스 스타일: 입력창과 키패드 간격 증가
        )

        // 키패드 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp), // 토스 스타일: 버튼 간격 조정 (padding 6dp와 조합)
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = keys,
                key = { key -> key.displayText }
            ) { key ->
                KeypadButton(
                    key = key,
                    colors = config.colors,
                    onClick = {
                        when (key.type) {
                            KeyType.COMPLETE -> {
                                // 완료 처리
                                val inputValue = viewModel.getInputValue()
                                onComplete(inputValue)
                            }
                            else -> {
                                // 일반 키 입력 처리
                                viewModel.handleKeyPress(key)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * ViewModel Factory
 */
private class KeypadViewModelFactory(
    private val config: KeypadConfig
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeypadViewModel::class.java)) {
            return KeypadViewModel(config) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}