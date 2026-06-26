package com.factlens.ocr

import android.app.IntentService
import android.content.Intent
import android.graphics.BitmapFactory
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
                showResult("OCR error: ${e.localizedMessage}", "Error", 0.0, emptyList())
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
            showResult("Error: ${e.localizedMessage}", "Error", 0.0, emptyList())
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
