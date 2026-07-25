package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun FloatingActionMenuContent(
    onScanFullScreen: () -> Unit,
    onSelectArea: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                MenuItem(
                    icon = Icons.Filled.CameraAlt,
                    text = "Scan Full Screen",
                    onClick = onScanFullScreen
                )
                HorizontalDivider(
                    color = FactLensColors.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
                MenuItem(
                    icon = Icons.Filled.Crop,
                    text = "Select Area",
                    onClick = onSelectArea
                )
                HorizontalDivider(
                    color = FactLensColors.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
                MenuItem(
                    icon = Icons.Filled.Settings,
                    text = "Open AntiTimpa",
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FactLensColors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FactLensColors.onSurface
        )
    }
}
