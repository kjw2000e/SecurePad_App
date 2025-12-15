package com.kica.android.secure.keypad.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kica.android.secure.keypad.domain.model.KeypadColors

/**
 * 입력 표시 컴포넌트
 *
 * @param maskedText 마스킹된 입력 텍스트 (예: "●●●")
 * @param colors 색상 테마
 * @param modifier Modifier
 */
@Composable
fun InputDisplay(
    maskedText: String,
    colors: KeypadColors,
    modifier: Modifier = Modifier
) {
    // 접근성을 위한 안내 메시지
    val accessibilityText = remember(maskedText) {
        if (maskedText.isEmpty()) {
            "입력 없음"
        } else {
            "${maskedText.length}자리 입력됨"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = accessibilityText
                liveRegion = LiveRegionMode.Polite
            },
        shape = RoundedCornerShape(16.dp), // 토스 스타일: 둥근 모서리
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp // 토스 스타일: 미니멀한 그림자
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.inputDisplayBackgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // 토스 스타일: 넉넉한 패딩
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (maskedText.isEmpty()) "입력 대기" else maskedText,
                fontSize = 24.sp,              // 토스 스타일: 더 큰 폰트
                fontWeight = FontWeight.Medium, // 토스 스타일: 중간 두께
                color = colors.inputDisplayTextColor,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp // 토스 스타일: 글자 간격
            )
        }
    }
}
