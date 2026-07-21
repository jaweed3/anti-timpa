package com.factlens.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager

class OverlayFabTouchHandler(
    private val context: Context,
    private val activator: OverlayFabActivator
) {
    private var isActivated = false
    private var longPressHandler: Handler? = null
    var onTriggerCapture: (() -> Unit)? = null

    fun setupDraggable(view: View, params: WindowManager.LayoutParams) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    isDragging = false; isActivated = false
                    longPressHandler?.removeCallbacksAndMessages(null)
                    longPressHandler = Handler(Looper.getMainLooper())
                    longPressHandler?.postDelayed({
                        isActivated = true
                        activator.activate()
                    }, 2000L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)) {
                        isDragging = true
                        longPressHandler?.removeCallbacksAndMessages(null)
                        if (isActivated) { isActivated = false; activator.deactivate() }
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt(); params.y = initialY + dy.toInt()
                        try { windowManager.updateViewLayout(view, params) } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    if (isDragging) {
                        isDragging = false
                        snapToEdge(view, params)
                    }
                    if (isActivated) {
                        isActivated = false; activator.deactivate()
                        onTriggerCapture?.invoke()
                    }
                    true
                }
                else -> { longPressHandler?.removeCallbacksAndMessages(null); true }
            }
        }
    }

    fun snapToEdge(view: View, params: WindowManager.LayoutParams) {
        try {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val buttonWidth = view.measuredWidth.coerceAtLeast(1)
            val density = displayMetrics.density
            val marginPx = (8 * density).toInt()
            val maxX = screenWidth - buttonWidth - marginPx
            val minX = marginPx

            val buttonCenterX = params.x + buttonWidth / 2
            val snapToRight = buttonCenterX > screenWidth / 2

            params.x = (if (snapToRight) maxX else minX).coerceIn(minX, maxX)

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            try { windowManager.updateViewLayout(view, params) } catch (_: IllegalArgumentException) {}
        } catch (e: Exception) {
            android.util.Log.e("FactLens.Touch", "Snap-to-edge failed: ${e.message}")
        }
    }

    fun destroy() {
        longPressHandler?.removeCallbacksAndMessages(null)
    }
}
