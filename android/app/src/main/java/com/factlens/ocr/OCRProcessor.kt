package com.factlens.ocr

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
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

class OCRProcessor : IntentService("OCRProcessor") {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onHandleIntent(intent: Intent?) {
        val imagePath = intent?.getStringExtra("image_path") ?: return
        val file = File(imagePath)
        if (!file.exists()) return

        val bitmap = BitmapFactory.decodeFile(imagePath) ?: run { file.delete(); return }
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = result.text
                if (text.isNotBlank()) {
                    verifyText(text)
                } else {
                    showResult("No text detected.", "Unknown", 0.0, emptyList())
                }
                bitmap.recycle()
                file.delete()
            }
            .addOnFailureListener { e ->
                showResult("OCR error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
                bitmap.recycle()
                file.delete()
            }
    }

    private fun verifyText(text: String) {
        val engine = VerdictEngine()
        try {
            val response = runBlocking { engine.verify(text) }
            showResult(response.explanation, response.verdict, response.confidence, response.sources)
            saveToHistory(text, response)
        } catch (e: Exception) {
            showResult("Error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
        }
    }

    private fun showResult(
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<com.factlens.model.Source>
    ) {
        try {
            ResultOverlayHelper.showResult(this, explanation, verdict, confidence, sources)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showScanNotification(verdict, confidence)
        sendBroadcast(Intent("com.factlens.SCAN_COMPLETE"))
    }

    private fun showScanNotification(verdict: String, confidence: Double) {
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
    }
}
