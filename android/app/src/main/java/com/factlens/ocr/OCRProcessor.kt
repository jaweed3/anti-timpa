package com.factlens.ocr

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.factlens.model.ScanHistory
import com.factlens.network.VerdictEngine
import com.factlens.overlay.ResultOverlayHelper
import com.google.gson.Gson
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
        Log.d(TAG, "Image path: $imagePath")

        val file = File(imagePath)
        if (!file.exists()) {
            Log.e(TAG, "Image file does not exist: $imagePath")
            return
        }
        Log.d(TAG, "Image file exists, size: ${file.length()} bytes")

        val bitmap = BitmapFactory.decodeFile(imagePath) ?: run {
            Log.e(TAG, "Failed to decode bitmap from: $imagePath")
            file.delete()
            return
        }
        Log.d(TAG, "Bitmap decoded: ${bitmap.width}x${bitmap.height}, config=${bitmap.config}")

        val image = InputImage.fromBitmap(bitmap, 0)
        Log.d(TAG, "InputImage created, starting ML Kit text recognition...")

        val ocrStartTime = System.currentTimeMillis()
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val ocrElapsed = System.currentTimeMillis() - ocrStartTime
                val text = result.text
                Log.d(TAG, "OCR completed in ${ocrElapsed}ms")
                Log.d(TAG, "Text blocks found: ${result.textBlocks.size}")
                Log.d(TAG, "Text lines found: ${result.textBlocks.sumOf { it.lines.size }}")

                if (text.isNotBlank()) {
                    Log.d(TAG, "─── OCR RESULT ───")
                    Log.d(TAG, "Extracted text (${text.length} chars):")
                    text.lines().forEachIndexed { i, line ->
                        Log.d(TAG, "  [${i + 1}] $line")
                    }
                    Log.d(TAG, "─── END OCR RESULT ───")
                    verifyText(text)
                } else {
                    Log.w(TAG, "No text detected in image")
                    showResult("No text detected.", "Unknown", 0.0, emptyList())
                }
                bitmap.recycle()
                file.delete()
                Log.d(TAG, "Temp image file cleaned up")
            }
            .addOnFailureListener { e ->
                val ocrElapsed = System.currentTimeMillis() - ocrStartTime
                Log.e(TAG, "OCR FAILED after ${ocrElapsed}ms: ${e.message}", e)
                showResult("OCR error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
                bitmap.recycle()
                file.delete()
            }
    }

    private fun verifyText(text: String) {
        Log.d(TAG, "Starting verification for text (${text.length} chars)...")
        Log.d(TAG, "Text preview: \"${text.take(100)}${if (text.length > 100) "..." else ""}\"")

        val engine = VerdictEngine()
        val verifyStartTime = System.currentTimeMillis()
        try {
            val response = runBlocking { engine.verify(text) }
            val verifyElapsed = System.currentTimeMillis() - verifyStartTime
            Log.d(TAG, "Verification completed in ${verifyElapsed}ms")
            Log.d(TAG, "─── VERIFICATION RESULT ───")
            Log.d(TAG, "  Verdict: ${response.verdict}")
            Log.d(TAG, "  Confidence: ${(response.confidence * 100).toInt()}%")
            Log.d(TAG, "  Claim: ${response.claim}")
            Log.d(TAG, "  Sources count: ${response.sources.size}")
            Log.d(TAG, "  Explanation: ${response.explanation.take(200)}")
            Log.d(TAG, "─── END VERIFICATION ───")

            showResult(response.explanation, response.verdict, response.confidence, response.sources)
            saveToHistory(text, response)
        } catch (e: Exception) {
            val verifyElapsed = System.currentTimeMillis() - verifyStartTime
            Log.e(TAG, "Verification FAILED after ${verifyElapsed}ms: ${e.message}", e)
            showResult("Error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
        }
    }

    private fun showResult(
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<com.factlens.model.Source>
    ) {
        Log.d(TAG, "Showing result overlay: verdict=$verdict, confidence=$confidence, sources=${sources.size}")
        try {
            ResultOverlayHelper.showResult(this, explanation, verdict, confidence, sources)
            Log.d(TAG, "Result overlay displayed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show result overlay: ${e.message}", e)
        }
        showScanNotification(verdict, confidence)
        sendBroadcast(Intent("com.factlens.SCAN_COMPLETE"))
        Log.d(TAG, "SCAN_COMPLETE broadcast sent")
    }

    private fun showScanNotification(verdict: String, confidence: Double) {
        Log.d(TAG, "Showing scan notification: $verdict (${(confidence * 100).toInt()}%)")
        val channelId = "factlens_scan_result"
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Scan Results",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("FactLens Scan Complete")
            .setContentText("$verdict — ${(confidence * 100).toInt()}% confidence")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveToHistory(text: String, response: com.factlens.model.VerificationResponse) {
        Log.d(TAG, "Saving scan to history...")
        val gson = Gson()
        val history = ScanHistory(
            claim = response.claim.ifBlank { text.take(200) },
            verdict = response.verdict,
            confidence = response.confidence,
            explanation = response.explanation,
            sourcesJson = gson.toJson(response.sources)
        )
        val dao = com.factlens.history.HistoryDatabase.getInstance(this).historyDao()
        runBlocking { dao.insert(history) }
        Log.d(TAG, "Scan saved to history successfully")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "OCR PROCESSING COMPLETE")
        Log.d(TAG, "═══════════════════════════════════════")
    }
}
