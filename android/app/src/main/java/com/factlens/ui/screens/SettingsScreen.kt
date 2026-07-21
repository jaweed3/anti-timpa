package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.AppVersionFooter
import com.factlens.ui.components.SettingsRow
import com.factlens.ui.components.SettingsToggleRow
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun SettingsScreen(
    hasOverlayPermission: Boolean,
    onRequestOverlay: () -> Unit,
    overlayVisible: Boolean = true,
    onToggleOverlay: (Boolean) -> Unit = {}
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
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.Security,
                contentDescription = "Security",
                modifier = Modifier.size(24.dp),
                tint = FactLensColors.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Spacing.xl))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite)
            ) {
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
                    Spacer(Modifier.height(Spacing.lg))

                    SettingsRow(
                        icon = {
                            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(24.dp), tint = FactLensColors.primary)
                        },
                        title = "Overlay Permission",
                        desc = if (hasOverlayPermission) "Granted \u2713" else "Allow AntiTimpa to show the floating button",
                        done = hasOverlayPermission,
                        onAction = onRequestOverlay
                    )
                    Spacer(Modifier.height(Spacing.md))

                    SettingsToggleRow(
                        icon = {
                            Icon(
                                if (overlayVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (overlayVisible) FactLensColors.primary else FactLensColors.neutralGray
                            )
                        },
                        title = "Show Floating Button",
                        desc = if (overlayVisible) "Floating button is visible on screen" else "Floating button is hidden",
                        checked = overlayVisible,
                        onToggle = onToggleOverlay
                    )
                    Spacer(Modifier.height(Spacing.md))

                    SettingsRow(
                        icon = {
                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(24.dp), tint = FactLensColors.primary)
                        },
                        title = "Gemini API Key",
                        desc = "Add API key for AI-powered verification",
                        done = false,
                        onAction = { }
                    )
                    Spacer(Modifier.height(Spacing.md))

                    SettingsRow(
                        icon = {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(24.dp), tint = FactLensColors.errorRed)
                        },
                        title = "Clear History",
                        desc = "Delete all scan history and saved results",
                        done = false,
                        onAction = { }
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            AppVersionFooter()
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}