package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Help
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun VerdictHeaderSection(verdict: String, confidence: Double) {
    val (verdictColor, verdictIcon) = getVerdictStyle(verdict)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(verdictColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = verdictIcon, contentDescription = null, tint = verdictColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(verdict.replaceFirstChar { it.uppercase() }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.onSurface)
        }
        ConfidenceBadge(confidence = confidence)
    }
}

private fun getVerdictStyle(verdict: String): Pair<Color, ImageVector> {
    val color = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) -> FactLensColors.successEmerald
        verdict.contains("contradicted", true) || verdict.contains("false", true) -> FactLensColors.errorRed
        verdict.contains("misleading", true) -> FactLensColors.warningAmber
        else -> FactLensColors.neutralGray
    }
    val icon: ImageVector = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) -> Icons.Filled.CheckCircle
        verdict.contains("contradicted", true) || verdict.contains("false", true) -> Icons.Filled.Cancel
        verdict.contains("misleading", true) -> Icons.Filled.Warning
        else -> Icons.Filled.Help
    }
    return Pair(color, icon)
}
