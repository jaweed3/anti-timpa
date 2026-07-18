package com.factlens

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.factlens.capture.ScreenCaptureManager
import com.factlens.capture.ScreenCaptureService

private const val TAG = "FactLens.Permission"

class PermissionActivity : ComponentActivity() {

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            Log.d(TAG, "Screen projection permission GRANTED from transparent activity")
            ScreenCaptureManager.setProjectionResult(result.resultCode, result.data)
            startCaptureService()
        } else {
            Log.w(TAG, "Screen projection permission DENIED from transparent activity")
            sendBroadcast(Intent("com.factlens.PERMISSION_DENIED"))
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "PermissionActivity onCreate - requesting screen capture permission")

        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (manager == null) {
            Log.e(TAG, "Failed to get MediaProjectionManager")
            finish()
            return
        }

        try {
            mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch screen capture intent: ${e.message}", e)
            finish()
        }
    }

    private fun startCaptureService() {
        val intent = Intent(this, ScreenCaptureService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
