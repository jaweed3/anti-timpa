package com.factlens.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

private const val TAG = "FactLens.Capture"

class ScreenCaptureService : Service() {

    private var handlerThread: HandlerThread? = null
    private var captureHelper: ScreenCaptureHelper? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(2, createNotification())

        val code = intent?.getIntExtra("code", -1) ?: -1
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Intent>("data")
        }

        val effectiveCode = if (code != -1) code else ScreenCaptureManager.getStoredCode()
        val effectiveData = if (data != null) data else ScreenCaptureManager.getStoredData()

        if (effectiveCode == 0 || effectiveData == null) {
            Log.e(TAG, "Invalid projection params, stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: run {
            stopSelf(); return START_NOT_STICKY
        }
        val projection = manager.getMediaProjection(effectiveCode, effectiveData)

        handlerThread = HandlerThread("ScreenCapture").apply { start() }
        val handler = Handler(handlerThread!!.looper)

        captureHelper = ScreenCaptureHelper(this, projection, { path ->
            val ocrIntent = Intent(this, com.factlens.ocr.OCRProcessor::class.java)
            ocrIntent.putExtra("image_path", path)
            startService(ocrIntent)
            stopSelf()
        })
        captureHelper!!.startCapture(handler)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Screen Capture", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)
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

    override fun onDestroy() {
        captureHelper?.release()
        handlerThread?.quitSafely()
        ScreenCaptureManager.clearProjection()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_capture"
    }
}
