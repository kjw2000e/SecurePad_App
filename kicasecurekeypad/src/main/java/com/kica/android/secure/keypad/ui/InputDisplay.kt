package com.kica.android.secure.keypad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kica.android.secure.keypad.domain.model.InputIndicatorStyle
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadConfig

/**
 * 입력 표시 컴포넌트
 *
 * @param currentLength 현재 입력된 길이
 * @param maxLength 최대 입력 길이 (null이면 TEXT 스타일 사용)
 * @param config 키패드 설정
 * @param maskedText 마스킹된 입력 텍스트 (TEXT 스타일에서 사용)
 * @param colors 색상 테마
 * @param modifier Modifier
 */
@Composable
fun InputDisplay(
    currentLength: Int,
    maxLength: Int?,
    config: KeypadConfig,
    maskedText: String,
    colors: KeypadColors,
    modifier: Modifier = Modifier
) {
    // 접근성을 위한 안내 메시지
    val accessibilityText = remember(currentLength, maxLength) {
        when {
            currentLength == 0 -> "입력 없음"
            maxLength != null -> "${currentLength}자리 입력됨, 총 ${maxLength}자리"
            else -> "${currentLength}자리 입력됨"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = accessibilityText
                liveRegion = LiveRegionMode.Polite
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = colors.inputDisplayBackgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // maxLength가 없거나 TEXT 스타일이면 기존 텍스트 방식
            if (maxLength == null || config.inputIndicatorStyle == InputIndicatorStyle.TEXT) {
                TextStyleIndicator(
                    maskedText = maskedText,
                    textColor = colors.inputDisplayTextColor
                )
            } else {
                // 인디케이터 스타일에 따라 렌더링
                when (config.inputIndicatorStyle) {
                    InputIndicatorStyle.DOT -> DotStyleIndicator(
                        currentLength = currentLength,
                        maxLength = maxLength,
                        filledColor = colors.inputDisplayTextColor,
                        emptyColor = colors.inputDisplayTextColor.copy(alpha = 0.3f)
                    )
                    InputIndicatorStyle.UNDERLINE -> UnderlineStyleIndicator(
                        currentLength = currentLength,
                        maxLength = maxLength,
                        filledColor = colors.inputDisplayTextColor,
                        emptyColor = colors.inputDisplayTextColor.copy(alpha = 0.3f)
                    )
                    InputIndicatorStyle.BOX -> BoxStyleIndicator(
                        currentLength = currentLength,
                        maxLength = maxLength,
                        filledColor = colors.inputDisplayTextColor,
                        emptyColor = colors.inputDisplayTextColor.copy(alpha = 0.3f)
                    )
                    InputIndicatorStyle.TEXT -> {
                        // 이미 위에서 처리됨
                    }
                }
            }
        }
    }
}

/**
 * 하위 호환성을 위한 오버로드
 */
@Composable
fun InputDisplay(
    maskedText: String,
    colors: KeypadColors,
    modifier: Modifier = Modifier
) {
    InputDisplay(
        currentLength = maskedText.length,
        maxLength = null,
        config = KeypadConfig(),
        maskedText = maskedText,
        colors = colors,
        modifier = modifier
    )
}

/**
 * 텍스트 스타일 (기존 방식)
 */
@Composable
private fun TextStyleIndicator(
    maskedText: String,
    textColor: Color
) {
    Text(
        text = if (maskedText.isEmpty()) "입력 대기" else maskedText,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = textColor,
        textAlign = TextAlign.Center,
        letterSpacing = 4.sp
    )
}

/**
 * DOT 스타일 (●●●○○○)
 */
@Composable
private fun DotStyleIndicator(
    currentLength: Int,
    maxLength: Int,
    filledColor: Color,
    emptyColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val isFilled = index < currentLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) filledColor else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (isFilled) filledColor else emptyColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * UNDERLINE 스타일 (─ ─ ─ _ _ _)
 */
@Composable
private fun UnderlineStyleIndicator(
    currentLength: Int,
    maxLength: Int,
    filledColor: Color,
    emptyColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val isFilled = index < currentLength
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(if (isFilled) 4.dp else 2.dp)
                    .background(
                        color = if (isFilled) filledColor else emptyColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * BOX 스타일 (■■■□□□)
 */
@Composable
private fun BoxStyleIndicator(
    currentLength: Int,
    maxLength: Int,
    filledColor: Color,
    emptyColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val isFilled = index < currentLength
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = if (isFilled) filledColor else Color.Transparent,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isFilled) filledColor else emptyColor,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

