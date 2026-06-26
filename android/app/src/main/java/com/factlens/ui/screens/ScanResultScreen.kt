package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.EvidenceCard
import com.factlens.ui.components.FactCard
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanResultScreen(
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit
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
                "arrow_back",
                fontSize = 24.sp,
                color = FactLensColors.primary,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                "Verification Result",
                style = MaterialTheme.typography.titleLarge,
                color = FactLensColors.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text("account_circle", fontSize = 24.sp, color = FactLensColors.onSurfaceVariant)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Verdict FactCard
            FactCard(
                verdict = "Supported",
                confidence = 0.98,
                explanation = "This claim is highly supported by current scientific consensus and official documentation. Cross-referencing multiple verified datasets confirms the temporal trends and statistical significance mentioned in the query.",
                sources = listOf(),
                onClick = {}
            )

            Spacer(Modifier.height(Spacing.lg))

            // Evidence section
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
                Text("3 Found", fontSize = 12.sp, color = FactLensColors.primary)
            }

            Spacer(Modifier.height(Spacing.md))

            EvidenceCard(
                title = "NASA Climate Study",
                domain = "nasa.gov",
                snippet = "Detailed analysis of global surface temperature changes over the last decade, showing a consistent upward trend across all recorded stations.",
                matchPercent = "99% Match",
                onClick = { onOpenSource("https://nasa.gov") }
            )

            Spacer(Modifier.height(Spacing.md))

            EvidenceCard(
                title = "NOAA Arctic Report Card",
                domain = "noaa.gov",
                snippet = "Annual update on the state of the Arctic, highlighting the rapid decrease in sea ice and its impact on global weather patterns.",
                matchPercent = "96% Match",
                onClick = { onOpenSource("https://noaa.gov") }
            )

            Spacer(Modifier.height(Spacing.md))

            EvidenceCard(
                title = "Nature Climate Journal",
                domain = "nature.com",
                snippet = "Peer-reviewed publication discussing the socio-economic impacts of climate change in emerging coastal economies.",
                matchPercent = "94% Match",
                onClick = { onOpenSource("https://nature.com") }
            )

            Spacer(Modifier.height(Spacing.xxxl))
        }

        // Bottom action bar
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
                Text("bookmark", fontSize = 18.sp)
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
                Text("share", fontSize = 18.sp)
                Spacer(Modifier.width(Spacing.sm))
                Text("Share")
            }
        }
    }
}


