package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanResultBottomBar(onSave: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FactLensColors.surfaceContainerLowest)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FactLensColors.primary,
                contentColor = FactLensColors.onPrimary
            )
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Bookmark", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("Save Result")
        }
        Spacer(Modifier.width(Spacing.md))
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FactLensColors.primary)
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("Share")
        }
    }
}
