package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ExplanationSection(explanation: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FactLensColors.surfaceContainerLow)
            .padding(Spacing.md)
    ) {
        Text(
            explanation,
            fontSize = 14.sp,
            color = FactLensColors.onSurfaceVariant,
            lineHeight = 20.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}
