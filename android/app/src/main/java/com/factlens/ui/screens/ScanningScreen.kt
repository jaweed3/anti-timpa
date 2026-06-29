package com.factlens.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

@Composable
fun ScanningScreen(
    onScanComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(3000)
        onScanComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.backgroundAlmostWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size((100 * pulseAnim).dp)
                    .clip(CircleShape)
                    .background(FactLensColors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FactLensColors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Scan",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            Text(
                "Scanning...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FactLensColors.onSurface
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Analyzing screen content\nand verifying information",
                fontSize = 14.sp,
                color = FactLensColors.neutralGray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.xxl))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = FactLensColors.primary,
                trackColor = FactLensColors.surfaceContainerHigh
            )
        }
    }
}