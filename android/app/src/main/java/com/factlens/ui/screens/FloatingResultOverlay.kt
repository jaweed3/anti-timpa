package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.factlens.ui.components.ExplanationSection
import com.factlens.ui.components.OverlayActionButtons
import com.factlens.ui.components.VerdictHeaderSection
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
            VerdictHeaderSection(verdict = verdict, confidence = confidence)
            Spacer(Modifier.height(Spacing.md))
            ExplanationSection(explanation = explanation)
            Spacer(Modifier.height(Spacing.md))
            OverlayActionButtons(onViewDetails = onViewDetails, onBookmark = onBookmark)
            Spacer(Modifier.height(Spacing.md))
        }
    }
}