package com.factlens.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager

private const val TAG = "FactLens.Indicator"

class OverlayIndicatorHelper(private val context: Context) {

    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var scanningIndicator: View? = null
    var scanningTimeout: Handler? = null

    fun showScanningIndicator() {
        hideScanningIndicator()
        val (view, params) = OverlayIndicatorViewBuilder.createScanningIndicator(context)
        scanningIndicator = view
        try { windowManager.addView(view, params) } catch (e: Exception) { e.printStackTrace() }

        scanningTimeout?.removeCallbacksAndMessages(null)
        scanningTimeout = Handler(Looper.getMainLooper())
        scanningTimeout?.postDelayed({
            Log.w(TAG, "Scanning timeout after 30s")
            hideScanningIndicator()
        }, 30000L)
    }

    fun hideScanningIndicator() {
        scanningIndicator?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            scanningIndicator = null
        }
    }

    fun showPermissionRetry(onRetry: () -> Unit) {
        val (view, params) = OverlayIndicatorViewBuilder.createPermissionRetryIndicator(context) {
            scanningIndicator?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
            scanningTimeout?.removeCallbacksAndMessages(null)
            onRetry()
        }
        scanningIndicator = view
        try { windowManager.addView(view, params) } catch (_: Exception) {}
    }

    fun destroy() {
        scanningTimeout?.removeCallbacksAndMessages(null)
        hideScanningIndicator()
    }
}
