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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun SettingsScreen(
    hasOverlayPermission: Boolean,
    onRequestOverlay: () -> Unit
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
                            Icon(
                                Icons.Filled.Security,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = FactLensColors.primary
                            )
                        },
                        title = "Overlay Permission",
                        desc = if (hasOverlayPermission) "Granted \u2713" else "Allow FactLens to show the floating button",
                        done = hasOverlayPermission,
                        onAction = onRequestOverlay
                    )
                    Spacer(Modifier.height(Spacing.md))

                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Filled.Key,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = FactLensColors.primary
                            )
                        },
                        title = "Gemini API Key",
                        desc = "Add API key for AI-powered verification",
                        done = false,
                        onAction = { /* TODO */ }
                    )
                    Spacer(Modifier.height(Spacing.md))

                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = FactLensColors.errorRed
                            )
                        },
                        title = "Clear History",
                        desc = "Delete all scan history and saved results",
                        done = false,
                        onAction = { /* TODO */ }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "FactLens v1.1\nVerify information directly from your screen",
                fontSize = 13.sp,
                color = FactLensColors.neutralGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    desc: String,
    done: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = FactLensColors.onSurface)
            Text(desc, fontSize = 12.sp, color = FactLensColors.neutralGray)
        }
        if (!done) {
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FactLensColors.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Allow", fontSize = 12.sp)
            }
        }
    }
}