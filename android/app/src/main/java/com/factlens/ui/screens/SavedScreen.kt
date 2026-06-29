package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun SavedScreen(
    onScanResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.backgroundAlmostWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Saved", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.Bookmark,
                contentDescription = "Saved",
                modifier = Modifier.size(24.dp),
                tint = FactLensColors.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Spacing.xl))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = FactLensColors.neutralGray
                )
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    "No saved results yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = FactLensColors.onSurface
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "Save verification results to access them later",
                    fontSize = 14.sp,
                    color = FactLensColors.neutralGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}