package com.factlens.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.factlens.model.Source
import com.google.gson.Gson

private const val TAG = "FactLens.ResultOverlay"

object ResultOverlayHelper {

    private var currentView: View? = null

    fun showResult(
        context: Context,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<Source>
    ) {
        Log.d(TAG, "Showing result overlay: verdict=$verdict, confidence=$confidence, sources=${sources.size}")
        dismiss()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val density = context.resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xFFFFFFFF.toInt())
                setCornerRadius(px(20).toFloat())
            }
            background = bg
            setPadding(px(20), px(20), px(20), px(20))
            elevation = px(8).toFloat()
        }

        val verdictColor = when (verdict.lowercase()) {
            "supported" -> 0xFF00AA44.toInt()
            "misleading", "unsupported" -> 0xFFDD3333.toInt()
            else -> 0xFFFF8C00.toInt()
        }

        val badge = TextView(context).apply {
            text = verdict.uppercase()
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(verdictColor)
                setCornerRadius(px(6).toFloat())
            }
            background = badgeBg
            setPadding(px(8), px(4), px(8), px(4))
        }
        card.addView(badge)

        val confidenceText = TextView(context).apply {
            text = "${(confidence * 100).toInt()}%"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(verdictColor)
            setPadding(0, px(8), 0, px(4))
        }
        card.addView(confidenceText)

        val verdictLabel = TextView(context).apply {
            text = verdict
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF191C20.toInt())
        }
        card.addView(verdictLabel)

        if (explanation.isNotEmpty()) {
            val explanationView = TextView(context).apply {
                text = explanation
                textSize = 14f
                setTextColor(0xFF5C6168.toInt())
                setPadding(0, px(8), 0, 0)
            }
            card.addView(explanationView)
        }

        val sourcesText = TextView(context).apply {
            text = "${sources.size} sources"
            textSize = 12f
            setTextColor(0xFF999999.toInt())
            setPadding(0, px(8), 0, 0)
        }
        card.addView(sourcesText)

        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, px(12), 0, 0)
        }

        val dismissBtn = Button(context).apply {
            text = "Dismiss"
            setTextColor(0xFF5C6168.toInt())
            textSize = 14f
            background = null
            setOnClickListener { dismiss() }
        }
        buttonRow.addView(dismissBtn)

        val detailBtn = Button(context).apply {
            text = "View Details"
            setTextColor(0xFF00497D.toInt())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            background = null
            setOnClickListener {
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra("open_scan_result", true)
                    intent.putExtra("verdict", verdict)
                    intent.putExtra("confidence", confidence)
                    intent.putExtra("explanation", explanation)
                    intent.putExtra("sources", Gson().toJson(sources))
                    context.startActivity(intent)
                }
                dismiss()
            }
        }
        buttonRow.addView(detailBtn)

        card.addView(buttonRow)

        val wrapper = FrameLayout(context).apply {
            setBackgroundColor(0x4D000000.toInt())
            setOnClickListener { dismiss() }
            addView(card, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ))
        }

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
