package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun AreaSelectionOverlayContent(
    onConfirmArea: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
                .padding(bottom = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Area Selection",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FactLensColors.onSurface
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Drag to select the area you want to analyze",
                    fontSize = 13.sp,
                    color = FactLensColors.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirmArea(0, 0, 0, 0) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FactLensColors.primary
                        )
                    ) {
                        Text("Capture Area")
                    }
                }
            }
        }
    }
}
