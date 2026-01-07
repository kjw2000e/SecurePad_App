package com.kica.android.secure.keypad.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
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
 * @param useAspectRatio 1:1 비율 사용 여부 (숫자 키패드: true, 영문/한글: false)
 */
@Composable
fun KeypadButton(
    key: Key,
    colors: KeypadColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useAspectRatio: Boolean = true
) {
    if (key.type == KeyType.EMPTY) {
        Spacer(
            modifier = modifier
                .then(if (useAspectRatio) Modifier.aspectRatio(1f) else Modifier)
                .padding(1.dp)
        )
        return
    }

    val isSpecialKey = key.type != KeyType.NORMAL

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (useAspectRatio) Modifier.aspectRatio(1f) else Modifier)
            .padding(1.dp) // 간격 최소화
            .semantics {
                // 접근성 지원
                contentDescription = when (key.type) {
                    KeyType.NORMAL -> "숫자 ${key.displayText}"
                    KeyType.BACKSPACE -> "삭제"
                    KeyType.SPACE -> "스페이스"
                    KeyType.COMPLETE -> "완료"
                    KeyType.SWITCH -> "언어 전환"
                    KeyType.SHIFT -> "대소문자 전환"
                    KeyType.SHUFFLE -> "재배열"
                    KeyType.SPECIAL_TOGGLE -> "특수문자"
                    KeyType.EMPTY -> "빈문자"
                }
                role = Role.Button
            },
        shape = RoundedCornerShape(6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(2.dp), // 버튼 내부 패딩 최소화
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
