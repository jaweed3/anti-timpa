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
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "AntiTimpa.Blur"

object ScreenBlurOverlay {

    private var containerView: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var blurredImageView: ImageView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingShow = AtomicBoolean(false)

    fun showScanningOverlay(context: Context) {
        pendingShow.set(true)
        mainHandler.post {
            if (!pendingShow.getAndSet(false)) return@post
            dismissInternal()

            val progress = ProgressBar(context, null, android.R.attr.progressBarStyleLarge).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }
            progressBar = progress

            val container = FrameLayout(context).apply {
                setBackgroundColor(0x80CC0000.toInt())
                addView(progress)
            }
            containerView = container

            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.addView(container, createLayoutParams())
                Log.d(TAG, "Scanning overlay shown (red + progress)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show scanning overlay: ${e.message}")
                containerView = null
                progressBar = null
            }
        }
    }

    fun updateToBlurredScreenshot(context: Context, bitmap: Bitmap) {
        mainHandler.post {
            val container = containerView ?: return@post

            blurredImageView?.let { container.removeView(it) }

            val blurred = blurBitmap(bitmap, 25f)
            val bgView = ImageView(context).apply {
                setImageBitmap(blurred)
                scaleType = ImageView.ScaleType.FIT_XY
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            blurredImageView = bgView
            container.addView(bgView, 0)

            container.setBackgroundColor(0x00000000.toInt())
            Log.d(TAG, "Blur overlay updated to blurred screenshot")
        }
    }

    fun hideProgress() {
        mainHandler.post {
            progressBar?.let {
                (it.parent as? FrameLayout)?.removeView(it)
                progressBar = null
                Log.d(TAG, "Progress indicator hidden")
            }
        }
    }

    fun dismiss() {
        pendingShow.set(false)
        mainHandler.post { dismissInternal() }
    }

    fun isShowing(): Boolean = containerView != null

    private fun dismissInternal() {
        containerView?.let { view ->
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(view)
                Log.d(TAG, "Blur overlay dismissed")
            } catch (_: Exception) {}
            containerView = null
            progressBar = null
            blurredImageView = null
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
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
