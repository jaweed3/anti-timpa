package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.factlens.ui.components.ClaimInfoCard
import com.factlens.ui.components.EvidenceSourcesList
import com.factlens.ui.components.FactCard
import com.factlens.ui.components.ScanResultBottomBar
import com.factlens.ui.components.ScanResultTopBar
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import com.factlens.model.ScanResultData

@Composable
fun ScanResultScreen(
    scanResult: ScanResultData,
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit,
    onSave: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val flaggedItems = remember(scanResult) { parseFlaggedItems(scanResult.explanation) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.backgroundAlmostWhite)
    ) {
        ScanResultTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            if (scanResult.claim.isNotBlank()) {
                ClaimInfoCard(claim = scanResult.claim)
                Spacer(Modifier.height(Spacing.lg))
            }

            FactCard(
                verdict = scanResult.verdict,
                confidence = scanResult.confidence,
                explanation = scanResult.explanation,
                sources = scanResult.sources,
                onClick = {}
            )

            if (flaggedItems.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.lg))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Item Terdeteksi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        flaggedItems.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (scanResult.sources.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.lg))
                EvidenceSourcesList(sources = scanResult.sources, onOpenSource = onOpenSource)
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }

        ScanResultBottomBar(onSave = onSave, onShare = onShare)
    }
}

private fun parseFlaggedItems(explanation: String): List<String> {
    val items = mutableListOf<String>()
    val lines = explanation.split("\n")
    var inFlagged = false
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed == "Item terdeteksi:") {
            inFlagged = true
            continue
        }
        if (inFlagged && trimmed.startsWith("\u2022")) {
            items.add(trimmed.removePrefix("\u2022 "))
        }
    }
    return items
}