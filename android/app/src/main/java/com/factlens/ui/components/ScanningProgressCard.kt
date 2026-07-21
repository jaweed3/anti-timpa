package com.factlens.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun ScanningProgressCard() {
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = FactLensColors.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text("SCANNING SCREEN CONTENT...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(Spacing.md))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(FactLensColors.surfaceContainerHigh)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progressAnim.value).clip(RoundedCornerShape(2.dp)).background(FactLensColors.primary))
            }
            Spacer(Modifier.height(Spacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("FactLens AI v2.4", fontSize = 11.sp, color = FactLensColors.onSurfaceVariant)
                Text("${(progressAnim.value * 100).toInt()}% Analysis Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            }
        }
    }
}
