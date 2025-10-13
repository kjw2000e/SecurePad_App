package com.kica.android.secure.keypad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecureKeypad(
    modifier: Modifier = Modifier,
    onKeyPressed: (String) -> Unit = {}
) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "⌫", "0", "✓"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var displayText by remember { mutableStateOf("") }

        // 키패드 ui 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(keys) { key ->
                Button(
                    onClick = {
                        when (key) {
                            "⌫" -> {
                                // 백스페이스
                                if (displayText.isNotEmpty()) {
                                    displayText = displayText.dropLast(1)
                                }
                            }

                            "✓" -> {
                                // 완료
                                onKeyPressed(displayText)
                            }
                            else -> {
                                displayText += key
                            }
                        }
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (key) {
                            "⌫", "✓" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        text = key,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}