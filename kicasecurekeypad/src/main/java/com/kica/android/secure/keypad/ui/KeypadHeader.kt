package com.kica.android.secure.keypad.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kica.android.secure.keypad.domain.model.KeypadColors

/**
 * 키패드 헤더 컴포넌트
 *
 * 제목, 부제목, 취소 버튼을 표시하는 상단 영역
 *
 * @param title 제목 텍스트 (예: "비밀번호 입력")
 * @param subtitle 부제목 텍스트 (예: "숫자 6자리를 입력해주세요")
 * @param showCancelButton 취소 버튼 표시 여부
 * @param cancelButtonText 취소 버튼 텍스트
 * @param colors 색상 테마
 * @param onCancel 취소 버튼 클릭 콜백
 * @param modifier Modifier
 */
@Composable
fun KeypadHeader(
    title: String?,
    subtitle: String?,
    showCancelButton: Boolean,
    cancelButtonText: String,
    colors: KeypadColors,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 제목과 부제목이 모두 없고 취소 버튼도 없으면 렌더링하지 않음
    if (title == null && subtitle == null && !showCancelButton) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // 제목/부제목 영역
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 제목
            if (title != null) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.titleColor
                )
            }

            // 부제목
            if (subtitle != null) {
                if (title != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.subtitleColor
                )
            }
        }

        // 취소 버튼
        if (showCancelButton) {
            Text(
                text = cancelButtonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.cancelButtonColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable(onClick = onCancel)
                    .padding(8.dp)
            )
        }
    }
}
