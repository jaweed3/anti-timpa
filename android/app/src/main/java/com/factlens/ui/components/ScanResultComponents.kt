package com.factlens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.Spacing
import com.factlens.model.Source

@Composable
fun ClaimInfoCard(claim: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text("Claim", style = MaterialTheme.typography.labelMedium, color = FactLensColors.neutralGray)
            Spacer(Modifier.height(Spacing.xs))
            Text(claim, style = MaterialTheme.typography.bodyLarge, color = FactLensColors.onSurface)
        }
    }
}

@Composable
fun EvidenceSourcesList(sources: List<Source>, onOpenSource: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Evidence Sources", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FactLensColors.onBackground)
        Text("${sources.size} Found", fontSize = 12.sp, color = FactLensColors.primary)
    }
    Spacer(Modifier.height(Spacing.md))
    sources.forEachIndexed { index, source ->
        EvidenceCard(
            title = source.title,
            domain = source.url,
            snippet = source.snippet,
            matchPercent = "${95 - index}% Match",
            onClick = { onOpenSource(source.url) }
        )
        Spacer(Modifier.height(Spacing.md))
    }
}
