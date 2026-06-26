package com.factlens.ocr

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.factlens.network.FactLensApi
import com.factlens.overlay.ResultOverlayHelper
import java.io.File

class OCRProcessor : IntentService("OCRProcessor") {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onHandleIntent(intent: Intent?) {
        val imagePath = intent?.getStringExtra("image_path") ?: return
        val file = File(imagePath)

        if (!file.exists()) return

        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap == null) {
            file.delete()
            return
        }

        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = result.text
                if (text.isNotBlank()) {
                    // Send to backend API for verification
                    verifyText(text)
                } else {
                    showResult("No text detected in the image.", "Unknown", 0.0, emptyList())
                }
                bitmap.recycle()
                file.delete()
            }
            .addOnFailureListener { e ->
                showResult(
                    "Could not read text: ${e.localizedMessage}",
                    "Error",
                    0.0,
                    emptyList()
                )
                bitmap.recycle()
                file.delete()
            }
    }

    private fun verifyText(text: String) {
        val api = FactLensApi.create()
        try {
            val response = api.verifyText(com.factlens.model.VerificationRequest(text)).execute()
            if (response.isSuccessful) {
                val body = response.body()!!
                showResult(body.explanation, body.verdict, body.confidence, body.sources)
                saveToHistory(text, body)
            } else {
                showResult("Backend error: ${response.code()}", "Error", 0.0, emptyList())
            }
        } catch (e: Exception) {
            showResult("Network error: ${e.localizedMessage}", "Error", 0.0, emptyList())
        }
    }

    private fun showResult(
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<com.factlens.model.Source>
    ) {
        ResultOverlayHelper.showResult(this, explanation, verdict, confidence, sources)
    }

    private fun saveToHistory(text: String, response: com.factlens.model.VerificationResponse) {
        val gson = com.google.gson.Gson()
        val history = com.factlens.model.ScanHistory(
            claim = response.claim.ifBlank { text.take(200) },
            verdict = response.verdict,
            confidence = response.confidence,
            explanation = response.explanation,
            sourcesJson = gson.toJson(response.sources)
        )
        val dao = com.factlens.history.HistoryDatabase.getInstance(this).historyDao()
        kotlinx.coroutines.runBlocking { dao.insert(history) }
    }
}
