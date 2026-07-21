package com.factlens.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanResultTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp).clickable { onBack() },
            tint = FactLensColors.primary
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            "Verification Result",
            style = MaterialTheme.typography.titleLarge,
            color = FactLensColors.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(24.dp),
            tint = FactLensColors.onSurfaceVariant
        )
    }
}
