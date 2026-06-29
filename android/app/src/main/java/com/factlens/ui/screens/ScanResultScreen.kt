package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.EvidenceCard
import com.factlens.ui.components.FactCard
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import com.factlens.ScanResultData

@Composable
fun ScanResultScreen(
    scanResult: ScanResultData,
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.backgroundAlmostWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
                tint = FactLensColors.primary
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                "Verification Result",
                style = MaterialTheme.typography.titleLarge,
                color = FactLensColors.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp),
                tint = FactLensColors.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            FactCard(
                verdict = scanResult.verdict,
                confidence = scanResult.confidence,
                explanation = scanResult.explanation,
                sources = scanResult.sources,
                onClick = {}
            )

            Spacer(Modifier.height(Spacing.lg))

            if (scanResult.sources.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Evidence Sources",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FactLensColors.onBackground
                    )
                    Text("${scanResult.sources.size} Found", fontSize = 12.sp, color = FactLensColors.primary)
                }

                Spacer(Modifier.height(Spacing.md))

                scanResult.sources.forEachIndexed { index, source ->
                    EvidenceCard(
                        title = source.title,
                        domain = source.url,
                        snippet = source.snippet,
                        matchPercent = "${95 - index}% Match",
                        onClick = { onOpenSource(source.url) }
                    )
                    Spacer(Modifier.height(Spacing.md))
                }
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FactLensColors.surfaceContainerLowest)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Button(
                onClick = {},
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FactLensColors.primary,
                    contentColor = FactLensColors.onPrimary
                )
            ) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = "Bookmark",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text("Save Result")
            }
            Spacer(Modifier.width(Spacing.md))
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FactLensColors.primary)
            ) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text("Share")
            }
        }
    }
}