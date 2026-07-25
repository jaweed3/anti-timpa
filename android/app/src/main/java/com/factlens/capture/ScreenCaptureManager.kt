package com.factlens.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.util.Log

private const val TAG = "FactLens.CaptureMgr"

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
            Log.d(TAG, "Projection result stored: code=$code, data=${data != null}")
        }

        fun getStoredCode(): Int = projectionCode
        fun getStoredData(): Intent? = projectionIntent
        fun hasProjection(): Boolean {
            val has = projectionIntent != null && projectionCode != 0
            Log.d(TAG, "hasProjection=$has (code=$projectionCode)")
            return has
        }

        fun clearProjection() {
            projectionIntent = null
            projectionCode = 0
            Log.d(TAG, "Projection cleared")
        }
    }

    fun requestCapture(activity: Activity) {
        Log.d(TAG, "Requesting screen capture permission...")
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: run {
            Log.e(TAG, "Failed to get MediaProjectionManager")
            return
        }
        val intent = manager.createScreenCaptureIntent()
        Log.d(TAG, "Launching screen capture intent")
        activity.startActivityForResult(intent, 1001)
    }

    data class CaptureResult(val success: Boolean, val imagePath: String?)

    suspend fun captureScreen(): CaptureResult {
        // ScreenCaptureService handles this and saves to cache
        return CaptureResult(true, null)
    }
}
