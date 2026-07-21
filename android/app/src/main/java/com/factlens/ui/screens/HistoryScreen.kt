package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.factlens.history.HistoryDatabase
import com.factlens.model.ScanHistory
import com.factlens.ui.components.HistoryEmptyState
import com.factlens.ui.components.HistorySearchBar
import com.factlens.ui.components.HistorySection
import com.factlens.ui.components.HistoryTopBar
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onScanResult: () -> Unit,
    onHistoryItemClick: (ScanHistory) -> Unit = {}
) {
    val context = LocalContext.current
    val dao = remember { HistoryDatabase.getInstance(context).historyDao() }
    val allHistory by dao.getAll().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(allHistory, searchQuery) {
        if (searchQuery.isBlank()) allHistory
        else allHistory.filter { it.claim.contains(searchQuery, ignoreCase = true) }
    }

    val grouped = remember(filteredHistory) {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = fmt.format(cal.time)
        val today = mutableListOf<ScanHistory>()
        val yesterday = mutableListOf<ScanHistory>()
        val older = mutableListOf<ScanHistory>()
        for (item in filteredHistory) {
            when (fmt.format(Date(item.timestamp))) {
                todayStr -> today.add(item)
                yesterdayStr -> yesterday.add(item)
                else -> older.add(item)
            }
        }
        Triple(today, yesterday, older)
    }

    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize().background(FactLensColors.backgroundAlmostWhite)
    ) {
        HistoryTopBar()
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Spacing.sm))
            HistorySearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
            Spacer(Modifier.height(Spacing.xl))

            if (filteredHistory.isEmpty()) {
                HistoryEmptyState(searchQuery = searchQuery)
            }

            val (todayItems, yesterdayItems, olderItems) = grouped
            HistorySection(title = "Today", items = todayItems, timeFmt = timeFmt, onItemClick = onHistoryItemClick)
            HistorySection(title = "Yesterday", items = yesterdayItems, timeFmt = timeFmt, onItemClick = onHistoryItemClick)
            HistorySection(title = "Older", items = olderItems, timeFmt = timeFmt, onItemClick = onHistoryItemClick)
        }
    }
}