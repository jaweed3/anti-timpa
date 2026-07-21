package com.factlens.ui.components

import androidx.compose.foundation.background
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
import com.factlens.model.Source
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun FactCard(
    verdict: String,
    confidence: Double,
    explanation: String,
    sources: List<Source>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verdictColor = when {
        verdict.contains("supported", true) -> FactLensColors.successEmerald
        verdict.contains("contradicted", true) -> FactLensColors.errorRed
        verdict.contains("misleading", true) -> FactLensColors.warningAmber
        else -> FactLensColors.neutralGray
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(verdictColor)
                .padding(Spacing.lg)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    verdict.uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(Spacing.sm))
                ConfidenceBadge(confidence = confidence)
            }
        }

        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                "AI Summary",
                style = MaterialTheme.typography.titleMedium,
                color = FactLensColors.onSurface
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = FactLensColors.onSurfaceVariant
            )
        }
    }
}
