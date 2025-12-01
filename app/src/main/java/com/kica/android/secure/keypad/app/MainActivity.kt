package com.kica.android.secure.keypad.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.unit.dp
import com.kica.android.secure.keypad.SecureKeypad
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 결과 표시
            if (actualInput.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "입력 완료",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "마스킹: $maskedInput",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "실제값: $actualInput",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 키패드
            SecureKeypad(
                config = KeypadConfig(
                    type = KeypadType.NUMERIC,
                    colors = KeypadColors.toss(), // 토스 스타일 적용!
                    maskingChar = '●',
                    maxLength = 6,
                    randomizeLayout = false,
                    enableHapticFeedback = true
                ),
                onKeyPressed = { masked ->
                    maskedInput = masked
                },
                onComplete = { input ->
                    actualInput = input
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "입력 완료: $input",
                            actionLabel = "닫기"
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
}