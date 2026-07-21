package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.history.HistoryDatabase
import com.factlens.model.ScanHistory
import com.factlens.ui.components.QuickScanBanner
import com.factlens.ui.components.SectionHeader
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onQuickScan: () -> Unit,
    onScanResult: () -> Unit,
    onViewAllHistory: () -> Unit,
    onViewAllSaved: () -> Unit = {},
    onHistoryItemClick: (ScanHistory) -> Unit = {}
) {
    val context = LocalContext.current
    val dao = remember { HistoryDatabase.getInstance(context).historyDao() }
    val allHistory by dao.getAll().collectAsState(initial = emptyList())
    val favorites by dao.getFavorites().collectAsState(initial = emptyList())
    val recentScans = remember(allHistory) { allHistory.take(2) }
    val recentFavorites = remember(favorites) { favorites.take(2) }
    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(FactLensColors.backgroundAlmostWhite)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm).height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(24.dp), tint = FactLensColors.primary)
            Spacer(Modifier.width(Spacing.md))
            Text("AntiTimpa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(FactLensColors.surfaceContainerHigh).padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(FactLensColors.successEmerald))
                Spacer(Modifier.width(Spacing.xs))
                Text("Ready", fontSize = 12.sp, color = FactLensColors.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))
            QuickScanBanner(onQuickScan)
            Spacer(Modifier.height(Spacing.xl))
            SectionHeader("Recent Scans", "View All", onViewAllHistory)
            Spacer(Modifier.height(Spacing.md))
            if (recentScans.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada scan.\nTekan & tahan tombol F (2 detik) di layar mana pun\nuntuk mulai scan.", color = FactLensColors.neutralGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            } else {
                recentScans.forEach { item ->
                    RecentScanCard(
                        verdict = item.verdict,
                        claim = item.claim,
                        detail = item.explanation,
                        time = timeFmt.format(Date(item.timestamp)),
                        isSupported = item.verdict.contains("supported", true) || item.verdict.contains("verified", true),
                        onClick = { onHistoryItemClick(item) }
                    )
                    Spacer(Modifier.height(Spacing.md))
                }
            }
            Spacer(Modifier.height(Spacing.xl))
            SectionHeader("Saved Results", "View All", onViewAllSaved)
            Spacer(Modifier.height(Spacing.md))
            if (recentFavorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada hasil tersimpan.\nSimpan hasil scan untuk dilihat di sini.", color = FactLensColors.neutralGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    recentFavorites.forEach { item ->
                        val conf = "${(item.confidence * 100).toInt()}% Confidence"
                        SavedResultCard(title = item.claim, confidence = conf, modifier = Modifier.weight(1f), onClick = { onHistoryItemClick(item) })
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}
