package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun SetupLogoHeader() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FactLensColors.primary),
        contentAlignment = Alignment.Center
    ) {
        Text("F", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(Spacing.lg))

    Text("FactLens", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
    Spacer(Modifier.height(Spacing.xs))
    Text(
        "Verify information directly from your screen",
        fontSize = 14.sp,
        color = FactLensColors.neutralGray,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SetupStep(number: String, title: String, desc: String, done: Boolean, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (done) FactLensColors.successEmerald else FactLensColors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (done) "\u2713" else number,
                color = if (done) Color.White else FactLensColors.neutralGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
