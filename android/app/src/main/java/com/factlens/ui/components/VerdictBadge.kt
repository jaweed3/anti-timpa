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
fun VerdictBadge(verdict: String, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) || verdict.contains("true", true) ->
            Triple(FactLensColors.secondaryContainer, FactLensColors.onSecondaryContainer, "✓")
        verdict.contains("contradicted", true) || verdict.contains("false", true) ->
            Triple(FactLensColors.errorContainer, FactLensColors.error, "✕")
        verdict.contains("misleading", true) ->
            Triple(FactLensColors.tertiaryFixed, FactLensColors.onTertiaryFixedVariant, "!")
        else ->
            Triple(FactLensColors.surfaceContainerHighest, FactLensColors.onSurfaceVariant, "?")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = Spacing.sm, vertical = 4.dp)
    ) {
        Text(icon, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(
            verdict.replaceFirstChar { it.uppercase() },
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
