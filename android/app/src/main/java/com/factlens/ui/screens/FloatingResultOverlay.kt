package com.factlens.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.PrimaryButton
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun FloatingResultOverlay(
    verdict: String,
    confidence: Double,
    explanation: String,
    hasDetail: Boolean = true,
    onViewDetails: () -> Unit,
    onBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val verdictColor = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) -> FactLensColors.successEmerald
        verdict.contains("contradicted", true) || verdict.contains("false", true) -> FactLensColors.errorRed
        verdict.contains("misleading", true) -> FactLensColors.warningAmber
        else -> FactLensColors.neutralGray
    }
    val iconChar = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) -> "check_circle"
        verdict.contains("contradicted", true) || verdict.contains("false", true) -> "cancel"
        verdict.contains("misleading", true) -> "warning"
        else -> "help"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(FactLensColors.surfacePureWhite)
    ) {
        // Drag handle
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FactLensColors.outlineVariant)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Verdict row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(verdictColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iconChar, fontSize = 20.sp, color = verdictColor)
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        verdict.replaceFirstChar { it.uppercase() },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FactLensColors.onSurface
                    )
                }
                // Confidence
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(FactLensColors.surfaceContainerHigh)
                        .padding(horizontal = Spacing.sm, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Confidence:", fontSize = 12.sp, color = FactLensColors.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${(confidence * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FactLensColors.primary
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // AI Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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

            Spacer(Modifier.height(Spacing.md))

            // Actions
            Row(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "View Full Details",
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Text("open_in_new", fontSize = 18.sp) }
                )
                Spacer(Modifier.width(Spacing.md))
                OutlinedButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FactLensColors.onSurfaceVariant)
                ) {
                    Text("bookmark", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}
