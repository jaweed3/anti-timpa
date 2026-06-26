package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FactLensColors.primary,
            contentColor = FactLensColors.onPrimary
        )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FactLensColors.primary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(FactLensColors.outline)
        )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FactCard(
    verdict: String,
    confidence: Double,
    explanation: String,
    sources: List<com.factlens.model.Source>,
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
        // Verdict header
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

        // Summary
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

@Composable
fun EvidenceCard(
    title: String,
    domain: String,
    snippet: String,
    matchPercent: String,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = FactLensColors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        domain,
                        color = FactLensColors.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    matchPercent,
                    color = FactLensColors.successEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FactLensColors.successEmerald.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = FactLensColors.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(Spacing.md))
            SecondaryButton(text = "Open Source", onClick = onClick)
        }
    }
}

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

@Composable
fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = FactLensColors.onSurface)
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.labelLarge,
                color = FactLensColors.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}
