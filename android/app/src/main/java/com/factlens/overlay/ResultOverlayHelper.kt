package com.factlens.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.factlens.model.Source
import com.factlens.ui.screens.FloatingResultOverlay
import com.factlens.ui.theme.FactLensTheme

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

        if (context !is Activity) return

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val composeView = ComposeView(context).apply {
            setContent {
                FactLensTheme {
                    FloatingResultOverlay(
                    verdict = verdict,
                    confidence = confidence,
                    explanation = explanation,
                    hasDetail = sources.isNotEmpty(),
                    onViewDetails = {
                        val intent = context.packageManager.getLaunchIntentForPackage(
                            context.packageName
                        )
                        if (intent != null) {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra("open_scan_result", true)
                            context.startActivity(intent)
                        }
                        dismiss()
                    },
                    onBookmark = { /* TODO: toggle favorite */ },
                    onDismiss = { dismiss() }
                )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
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
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        currentView = composeView
        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            currentView = null
        }
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
