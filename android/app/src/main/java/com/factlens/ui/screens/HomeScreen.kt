package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.factlens.ui.components.SectionHeader
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun HomeScreen(
    onQuickScan: () -> Unit,
    onScanResult: () -> Unit,
    onViewAllHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.backgroundAlmostWhite)
    ) {
        // TopAppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "FactLens",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = FactLensColors.primary
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(FactLensColors.surfaceContainerHigh)
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FactLensColors.successEmerald)
                )
                Spacer(Modifier.width(Spacing.xs))
                Text("System Ready", fontSize = 12.sp, color = FactLensColors.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Hero / Quick Scan Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FactLensColors.primaryContainer)
                    .clickable { onQuickScan() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Scan", fontSize = 28.sp, color = Color.White)
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        "Quick Scan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Verify any text, image, or URL instantly",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Button(
                        onClick = onQuickScan,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = FactLensColors.primary
                        )
                    ) {
                        Text("Start Verification", fontWeight = FontWeight.Bold)
                    }
                }

                // Decorative blurs
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = 60.dp, y = (-40).dp)
                        .clip(RoundedCornerShape(90.dp))
                        .background(FactLensColors.primary.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = (-60).dp, y = 60.dp)
                        .clip(RoundedCornerShape(90.dp))
                        .background(FactLensColors.secondaryContainer.copy(alpha = 0.2f))
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // Recent Scans
            SectionHeader("Recent Scans", "View All", onViewAllHistory)
            Spacer(Modifier.height(Spacing.md))

            RecentScanCard(
                verdict = "Verified",
                claim = "Global temperatures reach record high...",
                detail = "Analysis confirmed by 4 independent scientific reports.",
                time = "2h ago",
                isSupported = true,
                onClick = onScanResult
            )
            Spacer(Modifier.height(Spacing.md))
            RecentScanCard(
                verdict = "Misleading",
                claim = "New policy impact on local economy...",
                detail = "Context missing regarding specific demographics.",
                time = "5h ago",
                isSupported = false,
                onClick = onScanResult
            )

            Spacer(Modifier.height(Spacing.xl))

            // Saved Results
            SectionHeader("Saved Results")
            Spacer(Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SavedResultCard(
                    title = "The future of renewable energy",
                    confidence = "98% Confidence",
                    modifier = Modifier.weight(1f),
                    onClick = onScanResult
                )
                SavedResultCard(
                    title = "Blockchain impact on supply chain",
                    confidence = "92% Confidence",
                    modifier = Modifier.weight(1f),
                    onClick = onScanResult
                )
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun RecentScanCard(
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
                Text(
                    if (isSupported) "✓" else "!",
                    color = verdictColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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

@Composable
private fun SavedResultCard(
    title: String,
    confidence: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(FactLensColors.surfaceContainerHigh)
            )
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FactLensColors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    confidence,
                    fontSize = 11.sp,
                    color = FactLensColors.neutralGray
                )
            }
        }
    }
}
