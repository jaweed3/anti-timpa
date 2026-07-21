package com.factlens

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.factlens.capture.ScreenCaptureManager
import com.factlens.overlay.OverlayService
import com.factlens.ui.theme.FactLensTheme

private const val TAG = "FactLens.MainActivity"

class MainActivity : ComponentActivity() {

    lateinit var overlayPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    lateinit var mediaProjectionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    var hasScreenRecording by mutableStateOf(ScreenCaptureManager.hasProjection())
        private set
    var navigateToScanResult by mutableStateOf(false)
    var navigateToHistoryDetail by mutableStateOf(false)
    var pendingHistoryDetailId by mutableStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("trigger_capture", false) == true) {
            requestScreenCapture()
        }
        if (intent?.getBooleanExtra("open_scan_result", false) == true) navigateToScanResult = true
        if (intent?.getBooleanExtra("open_history_detail", false) == true) {
            navigateToHistoryDetail = true
            pendingHistoryDetailId = intent.getLongExtra("history_id", -1L)
        }

        overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        mediaProjectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Log.d(TAG, ">>> MediaProjection permission GRANTED")
                ScreenCaptureManager.setProjectionResult(result.resultCode, result.data)
                hasScreenRecording = true
                sendBroadcast(Intent("com.factlens.START_CAPTURE"))
                moveTaskToBack(true)
            } else {
                Toast.makeText(this, "Screen recording permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        }

        setContent { FactLensTheme { AppNavigation(this) } }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.getBooleanExtra("trigger_capture", false) == true) {
            requestScreenCapture()
        }
        if (intent?.getBooleanExtra("open_scan_result", false) == true) navigateToScanResult = true
        if (intent?.getBooleanExtra("open_history_detail", false) == true) {
            navigateToHistoryDetail = true
            pendingHistoryDetailId = intent.getLongExtra("history_id", -1L)
        }
    }

    private fun requestScreenCapture() {
        if (ScreenCaptureManager.hasProjection()) {
            Log.d(TAG, ">>> Already have projection, triggering capture directly")
            sendBroadcast(Intent("com.factlens.START_CAPTURE"))
            moveTaskToBack(true)
            return
        }
        Log.d(TAG, ">>> Launching screen capture intent from MainActivity")
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (manager != null) {
            try {
                mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
            } catch (e: Exception) {
                Log.e(TAG, ">>> Failed to launch screen capture: ${e.message}", e)
            }
        }
    }
}
