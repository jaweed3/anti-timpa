package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun MainScaffold(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FactLensColors.surfaceContainerLowest,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    selected = currentScreen == "home",
                    onClick = { onNavigate("home") }
                )
                BottomNavItem(
                    icon = Icons.Filled.History,
                    label = "History",
                    selected = currentScreen == "history",
                    onClick = { onNavigate("history") }
                )
                BottomNavItem(
                    icon = Icons.Filled.Bookmark,
                    label = "Saved",
                    selected = currentScreen == "saved",
                    onClick = { onNavigate("saved") }
                )
                BottomNavItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    selected = currentScreen == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) FactLensColors.secondaryContainer else Color.Transparent
    val fg = if (selected) FactLensColors.onSecondaryContainer else FactLensColors.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = fg
        )
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}
