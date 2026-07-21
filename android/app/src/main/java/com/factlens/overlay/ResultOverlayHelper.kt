package com.factlens.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.factlens.model.Source

private const val TAG = "FactLens.ResultOverlay"

object ResultOverlayHelper {

    private var currentView: View? = null

    fun showResult(
        context: Context,
        claim: String,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<Source>
    ) {
        Log.d(TAG, "Showing result overlay: verdict=$verdict, confidence=$confidence, sources=${sources.size}")
        dismiss()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val wrapper = OverlayViewFactory.createResultOverlayView(
            context = context,
            explanation = explanation,
            verdict = verdict,
            confidence = confidence,
            sources = sources,
            onDismiss = { dismiss() },
            onViewDetails = {
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra("open_scan_result", true)
                    intent.putExtra("claim", claim)
                    intent.putExtra("verdict", verdict)
                    intent.putExtra("confidence", confidence)
                    intent.putExtra("explanation", explanation)
                    intent.putExtra("sources", com.google.gson.Gson().toJson(sources))
                    context.startActivity(intent)
                }
                dismiss()
            }
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
