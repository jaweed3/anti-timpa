package com.factlens.ocr

import android.app.IntentService
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.factlens.model.FlaggedItem
import com.factlens.model.VerificationResponse
import com.factlens.network.VerdictEngine
import com.factlens.overlay.OverlayService
import com.factlens.overlay.ResultOverlayHelper
import com.factlens.overlay.ScreenBlurOverlay
import kotlinx.coroutines.runBlocking
import java.io.File

private const val TAG = "AntiTimpa.OCR"

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

        ScreenBlurOverlay.showBlur(this, bitmap)

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
                    showResult(-1L, "", "No text detected.", "Unknown", 0.0, emptyList(), emptyList())
                }
                bitmap.recycle()
                file.delete()
            }
            .addOnFailureListener { e ->
                val ocrElapsed = System.currentTimeMillis() - ocrStartTime
                Log.e(TAG, "OCR FAILED after ${ocrElapsed}ms: ${e.message}", e)
                showResult(-1L, "", "OCR error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList(), emptyList())
                bitmap.recycle()
                file.delete()
            }
    }

    private fun verifyText(text: String) {
        val engine = VerdictEngine()
        try {
            val response = runBlocking { engine.verify(text) }
            val historyId = HistorySaver.saveToHistory(this, text, response)
            val flaggedItems = parseFlaggedItems(response)

            showResult(historyId, response.claim, response.explanation,
                response.verdict, response.confidence, response.sources, flaggedItems)
        } catch (e: Exception) {
            Log.e(TAG, "Verification FAILED: ${e.message}", e)
            ScreenBlurOverlay.dismiss()
            showResult(-1L, text, "Error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList(), emptyList())
        }
    }

    private fun parseFlaggedItems(response: VerificationResponse): List<FlaggedItem> {
        val items = mutableListOf<FlaggedItem>()
        val lines = response.explanation.split("\n")
        var inFlagged = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "Item terdeteksi:") {
                inFlagged = true
                continue
            }
            if (inFlagged && trimmed.startsWith("\u2022")) {
                val parts = trimmed.removePrefix("\u2022 ").split(" \u2014 ")
                if (parts.size == 2) {
                    items.add(FlaggedItem(type = "detected", value = parts[0], reason = parts[1]))
                }
            }
        }
        return items
    }

    private fun showResult(
        historyId: Long,
        claim: String,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<com.factlens.model.Source>,
        flaggedItems: List<FlaggedItem>
    ) {
        try {
            ResultOverlayHelper.showResult(this, historyId, claim, explanation, verdict,
                confidence, sources, flaggedItems)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show result overlay: ${e.message}", e)
        }
        ScanNotifier.showScanNotification(this, verdict, confidence)
        OverlayService.hideScanningCallback?.invoke()
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "OCR PROCESSING COMPLETE")
        Log.d(TAG, "═══════════════════════════════════════")
    }
}
