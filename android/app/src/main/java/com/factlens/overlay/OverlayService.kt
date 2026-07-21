package com.factlens.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.factlens.PermissionActivity
import com.factlens.capture.ScreenCaptureManager

private const val TAG = "FactLens.Overlay"

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var fabHelper: OverlayFabHelper? = null
    private var indicatorHelper: OverlayIndicatorHelper? = null
    private var overlayView: android.view.View? = null
    private var overlayCreated = false
    private var overlayShown = false

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "com.factlens.SCAN_COMPLETE" -> indicatorHelper?.hideScanningIndicator()
                "com.factlens.PERMISSION_DENIED" -> {
                    indicatorHelper?.hideScanningIndicator()
                    indicatorHelper?.showPermissionRetry { triggerCapture() }
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
        ScreenCaptureManager.clearProjection()
        indicatorHelper?.showScanningIndicator()
        val intent = Intent(this, PermissionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "FactLens Overlay", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FactLens")
            .setContentText("Hold the F button to verify information")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.factlens.SCAN_COMPLETE")
            addAction("com.factlens.PERMISSION_DENIED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(overlayReceiver, filter)
        }
    }

    override fun onDestroy() {
        fabHelper?.destroy()
        indicatorHelper?.destroy()
        toggleOverlayCallback = null
        hideOverlayView()
        try { unregisterReceiver(overlayReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_overlay"
        var toggleOverlayCallback: ((Boolean) -> Unit)? = null
    }
}
