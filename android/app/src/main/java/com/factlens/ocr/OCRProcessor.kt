package com.factlens.ocr

import android.app.IntentService
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.factlens.model.VerificationResponse
import com.factlens.network.VerdictEngine
import com.factlens.overlay.ResultOverlayHelper
import kotlinx.coroutines.runBlocking
import java.io.File

private const val TAG = "FactLens.OCR"

class OCRProcessor : IntentService("OCRProcessor") {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "OCR PROCESSING STARTED")
        Log.d(TAG, "═══════════════════════════════════════")

        val imagePath = intent?.getStringExtra("image_path") ?: run {
            Log.e(TAG, "No image_path provided in intent")
            return
        }

        val file = File(imagePath)
        if (!file.exists()) {
            Log.e(TAG, "Image file does not exist: $imagePath")
            return
        }

        val bitmap = BitmapFactory.decodeFile(imagePath) ?: run {
            Log.e(TAG, "Failed to decode bitmap from: $imagePath")
            file.delete()
            return
        }

        val image = InputImage.fromBitmap(bitmap, 0)

        val ocrStartTime = System.currentTimeMillis()
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val ocrElapsed = System.currentTimeMillis() - ocrStartTime
                val text = result.text
                Log.d(TAG, "OCR completed in ${ocrElapsed}ms")

                if (text.isNotBlank()) {
                    verifyText(text)
                } else {
                    Log.w(TAG, "No text detected in image")
                    showResult("", "No text detected.", "Unknown", 0.0, emptyList())
                }
                bitmap.recycle()
                file.delete()
            }
            .addOnFailureListener { e ->
                val ocrElapsed = System.currentTimeMillis() - ocrStartTime
                Log.e(TAG, "OCR FAILED after ${ocrElapsed}ms: ${e.message}", e)
                showResult("", "OCR error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
                bitmap.recycle()
                file.delete()
            }
    }

    private fun verifyText(text: String) {
        val engine = VerdictEngine()
        try {
            val response: com.factlens.model.VerificationResponse = runBlocking { engine.verify(text) }
            showResult(response.claim, response.explanation, response.verdict, response.confidence, response.sources)
            HistorySaver.saveToHistory(this, text, response)
        } catch (e: Exception) {
            Log.e(TAG, "Verification FAILED: ${e.message}", e)
            showResult(text, "Error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
        }
    }

    private fun showResult(
        claim: String,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<com.factlens.model.Source>
    ) {
        try {
            ResultOverlayHelper.showResult(this, claim, explanation, verdict, confidence, sources)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show result overlay: ${e.message}", e)
        }
        ScanNotifier.showScanNotification(this, verdict, confidence)
        val scanComplete = Intent("com.factlens.SCAN_COMPLETE")
        scanComplete.`package` = packageName
        sendBroadcast(scanComplete)
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "OCR PROCESSING COMPLETE")
        Log.d(TAG, "═══════════════════════════════════════")
    }
}
