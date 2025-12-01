package com.kica.android.secure.keypad.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kica.android.secure.keypad.domain.model.Key
import com.kica.android.secure.keypad.domain.model.KeyType
import com.kica.android.secure.keypad.domain.model.KeypadColors

/**
 * 키패드 버튼 컴포넌트
 *
 * @param key 키 데이터
 * @param colors 색상 테마
 * @param onClick 클릭 콜백
 * @param modifier Modifier
 */
@Composable
fun KeypadButton(
    key: Key,
    colors: KeypadColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpecialKey = key.type != KeyType.NORMAL

    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp) // 토스 스타일: 간격 넓게
            .semantics {
                // 접근성 지원
                contentDescription = when (key.type) {
                    KeyType.NORMAL -> "숫자 ${key.displayText}"
                    KeyType.BACKSPACE -> "삭제"
                    KeyType.SPACE -> "스페이스"
                    KeyType.COMPLETE -> "완료"
                    KeyType.SWITCH -> "언어 전환"
                }
                role = Role.Button
            },
        shape = RoundedCornerShape(16.dp), // 토스 스타일: 둥근 모서리
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,       // 토스 스타일: 미니멀한 그림자
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSpecialKey) {
                colors.specialKeyBackgroundColor
            } else {
                colors.keyBackgroundColor
            },
            contentColor = if (isSpecialKey) {
                colors.specialKeyTextColor
            } else {
                colors.keyTextColor
            }
        )
    ) {
        Text(
            text = key.displayText,
            fontSize = 28.sp,              // 토스 스타일: 큰 폰트
            fontWeight = FontWeight.Medium // 토스 스타일: 중간 두께
        )
    }
}