package com.factlens.overlay

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.factlens.PermissionActivity
import com.factlens.capture.ScreenCaptureManager
import com.factlens.capture.ScreenCaptureService

private const val TAG = "FactLens.Overlay"

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var pulseAnimator: ValueAnimator? = null
    private var longPressHandler: Handler? = null
    private var scanningTimeout: Handler? = null
    private var isActivated = false
    private var pulseView: View? = null
    private var iconView: TextView? = null
    private var scanningIndicator: View? = null

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            scanningTimeout?.removeCallbacksAndMessages(null)
            when (intent.action) {
                "com.factlens.SCAN_COMPLETE" -> {
                    Log.d(TAG, "SCAN_COMPLETE received, hiding scanning indicator")
                    hideScanningIndicator()
                }
                "com.factlens.PERMISSION_DENIED" -> {
                    Log.d(TAG, "PERMISSION_DENIED received, hiding scanning indicator")
                    hideScanningIndicator()
                    showPermissionRetry()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService onCreate")
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager ?: run {
            Log.e(TAG, "Failed to get WindowManager")
            return
        }
        createNotificationChannel()
        val filter = IntentFilter().apply {
            addAction("com.factlens.SCAN_COMPLETE")
            addAction("com.factlens.PERMISSION_DENIED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(overlayReceiver, filter)
        }
        hideScanningCallback = { hideScanningIndicator() }
        Log.d(TAG, "Broadcast receivers registered")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "OverlayService onStartCommand, flags=$flags, startId=$startId")
        if (!::windowManager.isInitialized) {
            Log.e(TAG, "WindowManager not initialized, stopping self")
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = createNotification()
        try {
            startForeground(1, notification)
            Log.d(TAG, "Started as foreground service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
        }
        showOverlay()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FactLens Overlay",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.createNotificationChannel(channel)
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

    private fun showOverlay() {
        Log.d(TAG, "Creating overlay FAB...")
        val sizePx = (56 * resources.displayMetrics.density).toInt()

        pulseView = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x4D00497D.toInt())
            }
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        iconView = TextView(this).apply {
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

        val container = FrameLayout(this).apply {
            addView(pulseView)
            addView(iconView)
        }

        val animator = ValueAnimator.ofFloat(0.7f, 1.4f).apply {
            duration = 2500
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { anim ->
                val scale = anim.animatedValue as Float
                pulseView?.scaleX = scale
                pulseView?.scaleY = scale
                pulseView?.alpha = 1f - ((scale - 0.7f) / 0.7f) * 0.8f
            }
        }
        pulseAnimator = animator
        animator.start()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        if (::overlayView.isInitialized) {
            try { windowManager.removeView(overlayView) } catch (_: Exception) {}
            overlayView = container
        }
        try {
            windowManager.addView(container, params)
            Log.d(TAG, "Overlay FAB added to window at position (${params.x}, ${params.y})")
            setupDraggable(container, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay to window: ${e.message}", e)
            stopSelf()
        }
    }

    private fun setupDraggable(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    isActivated = false
                    longPressHandler?.removeCallbacksAndMessages(null)
                    longPressHandler = Handler(Looper.getMainLooper())
                    longPressHandler?.postDelayed({
                        isActivated = true
                        activateFab()
                    }, 2000L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)) {
                        isDragging = true
                        longPressHandler?.removeCallbacksAndMessages(null)
                        if (isActivated) {
                            isActivated = false
                            deactivateFab()
                        }
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    if (isActivated) {
                        isActivated = false
                        deactivateFab()
                        triggerCapture()
                    }
                    true
                }
                else -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    true
                }
            }
        }
    }

    private fun activateFab() {
        Log.d(TAG, "FAB activated (long press detected)")
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.OVAL
        gd.setColor(0x4D00AA44.toInt())
        pulseView?.setBackground(gd)
        iconView?.setTextColor(0xFF00AA44.toInt())
        vibrate()
    }

    private fun deactivateFab() {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.OVAL
        gd.setColor(0x4D00497D.toInt())
        pulseView?.setBackground(gd)
        iconView?.setTextColor(0xFFFFFFFF.toInt())
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                (getSystemService(VIBRATOR_SERVICE) as? Vibrator)?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {}
    }

    private fun triggerCapture() {
        Log.d(TAG, "Trigger capture requested")
        ScreenCaptureManager.clearProjection()
        showScanningIndicator()
        Log.d(TAG, "Launching transparent PermissionActivity for fresh projection")
        val intent = Intent(this, PermissionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun showScanningIndicator() {
        hideScanningIndicator()
        val density = resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val container = LinearLayout(this).apply {
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

        val spinner = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        val label = TextView(this).apply {
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
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            y = px(80)
        }

        scanningIndicator = container
        try {
            windowManager.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        scanningTimeout?.removeCallbacksAndMessages(null)
        scanningTimeout = Handler(Looper.getMainLooper())
        scanningTimeout?.postDelayed({
            Log.w(TAG, "Scanning timeout after 30s, hiding indicator")
            hideScanningIndicator()
        }, 30000L)
    }

    private fun hideScanningIndicator() {
        scanningIndicator?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            scanningIndicator = null
        }
    }

    private fun showPermissionRetry() {
        val density = resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xE6CC3333.toInt())
                setCornerRadius(px(12).toFloat())
            }
            background = bg
            setPadding(px(16), px(12), px(16), px(12))
            setOnClickListener {
                try { windowManager.removeView(this) } catch (_: Exception) {}
                scanningTimeout?.removeCallbacksAndMessages(null)
                triggerCapture()
            }
        }
        val label = TextView(this).apply {
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
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            y = px(80)
        }
        scanningIndicator = container
        try { windowManager.addView(container, params) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        Log.d(TAG, "OverlayService onDestroy")
        hideScanningCallback = null
        pulseAnimator?.cancel()
        longPressHandler?.removeCallbacksAndMessages(null)
        scanningTimeout?.removeCallbacksAndMessages(null)
        hideScanningIndicator()
        try {
            unregisterReceiver(overlayReceiver)
        } catch (_: Exception) {}
        if (::overlayView.isInitialized) {
            try {
                windowManager.removeView(overlayView)
            } catch (_: Exception) {}
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "factlens_overlay"
        var hideScanningCallback: (() -> Unit)? = null
    }
}
