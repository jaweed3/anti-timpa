package com.factlens.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.factlens.model.FlaggedItem
import com.factlens.model.Source

private const val TAG = "AntiTimpa.ResultOverlay"

object ResultOverlayHelper {

    private var currentView: View? = null

    fun showResult(
        context: Context,
        historyId: Long,
        claim: String,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<Source>,
        flaggedItems: List<FlaggedItem> = emptyList()
    ) {
        Log.d(TAG, "Showing result overlay: verdict=$verdict, confidence=$confidence, sources=${sources.size}")
        dismissInternal()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val wrapper = OverlayViewFactory.createResultOverlayView(
            context = context,
            explanation = explanation,
            verdict = verdict,
            confidence = confidence,
            sources = sources,
            onDismiss = { dismiss() },
            onViewDetails = {
                Log.d(TAG, "View Detail clicked, historyId=$historyId")
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("open_history_detail", true)
                    intent.putExtra("history_id", historyId)
                    intent.putExtra("claim", claim)
                    intent.putExtra("verdict", verdict)
                    intent.putExtra("confidence", confidence)
                    intent.putExtra("explanation", explanation)
                    Log.d(TAG, "Launching app with history_id=$historyId")
                    context.startActivity(intent)
                } else {
                    Log.e(TAG, "getLaunchIntentForPackage returned null!")
                }
                dismiss()
            },
            flaggedItems = flaggedItems
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        currentView = wrapper
        try {
            windowManager.addView(wrapper, params)
            Log.d(TAG, "Result overlay added to window successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add result overlay: ${e.message}", e)
            currentView = null
        }
    }

    fun dismiss() {
        dismissInternal()
        ScreenBlurOverlay.dismiss()
    }

    private fun dismissInternal() {
        currentView?.let { view ->
            Log.d(TAG, "Dismissing result overlay")
            view.context.sendBroadcast(Intent("com.factlens.SCAN_COMPLETE"))
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(view)
                Log.d(TAG, "Result overlay dismissed")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to dismiss overlay: ${e.message}")
            }
            currentView = null
        }
    }
}
