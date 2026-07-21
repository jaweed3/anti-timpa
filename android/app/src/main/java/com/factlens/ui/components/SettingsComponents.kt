package com.factlens.ui.components

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
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

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

@Composable
fun SettingsToggleRow(
    icon: @Composable () -> Unit,
    title: String,
    desc: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
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
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FactLensColors.primary,
                checkedTrackColor = FactLensColors.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AppVersionFooter() {
    Text(
        "FactLens v1.1\nVerify information directly from your screen",
        fontSize = 13.sp,
        color = FactLensColors.neutralGray,
        textAlign = TextAlign.Center
    )
}
