package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
fun RecentScanCard(
    verdict: String,
    claim: String,
    detail: String,
    time: String,
    isSupported: Boolean,
    onClick: () -> Unit
) {
    val verdictColor = if (isSupported) FactLensColors.successEmerald else FactLensColors.warningAmber
    val badgeBg = if (isSupported)
        FactLensColors.secondaryContainer.copy(alpha = 0.2f)
    else
        FactLensColors.tertiaryFixed.copy(alpha = 0.4f)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FactLensColors.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (isSupported) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = verdictColor
                    )
                } else {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = verdictColor
                    )
                }
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        verdict,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = verdictColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Text(time, fontSize = 12.sp, color = FactLensColors.neutralGray)
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(claim, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = FactLensColors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, fontSize = 12.sp, color = FactLensColors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
