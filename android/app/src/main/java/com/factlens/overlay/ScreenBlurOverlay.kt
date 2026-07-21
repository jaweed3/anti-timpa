package com.factlens.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "AntiTimpa.Blur"

object ScreenBlurOverlay {

    private var currentView: View? = null
    private var blurredBitmap: Bitmap? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingShow = AtomicBoolean(false)

    fun showBlur(context: Context, screenshot: Bitmap) {
        val blurred = blurBitmap(screenshot, 25f)

        val imageView = ImageView(context).apply {
            setImageBitmap(blurred)
            scaleType = ImageView.ScaleType.FIT_XY
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
        }

        pendingShow.set(true)
        mainHandler.post {
            if (!pendingShow.getAndSet(false)) {
                if (!blurred.isRecycled) blurred.recycle()
                return@post
            }
            try {
                dismissInternal()
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (wm != null) {
                    wm.addView(imageView, params)
                    currentView = imageView
                    blurredBitmap = blurred
                    Log.d(TAG, "Blur overlay shown")
                } else {
                    if (!blurred.isRecycled) blurred.recycle()
                    Log.e(TAG, "WindowManager is null")
                }
            } catch (e: Exception) {
                if (!blurred.isRecycled) blurred.recycle()
                Log.e(TAG, "Failed to show blur overlay: ${e.message}")
            }
        }
    }

    fun dismiss() {
        pendingShow.set(false)
        mainHandler.post { dismissInternal() }
    }

    private fun dismissInternal() {
        currentView?.let { view ->
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                wm?.removeView(view)
            } catch (_: Exception) {}
            currentView = null
        }
        blurredBitmap?.let {
            if (!it.isRecycled) it.recycle()
            blurredBitmap = null
        }
    }

    private fun blurBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val scale = 0.25f
        val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)

        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        if (!scaled.isRecycled) scaled.recycle()
        return output
    }
}
