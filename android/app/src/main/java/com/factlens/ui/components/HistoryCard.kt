package com.factlens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun HistoryCard(
    claim: String,
    time: String,
    verdict: String,
    sourceLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    claim,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FactLensColors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = FactLensColors.neutralGray
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                VerdictBadge(verdict = verdict)
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    "• $sourceLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = FactLensColors.neutralGray
                )
            }
        }
    }
}
