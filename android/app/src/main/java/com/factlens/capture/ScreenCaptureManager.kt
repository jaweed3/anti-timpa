package com.factlens.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build

class ScreenCaptureManager(private val context: Context) {

    interface CaptureCallback {
        fun onCaptureResult(success: Boolean, imagePath: String?)
    }

    companion object {
        private var projectionIntent: Intent? = null
        private var projectionCode: Int = 0
        const val CAPTURE_REQUEST_CODE = 1001

        fun setProjectionResult(code: Int, data: Intent?) {
            projectionCode = code
            projectionIntent = data
        }

        fun getStoredCode(): Int = projectionCode
        fun getStoredData(): Intent? = projectionIntent
        fun hasProjection(): Boolean = projectionIntent != null && projectionCode != 0
    }

    fun requestCapture(activity: Activity) {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: return
        val intent = manager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, 1001)
    }

    fun startCaptureService(activity: Activity) {
        val intent = Intent(activity, ScreenCaptureService::class.java)
        intent.putExtra("code", projectionCode)
        intent.putExtra("data", projectionIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }

    data class CaptureResult(val success: Boolean, val imagePath: String?)

    suspend fun captureScreen(): CaptureResult {
        // ScreenCaptureService handles this and saves to cache
        return CaptureResult(true, null)
    }
}
