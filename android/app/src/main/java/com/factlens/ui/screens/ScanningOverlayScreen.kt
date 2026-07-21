package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.factlens.ui.components.ScanningHeader
import com.factlens.ui.components.ScanningProgressCard
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanningOverlayContent(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.background)
    ) {
        ScanningHeader(onDismiss = onDismiss)

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
                ScanningProgressCard()
            }
        }
    }
}