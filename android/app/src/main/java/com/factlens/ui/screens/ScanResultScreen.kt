package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

            Spacer(Modifier.height(Spacing.lg))

            if (scanResult.sources.isNotEmpty()) {
                EvidenceSourcesList(sources = scanResult.sources, onOpenSource = onOpenSource)
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }

        ScanResultBottomBar(onSave = onSave, onShare = onShare)
    }
}