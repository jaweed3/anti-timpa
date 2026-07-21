package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ConfidenceBadge(confidence: Double, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(FactLensColors.surfaceContainerHigh)
            .padding(horizontal = Spacing.sm, vertical = 4.dp)
    ) {
        Text(
            "Confidence:",
            color = FactLensColors.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "${(confidence * 100).toInt()}%",
            color = FactLensColors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
