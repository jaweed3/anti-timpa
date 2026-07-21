package com.factlens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.factlens.model.ScanHistory
import com.factlens.ui.theme.Spacing
import java.util.Date

@Composable
fun HistorySection(
    title: String,
    items: List<ScanHistory>,
    timeFmt: java.text.SimpleDateFormat,
    onItemClick: (ScanHistory) -> Unit
) {
    if (items.isNotEmpty()) {
        SectionHeader(title = title)
        Spacer(Modifier.height(Spacing.md))
        items.forEach { item ->
            HistoryCard(
                claim = item.claim,
                time = timeFmt.format(Date(item.timestamp)),
                verdict = item.verdict,
                sourceLabel = "Confidence: ${(item.confidence * 100).toInt()}%",
                onClick = { onItemClick(item) }
            )
            Spacer(Modifier.height(Spacing.md))
        }
        Spacer(Modifier.height(Spacing.xl))
    }
}
