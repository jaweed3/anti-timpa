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
import com.factlens.capture.ScreenCaptureManager
import com.factlens.capture.ScreenCaptureService

private const val TAG = "FactLens.Overlay"

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var pulseAnimator: ValueAnimator? = null
    private var menuView: View? = null
    private var longPressHandler: Handler? = null
    private var isActivated = false
    private var pulseView: View? = null
    private var iconView: TextView? = null
    private var scanningIndicator: View? = null

    private val scanCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "SCAN_COMPLETE received, hiding scanning indicator")
            hideScanningIndicator()
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
        val filter = IntentFilter("com.factlens.SCAN_COMPLETE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scanCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(scanCompleteReceiver, filter)
        }
        Log.d(TAG, "SCAN_COMPLETE receiver registered")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "OverlayService onStartCommand, flags=$flags, startId=$startId")
        if (!::windowManager.isInitialized) {
            Log.e(TAG, "WindowManager not initialized, stopping self")
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = createNotification()
        startForeground(1, notification)
        Log.d(TAG, "Started as foreground service")
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

        overlayView = container
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
                        showMenu(params.x, params.y)
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

    private fun showMenu(fabX: Int, fabY: Int) {
        Log.d(TAG, "Showing menu at FAB position ($fabX, $fabY)")
        dismissMenu()

        val bgOverlay = View(this)
        bgOverlay.setBackgroundColor(0x4D000000.toInt())
        bgOverlay.setOnClickListener { dismissMenu() }

        val card = buildMenuCard()

        val container = FrameLayout(this)
        container.addView(bgOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        container.addView(card, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        val menuParams = WindowManager.LayoutParams(
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

        menuView = container
        try {
            windowManager.addView(container, menuParams)
        } catch (e: Exception) {
            e.printStackTrace()
            menuView = null
        }
    }

    private fun buildMenuCard(): LinearLayout {
        val density = resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(android.R.color.transparent)

        val cardBg = GradientDrawable()
        cardBg.shape = GradientDrawable.RECTANGLE
        cardBg.setColor(0xFFFFFFFF.toInt())
        cardBg.setCornerRadius(px(16).toFloat())
        card.background = cardBg
        card.elevation = px(8).toFloat()

        card.addView(buildMenuItem("\uD83D\uDCF7", "Scan Full Screen") {
            dismissMenu(); triggerCapture()
        })
        card.addView(buildDivider())
        card.addView(buildMenuItem("\u2702\uFE0F", "Select Area") {
            dismissMenu(); triggerCapture()
        })
        card.addView(buildDivider())
        card.addView(buildMenuItem("\u2699\uFE0F", "Open FactLens") {
            dismissMenu(); openApp()
        })

        return card
    }

    private fun buildMenuItem(emoji: String, label: String, onClick: () -> Unit): View {
        val density = resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(px(16), px(16), px(16), px(16))
        row.setOnClickListener { onClick() }

        val iconView = TextView(this)
        iconView.text = emoji
        iconView.textSize = 18f
        row.addView(iconView, px(24), px(24))

        val labelView = TextView(this)
        labelView.text = label
        labelView.textSize = 14f
        labelView.setTextColor(0xFF191C20.toInt())
        labelView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        val labelLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        labelLp.leftMargin = px(12)
        row.addView(labelView, labelLp)

        return row
    }

    private fun buildDivider(): View {
        val density = resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val divider = View(this)
        divider.setBackgroundColor(0xFFC1C7D2.toInt())
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        lp.leftMargin = px(16)
        lp.rightMargin = px(16)
        divider.layoutParams = lp
        return divider
    }

    private fun dismissMenu() {
        menuView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            menuView = null
        }
    }

    private fun triggerCapture() {
        Log.d(TAG, "Trigger capture requested")
        if (!ScreenCaptureManager.hasProjection()) {
            Log.d(TAG, "No screen projection available, launching app for permission")
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent?.putExtra("trigger_capture", true)
            startActivity(intent)
            return
        }
        showScanningIndicator()
        Log.d(TAG, "Starting ScreenCaptureService...")
        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun openApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
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
    }

    private fun hideScanningIndicator() {
        scanningIndicator?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            scanningIndicator = null
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "OverlayService onDestroy")
        pulseAnimator?.cancel()
        longPressHandler?.removeCallbacksAndMessages(null)
        dismissMenu()
        hideScanningIndicator()
        try {
            unregisterReceiver(scanCompleteReceiver)
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
    }
}
