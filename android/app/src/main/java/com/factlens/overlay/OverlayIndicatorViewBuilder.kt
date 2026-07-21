package com.factlens.overlay

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

object OverlayIndicatorViewBuilder {

    fun createScanningIndicator(context: Context): Pair<View, WindowManager.LayoutParams> {
        val density = context.resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xE6000000.toInt())
                setCornerRadius(px(12).toFloat())
            }
            background = bg
            setPadding(px(16), px(12), px(16), px(12))
        }

        val spinner = ProgressBar(context, null, android.R.attr.progressBarStyleSmall)
        val label = TextView(context).apply {
            text = "FactLens Scanning..."
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(px(8), 0, 0, 0)
        }

        container.addView(spinner)
        container.addView(label)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            y = px(80)
        }

        return Pair(container, params)
    }

    fun createPermissionRetryIndicator(context: Context, onRetry: () -> Unit): Pair<View, WindowManager.LayoutParams> {
        val density = context.resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xE6CC3333.toInt())
                setCornerRadius(px(12).toFloat())
            }
            background = bg
            setPadding(px(16), px(12), px(16), px(12))
            setOnClickListener { onRetry() }
        }

        val label = TextView(context).apply {
            text = "Permission required — tap to retry"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
        }
        container.addView(label)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            y = px(80)
        }

        return Pair(container, params)
    }
}
