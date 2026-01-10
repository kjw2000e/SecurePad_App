package com.kica.android.secure.keypad.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kica.android.secure.keypad.SecureKeypad
import com.kica.android.secure.keypad.domain.model.InputIndicatorStyle
import com.kica.android.secure.keypad.domain.model.InputValidation
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadDisplayMode
import com.kica.android.secure.keypad.domain.model.KeypadType
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                EnhancedSampleScreen()
            }
        }
    }
}

// 테마 프리셋 정의
enum class ThemePreset(val displayName: String, val colors: KeypadColors) {
    DEFAULT("라이트", KeypadColors.toss()),  // 기본을 라이트(토스) 테마로
    DARK("다크", KeypadColors.dark()),
    KICA("KICA", KeypadColors(
        // KICA 브랜드 컬러 (파랑, 연두, 주황 포인트 + 차분한 키패드)
        backgroundColor = Color(0xFFF5F7FA),           // 밝은 배경
        keyBackgroundColor = Color(0xFFFFFFFF),        // 흰색 버튼 (차분하게)
        keyTextColor = Color(0xFF2D3748),              // 진한 회색 텍스트
        specialKeyBackgroundColor = Color(0xFFE2E8F0), // 연한 회색 (특수 키)
        specialKeyTextColor = Color(0xFF2D3748),       // 진한 회색
        inputDisplayBackgroundColor = Color(0xFFFFFFFF),
        inputDisplayTextColor = Color(0xFF1976D2),     // Material Blue
        titleColor = Color(0xFF1976D2),                // Material Blue
        subtitleColor = Color(0xFF4CAF50),             // Material Green
        cancelButtonColor = Color(0xFFFF9800)          // Material Orange
    )),
    LAVENDER("라벤더", KeypadColors.lavender()),
    CUSTOM("커스텀", KeypadColors(
        backgroundColor = Color(0xFF1A1A2E),
        keyBackgroundColor = Color(0xFF16213E),
        keyTextColor = Color(0xFFE94560),
        specialKeyBackgroundColor = Color(0xFF0F3460),
        specialKeyTextColor = Color(0xFFE94560),
        inputDisplayBackgroundColor = Color(0xFF0F3460),
        inputDisplayTextColor = Color(0xFFE94560),
        titleColor = Color(0xFFE94560),
        subtitleColor = Color(0xFFE94560).copy(alpha = 0.7f),
        cancelButtonColor = Color(0xFFE94560)
    ))
}

// 사용 시나리오 정의
enum class UseCaseScenario(
    val displayName: String,
    val description: String,
    val keypadType: KeypadType,
    val title: String,
    val subtitle: String,
    val maxLength: Int,
    val minLength: Int,
    val indicatorStyle: InputIndicatorStyle,
    val showFixedSlots: Boolean,
    val enableEncryption: Boolean
) {
    PIN_CODE(
        displayName = "PIN 번호",
        description = "6자리 숫자",
        keypadType = KeypadType.NUMERIC,
        title = "PIN 입력",
        subtitle = "숫자 6자리를 입력해주세요",
        maxLength = 6,
        minLength = 6,
        indicatorStyle = InputIndicatorStyle.DOT,
        showFixedSlots = true,
        enableEncryption = true
    ),
    SIMPLE_PASSWORD(
        displayName = "간편 비밀번호",
        description = "4자리 숫자",
        keypadType = KeypadType.NUMERIC,
        title = "간편 비밀번호",
        subtitle = "4자리 숫자를 입력해주세요",
        maxLength = 4,
        minLength = 4,
        indicatorStyle = InputIndicatorStyle.UNDERLINE,
        showFixedSlots = true,
        enableEncryption = true
    ),
    ACCOUNT_PASSWORD(
        displayName = "계정 비밀번호",
        description = "영문/숫자/특수문자",
        keypadType = KeypadType.ALPHANUMERIC,
        title = "비밀번호 입력",
        subtitle = "영문, 숫자, 특수문자 조합 8자 이상",
        maxLength = 20,
        minLength = 8,
        indicatorStyle = InputIndicatorStyle.DOT,
        showFixedSlots = false,
        enableEncryption = true
    ),
    OTP_CODE(
        displayName = "OTP 코드",
        description = "인증번호 6자리",
        keypadType = KeypadType.NUMERIC,
        title = "인증번호 입력",
        subtitle = "SMS로 전송된 6자리 코드",
        maxLength = 6,
        minLength = 6,
        indicatorStyle = InputIndicatorStyle.BOX,
        showFixedSlots = true,
        enableEncryption = false
    ),
    AMOUNT_INPUT(
        displayName = "금액 입력",
        description = "송금 금액",
        keypadType = KeypadType.NUMERIC,
        title = "송금 금액",
        subtitle = "보낼 금액을 입력해주세요",
        maxLength = 10,
        minLength = 1,
        indicatorStyle = InputIndicatorStyle.TEXT,
        showFixedSlots = false,
        enableEncryption = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSampleScreen() {
    // 상태 관리
    var selectedScenario by remember { mutableStateOf<UseCaseScenario?>(null) }
    var selectedTheme by remember { mutableStateOf(ThemePreset.DEFAULT) }
    var displayMode by remember { mutableStateOf(KeypadDisplayMode.HALF) }
    var enableRandomize by remember { mutableStateOf(false) }
    var enableHaptic by remember { mutableStateOf(true) }
    var showKeypad by remember { mutableStateOf(false) }

    var maskedInput by remember { mutableStateOf("") }
    var actualInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showKeypad && selectedScenario != null) {
                val scenario = selectedScenario!!
                SecureKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    config = KeypadConfig(
                        type = scenario.keypadType,
                        displayMode = displayMode,
                        colors = selectedTheme.colors,
                        title = scenario.title,
                        subtitle = scenario.subtitle,
                        showCancelButton = true,
                        cancelButtonText = "취소",
                        maskingChar = '●',
                        inputIndicatorStyle = scenario.indicatorStyle,
                        showMasking = false,
                        showFixedInputSlots = scenario.showFixedSlots,
                        maxLength = scenario.maxLength,
                        validation = InputValidation(
                            minLength = scenario.minLength
                        ),
                        randomizeLayout = enableRandomize,
                        enableHapticFeedback = enableHaptic,
                        enableEncryption = scenario.enableEncryption
                    ),
                    onKeyPressed = { masked -> maskedInput = masked },
                    onComplete = { input ->
                        actualInput = input
                        showKeypad = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "✓ 입력 완료! (${if (scenario.enableEncryption) "암호화됨" else "평문"})",
                                actionLabel = "확인"
                            )
                        }
                    },
                    onCancel = {
                        showKeypad = false
                        maskedInput = ""
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "입력이 취소되었습니다",
                                actionLabel = "확인"
                            )
                        }
                    },
                    onError = { errorMsg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message = errorMsg)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 결과 표시
            if (actualInput.isNotEmpty()) {
                ResultCard(
                    scenario = selectedScenario,
                    maskedInput = maskedInput,
                    actualInput = actualInput,
                    onClear = { actualInput = ""; maskedInput = "" }
                )
            }

            // 1. 사용 시나리오 선택
            SectionCard(title = "📱 사용 시나리오") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UseCaseScenario.values().forEach { scenario ->
                        ScenarioChip(
                            scenario = scenario,
                            isSelected = selectedScenario == scenario,
                            onClick = {
                                selectedScenario = scenario
                                actualInput = ""
                                maskedInput = ""
                            }
                        )
                    }
                }
            }

            // 2. 테마 선택
            SectionCard(title = "🎨 테마") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemePreset.values().forEach { theme ->
                        ThemePreviewChip(
                            theme = theme,
                            isSelected = selectedTheme == theme,
                            onClick = { selectedTheme = theme }
                        )
                    }
                }
            }

            // 3. 표시 모드
            SectionCard(title = "📐 표시 모드") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeypadDisplayMode.values().forEach { mode ->
                        FilterChip(
                            selected = displayMode == mode,
                            onClick = { displayMode = mode },
                            label = {
                                Text(
                                    when (mode) {
                                        KeypadDisplayMode.FULL -> "전체화면"
                                        KeypadDisplayMode.HALF -> "하단 절반"
                                        KeypadDisplayMode.COMPACT -> "컴팩트"
                                    }
                                )
                            },
                            leadingIcon = if (displayMode == mode) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            // 4. 추가 옵션
            SectionCard(title = "⚙️ 옵션") {
                Column {
                    OptionToggle(
                        title = "키 배열 랜덤화",
                        subtitle = "매번 숫자 위치가 바뀝니다",
                        checked = enableRandomize,
                        onCheckedChange = { enableRandomize = it }
                    )
                    OptionToggle(
                        title = "햅틱 피드백",
                        subtitle = "키 입력 시 진동 발생",
                        checked = enableHaptic,
                        onCheckedChange = { enableHaptic = it }
                    )
                }
            }

            // 시작 버튼
            Button(
                onClick = {
                    if (selectedScenario != null) {
                        showKeypad = true
                        maskedInput = ""
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("시나리오를 먼저 선택해주세요")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedScenario != null
            ) {
                Text(
                    text = if (selectedScenario != null)
                        "🔐 ${selectedScenario!!.displayName} 키패드 열기"
                    else
                        "시나리오를 선택해주세요",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(100.dp)) // 키패드 공간 확보
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun ScenarioChip(
    scenario: UseCaseScenario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ThemePreviewChip(
    theme: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(theme.colors.backgroundColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                        .background(theme.colors.keyBackgroundColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun OptionToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ResultCard(
    scenario: UseCaseScenario?,
    maskedInput: String,
    actualInput: String,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ 입력 완료",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "지우기",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onClear)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (scenario != null) {
                Text(
                    text = "시나리오: ${scenario.displayName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "마스킹: $maskedInput",
                style = MaterialTheme.typography.bodyMedium
            )

            if (scenario?.enableEncryption == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "암호화 데이터: ${actualInput.take(60)}${if (actualInput.length > 60) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "입력값: $actualInput",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
