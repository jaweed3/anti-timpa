package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.history.HistoryDatabase
import com.factlens.model.ScanHistory
import com.factlens.ui.components.HistoryCard
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedScreen(
    onScanResult: () -> Unit,
    onHistoryItemClick: (ScanHistory) -> Unit = {}
) {
    val context = LocalContext.current
    val dao = remember { HistoryDatabase.getInstance(context).historyDao() }
    val favorites by dao.getFavorites().collectAsState(initial = emptyList())
    val timeFmt = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(FactLensColors.backgroundAlmostWhite)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm).height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Saved", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Bookmark, contentDescription = "Saved", modifier = Modifier.size(24.dp), tint = FactLensColors.primary)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Spacing.lg))

            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(80.dp), tint = FactLensColors.neutralGray)
                        Spacer(Modifier.height(Spacing.lg))
                        Text("No saved results yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = FactLensColors.onSurface)
                        Spacer(Modifier.height(Spacing.sm))
                        Text("Save verification results to access them later", fontSize = 14.sp, color = FactLensColors.neutralGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                favorites.forEach { item ->
                    HistoryCard(
                        claim = item.claim,
                        time = timeFmt.format(Date(item.timestamp)),
                        verdict = item.verdict,
                        sourceLabel = "Confidence: ${(item.confidence * 100).toInt()}%",
                        onClick = { onHistoryItemClick(item) }
                    )
                    Spacer(Modifier.height(Spacing.md))
                }
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}
