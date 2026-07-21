package com.factlens.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors

@Composable
fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = FactLensColors.onSurface)
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.labelLarge,
                color = FactLensColors.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}
