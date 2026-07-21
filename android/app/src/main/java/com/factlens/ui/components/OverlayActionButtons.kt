package com.factlens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun OverlayActionButtons(onViewDetails: () -> Unit, onBookmark: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        PrimaryButton(
            text = "View Full Details",
            onClick = onViewDetails,
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        )
        Spacer(Modifier.width(Spacing.md))
        OutlinedButton(
            onClick = onBookmark,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FactLensColors.onSurfaceVariant)
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Bookmark", modifier = Modifier.size(18.dp))
        }
    }
}
