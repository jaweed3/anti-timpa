package com.factlens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun HistoryTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm).height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("AntiTimpa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.Bookmark, contentDescription = "Bookmark", modifier = Modifier.size(24.dp), tint = FactLensColors.onSurfaceVariant)
    }
}
