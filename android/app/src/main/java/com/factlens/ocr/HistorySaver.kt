package com.factlens.ocr

import android.util.Log
import com.factlens.history.HistoryDatabase
import com.factlens.model.ScanHistory
import com.factlens.model.VerificationResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking

private const val TAG = "FactLens.HistorySaver"

object HistorySaver {

    fun saveToHistory(context: android.content.Context, text: String, response: VerificationResponse): Long {
        Log.d(TAG, "Saving scan to history...")
        val gson = Gson()
        val history = ScanHistory(
            claim = response.claim.ifBlank { text.take(200) },
            verdict = response.verdict,
            confidence = response.confidence,
            explanation = response.explanation,
            sourcesJson = gson.toJson(response.sources)
        )
        val dao = HistoryDatabase.getInstance(context).historyDao()
        val id = runBlocking { dao.insert(history) }
        Log.d(TAG, "Scan saved to history successfully, id=$id")
        return id
    }
}
