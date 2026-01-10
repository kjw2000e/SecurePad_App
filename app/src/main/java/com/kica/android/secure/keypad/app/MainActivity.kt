package com.kica.android.secure.keypad.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kica.android.secure.keypad.SecureKeypad
import com.kica.android.secure.keypad.domain.model.InputIndicatorStyle
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadType
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                SampleScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen() {
    var selectedKeypadType by remember { mutableStateOf<KeypadType?>(null) }
    var maskedInput by remember { mutableStateOf("") }
    var actualInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "보안 키패드 데모") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // 키패드를 하단에 고정
            selectedKeypadType?.let { keypadType ->
                SecureKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    config = KeypadConfig(
                        type = keypadType,
                        colors = KeypadColors.default(),
                        // 새 기능: 제목 및 부제목
                        title = if (keypadType == KeypadType.NUMERIC) "비밀번호 입력" else "비밀번호 입력",
                        subtitle = if (keypadType == KeypadType.NUMERIC)
                            "숫자 6자리를 입력해주세요"
                        else
                            "영문, 숫자, 특수문자를 조합해주세요",
                        showCancelButton = true,
                        cancelButtonText = "취소",
                        maskingChar = '●',
                        // 새 기능: 인디케이터 스타일 (숫자 키패드만 DOT 스타일)
                        inputIndicatorStyle = if (keypadType == KeypadType.NUMERIC)
                            InputIndicatorStyle.DOT
                        else
                            InputIndicatorStyle.TEXT,
                        showMasking = false,
                        maxLength = if (keypadType == KeypadType.NUMERIC) 6 else 20,
                        randomizeLayout = false,
                        enableHapticFeedback = true,
                        enableEncryption = true  // 암호화 활성화
                    ),
                    onKeyPressed = { masked ->
                        maskedInput = masked
                    },
                    onComplete = { input ->
                        actualInput = input
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "입력 완료 (길이: ${input.length}자)",
                                actionLabel = "닫기"
                            )
                        }
                    },
                    onCancel = {
                        // 취소 버튼 클릭 시 키패드 닫기
                        selectedKeypadType = null
                        maskedInput = ""
                        actualInput = ""
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "입력이 취소되었습니다",
                                actionLabel = "확인"
                            )
                        }
                    },
                    onError = { errorMsg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorMsg,
                                actionLabel = "확인"
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 결과 표시
            if (actualInput.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "입력 완료 (암호화 모드)",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "마스킹: $maskedInput",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "암호화 데이터 (Hex):",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = actualInput.take(200) + if (actualInput.length > 200) "..." else "",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "총 길이: ${actualInput.length} 문자",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 키패드 선택 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 숫자 키패드 버튼
                Button(
                    onClick = {
                        selectedKeypadType = KeypadType.NUMERIC
                        actualInput = ""
                        maskedInput = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedKeypadType == KeypadType.NUMERIC)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "숫자 키패드",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 문자 키패드 버튼
                Button(
                    onClick = {
                        selectedKeypadType = KeypadType.ALPHANUMERIC
                        actualInput = ""
                        maskedInput = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedKeypadType == KeypadType.ALPHANUMERIC)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "문자 키패드\n(한글/영문)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // 키패드가 선택되지 않았을 때 안내 메시지
            if (selectedKeypadType == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "👆",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "위 버튼을 눌러\n키패드를 선택하세요",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
