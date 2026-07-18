package com.factlens.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.factlens.overlay.OverlayService
import com.factlens.capture.ScreenCaptureManager
import java.io.File
import java.io.FileOutputStream

private const val TAG = "FactLens.Capture"

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var captureCallback: ((Bitmap) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "ScreenCaptureService onStartCommand")
        val notification = createNotification()
        try {
            startForeground(2, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "FGS mediaProjection permission not granted: ${e.message}")
            Toast.makeText(this, "Screen capture permission error. Reinstall app.", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }

        val code = intent?.getIntExtra("code", -1) ?: -1
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Intent>("data")
        }

        val effectiveCode = if (code != -1) code else ScreenCaptureManager.getStoredCode()
        val effectiveData = if (data != null) data else ScreenCaptureManager.getStoredData()

        Log.d(TAG, "Projection code=$effectiveCode, data=${effectiveData != null}")

        if (effectiveCode != 0 && effectiveData != null) {
            try {
                startProjection(effectiveCode, effectiveData)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start projection: ${e.message}", e)
                stopSelf()
            }
        } else {
            Log.e(TAG, "Invalid projection params (code=$effectiveCode, data=$effectiveData), stopping")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Screen Capture",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FactLens")
            .setContentText("Capturing screen...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startProjection(code: Int, data: Intent) {
        Log.d(TAG, "Starting screen projection...")
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: run {
            Log.e(TAG, "Failed to get MediaProjectionManager")
            return
        }
        mediaProjection = manager.getMediaProjection(code, data)
        Log.d(TAG, "MediaProjection created: ${mediaProjection != null}")

        val metrics = DisplayMetrics()
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        wm.defaultDisplay.getRealMetrics(metrics)

        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        Log.d(TAG, "Screen metrics: ${width}x${height} @ ${density}dpi")

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        Log.d(TAG, "ImageReader created: ${width}x${height}")

        handlerThread = HandlerThread("ScreenCapture").apply { start() }
        val handler = handlerThread?.looper?.let { Handler(it) } ?: return

        // MUST register callback before createVirtualDisplay (required on newer Android)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.d(TAG, "MediaProjection stopped by system")
                stopSelf()
            }
        }, handler)

        // Set up listener to capture when first frame is available
        imageReader?.setOnImageAvailableListener({ reader ->
            Log.d(TAG, "Image available from ImageReader")
            try {
                val image = reader.acquireLatestImage() ?: run {
                    Log.w(TAG, "acquireLatestImage returned null, retrying...")
                    return@setOnImageAvailableListener
                }
                Log.d(TAG, "Image acquired: ${image.width}x${image.height}")
                processImage(image)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to acquire image from listener: ${e.message}", e)
            }
        }, handler)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )
        Log.d(TAG, "VirtualDisplay created: ${virtualDisplay != null}")

        // Fallback: try polling after a short delay in case listener doesn't fire
        handler.postDelayed({
            if (virtualDisplay != null) {
                Log.d(TAG, "Fallback: polling for image...")
                try {
                    val image = imageReader?.acquireLatestImage()
                    if (image != null) {
                        processImage(image)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fallback capture failed: ${e.message}")
                }
            }
        }, 500)
    }

    private fun processImage(image: android.media.Image) {
        Log.d(TAG, "Processing image: ${image.width}x${image.height}")
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride

        val bitmap = Bitmap.createBitmap(
            rowStride / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        val cropped = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
        Log.d(TAG, "Bitmap created: ${cropped.width}x${cropped.height}")

        image.close()
        bitmap.recycle()

        saveAndProcess(cropped)
    }

    private fun saveAndProcess(bitmap: Bitmap) {
        val cacheDir = cacheDir
        val file = File(cacheDir, "factlens_screenshot_${System.currentTimeMillis()}.png")
        Log.d(TAG, "Saving screenshot to: ${file.absolutePath}")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        Log.d(TAG, "Screenshot saved (${file.length()} bytes)")

        Log.d(TAG, "Starting OCRProcessor with image...")
        val intent = Intent(this, com.factlens.ocr.OCRProcessor::class.java)
        intent.putExtra("image_path", file.absolutePath)
        startService(intent)

        Log.d(TAG, "Screen capture complete, stopping service")
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "ScreenCaptureService onDestroy, releasing resources")
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
        handlerThread?.quitSafely()
        ScreenCaptureManager.clearProjection()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_capture"
    }
}
