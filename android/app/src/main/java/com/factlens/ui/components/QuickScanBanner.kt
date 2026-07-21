package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun QuickScanBanner(onQuickScan: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FactLensColors.primaryContainer)
            .clickable { onQuickScan() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDEE1\uFE0F", fontSize = 28.sp)
            }
            Spacer(Modifier.height(Spacing.md))
            Text("AntiScam Scanner", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.height(Spacing.xs))
            Text("Tekan & tahan tombol F (2 detik) di app mana pun\nuntuk scan dan blur konten mencurigakan", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.md))
            Button(
                onClick = onQuickScan,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = FactLensColors.primary)
            ) { Text("Coba Scan", fontWeight = FontWeight.Bold) }
        }
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 60.dp, y = (-40).dp)
                .clip(RoundedCornerShape(90.dp))
                .background(FactLensColors.primary.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = 60.dp)
                .clip(RoundedCornerShape(90.dp))
                .background(FactLensColors.secondaryContainer.copy(alpha = 0.2f))
        )
    }
}
