package com.factlens.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView

data class OverlayFabViews(
    val container: FrameLayout,
    val pulseView: View,
    val iconView: TextView,
    val animator: ValueAnimator
)

object OverlayFabViewFactory {

    fun createOverlayView(context: Context): OverlayFabViews {
        val density = context.resources.displayMetrics.density
        val sizePx = (56 * density).toInt()

        val pulseView = View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x4D00497D.toInt())
            }
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        val iconView = TextView(context).apply {
            text = "F"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF00497D.toInt())
            }
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        val container = FrameLayout(context).apply {
            addView(pulseView)
            addView(iconView)
        }

        val animator = ValueAnimator.ofFloat(0.7f, 1.4f).apply {
            duration = 2500
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { anim ->
                val scale = anim.animatedValue as Float
                pulseView.scaleX = scale
                pulseView.scaleY = scale
                pulseView.alpha = 1f - ((scale - 0.7f) / 0.7f) * 0.8f
            }
        }
        animator.start()

        return OverlayFabViews(container, pulseView, iconView, animator)
    }

    fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
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
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }
    }
}
