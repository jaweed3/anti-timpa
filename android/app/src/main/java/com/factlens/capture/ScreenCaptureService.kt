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
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.factlens.overlay.OverlayService
import com.factlens.capture.ScreenCaptureManager
import java.io.File
import java.io.FileOutputStream

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
        val code = intent?.getIntExtra("code", -1) ?: -1
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Intent>("data")
        }

        val effectiveCode = if (code != -1) code else ScreenCaptureManager.getStoredCode()
        val effectiveData = if (data != null) data else ScreenCaptureManager.getStoredData()

        if (effectiveCode != 0 && effectiveData != null) {
            try {
                val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                if (manager == null) { stopSelf(); return START_NOT_STICKY }
                mediaProjection = manager.getMediaProjection(effectiveCode, effectiveData)

                startForeground(2, createNotification())

                if (mediaProjection != null) {
                    startProjection(mediaProjection!!)
                } else {
                    stopSelf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        } else {
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

    private fun startProjection(projection: MediaProjection) {
        val metrics = DisplayMetrics()
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        wm.defaultDisplay.getRealMetrics(metrics)

        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        handlerThread = HandlerThread("ScreenCapture").apply { start() }
        val handler = handlerThread?.looper?.let { Handler(it) } ?: return

        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopSelf()
            }
        }, handler)

        virtualDisplay = projection.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )

        captureScreenshot(handler)
    }

    private fun captureScreenshot(handler: Handler) {
        handler.post {
            try {
                val image = imageReader?.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * image.width

                    val bitmap = Bitmap.createBitmap(
                        image.width + rowPadding / pixelStride,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    val cropped = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
                    saveAndProcess(cropped)

                    image.close()
                    bitmap.recycle()
                    cropped.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveAndProcess(bitmap: Bitmap) {
        val cacheDir = cacheDir
        val file = File(cacheDir, "factlens_screenshot_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        val intent = Intent(this, com.factlens.ocr.OCRProcessor::class.java)
        intent.putExtra("image_path", file.absolutePath)
        startService(intent)

        stopSelf()
    }

    override fun onDestroy() {
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
        handlerThread?.quitSafely()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_capture"
    }
}
