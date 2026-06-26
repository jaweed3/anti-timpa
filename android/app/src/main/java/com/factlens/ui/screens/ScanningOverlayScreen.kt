package com.factlens.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanningOverlayContent(
    onDismiss: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.background)
    ) {
        // Branding header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(FactLensColors.surfacePureWhite.copy(alpha = 0.9f))
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FactLens", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(FactLensColors.surfacePureWhite.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text("close", fontSize = 20.sp, color = FactLensColors.onSurfaceVariant,
                    modifier = Modifier.clickable { onDismiss() })
            }
        }

        // Scanning feedback card
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "refresh",
                                fontSize = 20.sp,
                                color = FactLensColors.primary
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                "SCANNING SCREEN CONTENT...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FactLensColors.primary,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(Modifier.height(Spacing.md))

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(FactLensColors.surfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressAnim.value)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(FactLensColors.primary)
                            )
                        }

                        Spacer(Modifier.height(Spacing.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("FactLens AI v2.4", fontSize = 11.sp, color = FactLensColors.onSurfaceVariant)
                            Text(
                                "${(progressAnim.value * 100).toInt()}% Analysis Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FactLensColors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        androidx.compose.foundation.clickable { onClick() }
    )
}
