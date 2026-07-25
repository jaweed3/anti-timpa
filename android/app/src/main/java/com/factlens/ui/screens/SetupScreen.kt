package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.SetupLogoHeader
import com.factlens.ui.components.SetupStep
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun SetupScreen(
    hasOverlayPermission: Boolean,
    hasScreenRecording: Boolean,
    isServiceRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestScreenRecording: () -> Unit,
    onStartService: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.background)
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))
        SetupLogoHeader()
        Spacer(Modifier.height(48.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite)
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Text("Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
                Spacer(Modifier.height(Spacing.lg))

                SetupStep(number = "1", title = "Overlay Permission", desc = "Allow AntiTimpa to show the floating button", done = hasOverlayPermission, onAction = onRequestOverlay)
                Spacer(Modifier.height(Spacing.md))
                SetupStep(number = "2", title = "Screen Recording", desc = "Allow AntiTimpa to capture your screen", done = hasScreenRecording, onAction = onRequestScreenRecording)
                Spacer(Modifier.height(Spacing.md))
                SetupStep(number = "3", title = "Start Overlay", desc = "Launch the floating verification button", done = isServiceRunning, onAction = onStartService)
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Hold the floating button on any screen\nto scan for scams instantly",
            fontSize = 13.sp,
            color = FactLensColors.neutralGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xl))
    }
}
