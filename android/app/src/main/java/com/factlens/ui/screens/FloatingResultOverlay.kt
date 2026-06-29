package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val verdictIcon: ImageVector = when {
        verdict.contains("supported", true) || verdict.contains("verified", true) -> Icons.Filled.CheckCircle
        verdict.contains("contradicted", true) || verdict.contains("false", true) -> Icons.Filled.Cancel
        verdict.contains("misleading", true) -> Icons.Filled.Warning
        else -> Icons.Filled.Help
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(FactLensColors.surfacePureWhite)
    ) {
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
                        Icon(
                            verdictIcon,
                            contentDescription = null,
                            tint = verdictColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        verdict.replaceFirstChar { it.uppercase() },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FactLensColors.onSurface
                    )
                }
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

            Row(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "View Full Details",
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(Modifier.width(Spacing.md))
                OutlinedButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FactLensColors.onSurfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = "Bookmark",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}