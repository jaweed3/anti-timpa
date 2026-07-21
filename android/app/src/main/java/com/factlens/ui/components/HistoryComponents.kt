package com.factlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun HistorySearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp)).background(FactLensColors.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(20.dp), tint = FactLensColors.outline)
            Spacer(Modifier.width(Spacing.md))
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search history...", color = FactLensColors.outline, fontSize = 14.sp) },
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(color = FactLensColors.onSurface, fontSize = 14.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun HistoryEmptyState(searchQuery: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Text(
            if (searchQuery.isNotBlank()) "No results found" else "No scan history yet.\nTap the floating button to start scanning!",
            color = FactLensColors.neutralGray, fontSize = 14.sp, textAlign = TextAlign.Center
        )
    }
}
