package com.factlens.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factlens.model.Source

object ResultOverlayHelper {

    private var currentView: ComposeView? = null

    fun showResult(
        context: Context,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<Source>
    ) {
        dismiss()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val composeView = ComposeView(context).apply {
            setContent {
                ResultCard(
                    explanation = explanation,
                    verdict = verdict,
                    confidence = confidence,
                    sources = sources,
                    onDismiss = { dismiss() }
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            dimAmount = 0.3f
            x = 0
            y = 0
        }

        currentView = composeView
        windowManager.addView(composeView, params)
    }

    fun dismiss() {
        currentView?.let { view ->
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(view)
            } catch (_: Exception) {}
            currentView = null
        }
    }
}

@Composable
fun ResultCard(
    explanation: String,
    verdict: String,
    confidence: Double,
    sources: List<Source>,
    onDismiss: () -> Unit
) {
    val verdictColor = when (verdict.lowercase()) {
        "supported" -> Color(0xFF4CAF50)
        "contradicted" -> Color(0xFFF44336)
        "misleading" -> Color(0xFFFF9800)
        "mixed" -> Color(0xFF9C27B0)
        "insufficient evidence" -> Color(0xFF607D8B)
        else -> Color(0xFF9E9E9E)
    }

    val surfaceColor = Color(0xFF1A1A2E)
    val textColor = Color.White
    val secondaryText = Color(0xFFB0B0B0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { /* prevent dismiss on tap */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FactLens", color = Color(0xFF6C63FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "✕",
                    color = secondaryText,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verdict
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(verdictColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = verdict.uppercase(),
                    color = verdictColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(confidence * 100).toInt()}%",
                    color = verdictColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explanation
            Text(
                text = explanation,
                color = textColor,
                fontSize = 14.sp,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // Sources
            if (sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sources", color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                sources.take(3).forEach { source ->
                    Text(
                        text = source.title,
                        color = Color(0xFF6C63FF),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
