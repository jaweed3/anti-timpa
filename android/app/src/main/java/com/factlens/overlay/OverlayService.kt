package com.factlens.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.factlens.MainActivity
import com.factlens.capture.ScreenCaptureManager
import com.factlens.ocr.OCRProcessor
import java.io.File
import java.io.FileOutputStream

private const val TAG = "FactLens.Overlay"

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var fabHelper: OverlayFabHelper? = null
    private var indicatorHelper: OverlayIndicatorHelper? = null
    private var overlayView: android.view.View? = null
    private var overlayCreated = false
    private var overlayShown = false

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureHandlerThread: HandlerThread? = null
    private var captureHandler: Handler? = null
    private var displayMetrics: DisplayMetrics? = null
    private var captureTimeout: Handler? = null

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Broadcast received: action=${intent.action}")
            when (intent.action) {
                "com.factlens.SCAN_COMPLETE" -> indicatorHelper?.hideScanningIndicator()
                "com.factlens.PERMISSION_DENIED" -> {
                    indicatorHelper?.hideScanningIndicator()
                    indicatorHelper?.showPermissionRetry { triggerCapture() }
                }
                "com.factlens.START_CAPTURE" -> {
                    Log.d(TAG, "START_CAPTURE received, calling startCapture()")
                    startCapture()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        createNotificationChannel()
        registerReceiver()
        toggleOverlayCallback = { visible -> updateOverlayVisibility(visible) }
        hideScanningCallback = { indicatorHelper?.hideScanningIndicator() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::windowManager.isInitialized) { stopSelf(); return START_NOT_STICKY }
        startForeground(1, createNotification())
        if (!overlayCreated) {
            showOverlay()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        fabHelper = OverlayFabHelper(this).apply {
            onTriggerCapture = this@OverlayService::triggerCapture
        }
        indicatorHelper = OverlayIndicatorHelper(this)

        val container = fabHelper!!.createOverlayView()
        val params = fabHelper!!.getLayoutParams() ?: return

        fabHelper!!.setupDraggable(container, params)
        overlayView = container
        overlayCreated = true

        try {
            windowManager.addView(container, params)
            overlayShown = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay: ${e.message}")
            stopSelf()
        }

        val prefs = getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("overlay_visible", true)) {
            hideOverlayView()
        }
    }

    private fun showOverlayView() {
        if (overlayShown) return
        val container = overlayView ?: return
        val params = fabHelper?.getLayoutParams() ?: return
        try {
            windowManager.addView(container, params)
            overlayShown = true
        } catch (_: Exception) {}
    }

    private fun hideOverlayView() {
        if (!overlayShown) return
        val container = overlayView ?: return
        try {
            windowManager.removeView(container)
            overlayShown = false
        } catch (_: Exception) {}
    }

    private fun updateOverlayVisibility(visible: Boolean) {
        if (visible) showOverlayView() else hideOverlayView()
    }

    private fun triggerCapture() {
        if (ScreenCaptureManager.hasProjection()) {
            Log.d(TAG, ">>> triggerCapture() - projection exists, capturing directly")
            startCapture()
            return
        }
        Log.d(TAG, ">>> triggerCapture() - no projection, launching MainActivity")
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("trigger_capture", true)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, ">>> FAILED to launch MainActivity: ${e.message}", e)
        }
    }

    private fun ensureProjectionAndDisplay(): Boolean {
        if (mediaProjection != null && virtualDisplay != null) return true

        if (mediaProjection == null) {
            val code = ScreenCaptureManager.getStoredCode()
            val data = ScreenCaptureManager.getStoredData()
            if (code == 0 || data == null) return false

            val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: return false
            mediaProjection = manager.getMediaProjection(code, data) ?: return false

            mediaProjection!!.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped by system — clearing")
                    releaseProjection()
                    ScreenCaptureManager.clearProjection()
                }
            }, null)
        }

        if (virtualDisplay == null && mediaProjection != null) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            displayMetrics = metrics

            captureHandlerThread = HandlerThread("ScreenCapture").apply { start() }
            captureHandler = Handler(captureHandlerThread!!.looper)

            imageReader = ImageReader.newInstance(
                metrics.widthPixels, metrics.heightPixels,
                android.graphics.PixelFormat.RGBA_8888, 2
            )

            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "ScreenCapture",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface, null, captureHandler
            )
        }

        return mediaProjection != null && virtualDisplay != null
    }

    private fun startCapture() {
        Log.d(TAG, ">>> startCapture() called")
        ScreenBlurOverlay.showScanningOverlay(this)

        if (!ensureProjectionAndDisplay()) {
            Log.e(TAG, "No projection available, re-requesting")
            ScreenBlurOverlay.dismiss()
            triggerCapture()
            return
        }

        captureTimeout?.removeCallbacksAndMessages(null)
        captureTimeout = Handler(Looper.getMainLooper())
        captureTimeout?.postDelayed({
            Log.w(TAG, "Capture timeout after 30s — dismissing blur")
            ScreenBlurOverlay.dismiss()
        }, 30000L)

        var captured = false

        val completeCapture = { image: Image? ->
            captureTimeout?.removeCallbacksAndMessages(null)
            if (image != null) {
                processAndSaveImage(image)
            } else {
                Log.w(TAG, "No image captured")
                ScreenBlurOverlay.dismiss()
            }
        }

        imageReader?.setOnImageAvailableListener({ reader ->
            if (captured) return@setOnImageAvailableListener
            captured = true
            Log.d(TAG, "Image captured via listener")
            completeCapture(try { reader.acquireLatestImage() } catch (e: Exception) { Log.e(TAG, "Listener capture failed: ${e.message}"); null })
        }, captureHandler)

        captureHandler?.postDelayed({
            if (captured) return@postDelayed
            captured = true
            Log.d(TAG, "Image captured via delayed fallback")
            completeCapture(try { imageReader?.acquireLatestImage() } catch (e: Exception) { Log.e(TAG, "Delayed capture failed: ${e.message}"); null })
        }, 500)
    }

    private fun processAndSaveImage(image: Image) {
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride

            val bitmap = android.graphics.Bitmap.createBitmap(rowStride / pixelStride, image.height, android.graphics.Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            val cropped = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            image.close()
            bitmap.recycle()

            ScreenBlurOverlay.updateToBlurredScreenshot(this, cropped)

            val screenshotDir = File(filesDir, "screenshots")
            if (!screenshotDir.exists()) screenshotDir.mkdirs()
            val file = File(screenshotDir, "factlens_screenshot_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out -> cropped.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, out) }
            Log.d(TAG, "Screenshot saved: ${file.absolutePath} (${file.length()} bytes)")
            cropped.recycle()

            val ocrIntent = Intent(this, OCRProcessor::class.java)
            ocrIntent.putExtra("image_path", file.absolutePath)
            startService(ocrIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image: ${e.message}")
            ScreenBlurOverlay.dismiss()
        }
    }

    private fun releaseProjection() {
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { imageReader?.close() } catch (_: Exception) {}
        captureHandlerThread?.quitSafely()
        captureTimeout?.removeCallbacksAndMessages(null)
        mediaProjection?.stop()
        mediaProjection = null
        virtualDisplay = null
        imageReader = null
        captureHandler = null
        captureHandlerThread = null
        displayMetrics = null
        captureTimeout = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "AntiTimpa Overlay", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AntiTimpa")
            .setContentText("Hold the F button to check for scams")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.factlens.SCAN_COMPLETE")
            addAction("com.factlens.PERMISSION_DENIED")
            addAction("com.factlens.START_CAPTURE")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(overlayReceiver, filter)
        }
    }

    override fun onDestroy() {
        releaseProjection()
        fabHelper?.destroy()
        indicatorHelper?.destroy()
        toggleOverlayCallback = null
        hideScanningCallback = null
        hideOverlayView()
        try { unregisterReceiver(overlayReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_overlay"
        var toggleOverlayCallback: ((Boolean) -> Unit)? = null
        var hideScanningCallback: (() -> Unit)? = null
    }
}
