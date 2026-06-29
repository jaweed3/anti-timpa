package com.factlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.components.HistoryCard
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing

@Composable
fun HistoryScreen(
    onScanResult: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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
            Text("FactLens", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.Bookmark,
                contentDescription = "Bookmark",
                modifier = Modifier.size(24.dp),
                tint = FactLensColors.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Spacing.sm))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FactLensColors.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp),
                        tint = FactLensColors.outline
                    )
                    Spacer(Modifier.width(Spacing.md))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search history...", color = FactLensColors.outline, fontSize = 14.sp)
                        },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = LocalTextStyle.current.copy(
                            color = FactLensColors.onSurface,
                            fontSize = 14.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            SectionHeaderHistory("Today")
            Spacer(Modifier.height(Spacing.md))

            HistoryCard(
                claim = "\"Studies show that drinking 5 liters of water a day reverses aging by 20 years within a week.\"",
                time = "10:45 AM",
                verdict = "False",
                sourceLabel = "Web verification",
                onClick = onScanResult
            )
            Spacer(Modifier.height(Spacing.md))
            HistoryCard(
                claim = "NASA's Voyager 1 is the first spacecraft to reach interstellar space.",
                time = "9:12 AM",
                verdict = "Verified",
                sourceLabel = "Source: NASA.gov",
                onClick = onScanResult
            )

            Spacer(Modifier.height(Spacing.xl))

            SectionHeaderHistory("Yesterday")
            Spacer(Modifier.height(Spacing.md))

            HistoryCard(
                claim = "New tax law proposed to eliminate all property taxes by 2025.",
                time = "4:30 PM",
                verdict = "Misleading",
                sourceLabel = "Partial context missing",
                onClick = onScanResult
            )
            Spacer(Modifier.height(Spacing.md))
            HistoryCard(
                claim = "Electric vehicles cause more pollution than diesel trucks due to battery manufacturing.",
                time = "2:15 PM",
                verdict = "False",
                sourceLabel = "Scientific consensus",
                onClick = onScanResult
            )
            Spacer(Modifier.height(Spacing.md))
            HistoryCard(
                claim = "Honey never spoils. Archaeologists have found edible honey in ancient Egyptian tombs.",
                time = "10:00 AM",
                verdict = "Verified",
                sourceLabel = "Historical Fact",
                onClick = onScanResult
            )

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun SectionHeaderHistory(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FactLensColors.onSurface
        )
        Spacer(Modifier.width(Spacing.md))
        Divider(
            modifier = Modifier.weight(1f),
            color = FactLensColors.outlineVariant,
            thickness = 1.dp
        )
    }
}