package com.factlens.capture

import android.content.Context
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream

private const val TAG = "FactLens.CaptureHelper"

class ScreenCaptureHelper(
    private val context: Context,
    private val mediaProjection: MediaProjection,
    private val onImageSaved: (String) -> Unit
) {
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    fun startCapture(handler: Handler) {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        wm.defaultDisplay.getRealMetrics(metrics)

        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2)

        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { release() }
        }, handler)

        imageReader?.setOnImageAvailableListener({ reader ->
            try {
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                processAndSaveImage(image)
            } catch (e: Exception) {
                Log.e(TAG, "Image capture failed: ${e.message}")
            }
        }, handler)

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, handler
        )

        handler.postDelayed({
            val image = imageReader?.acquireLatestImage()
            if (image != null) processAndSaveImage(image)
        }, 500)
    }

    private fun processAndSaveImage(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride

        val bitmap = Bitmap.createBitmap(rowStride / pixelStride, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        val cropped = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
        image.close()
        bitmap.recycle()

        val file = File(context.cacheDir, "factlens_screenshot_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> cropped.compress(Bitmap.CompressFormat.PNG, 90, out) }
        Log.d(TAG, "Screenshot saved: ${file.absolutePath} (${file.length()} bytes)")
        cropped.recycle()

        onImageSaved(file.absolutePath)
    }

    fun release() {
        virtualDisplay?.release()
        imageReader?.close()
    }
}
