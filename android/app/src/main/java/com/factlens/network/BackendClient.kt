package com.factlens.network

import android.util.Log
import com.factlens.model.FlaggedItem
import com.factlens.model.ScamCheckResponse
import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "AntiTimpa.Backend"

class BackendClient(
    private val backendUrl: String = VerdictEngine.backendUrl
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun verify(claim: String, sources: List<Source>): VerificationResponse {
        Log.d(TAG, "Calling backend POST $backendUrl/verify")

        val jsonBody = JSONObject().apply {
            put("text", claim)
            put("language", "id")
        }

        val request = Request.Builder()
            .url("$backendUrl/verify")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()

        if (response.code != 200) {
            throw Exception("Backend returned HTTP ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty response body")
        val json = JSONObject(body)

        return VerificationResponse(
            claim = json.optString("claim", claim),
            verdict = json.optString("verdict", "Unknown"),
            confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95),
            explanation = json.optString("explanation", "No explanation."),
            sources = sources
        )
    }

    fun checkScam(text: String): ScamCheckResponse {
        Log.d(TAG, "Calling backend POST $backendUrl/check-scam")

        val jsonBody = JSONObject().apply {
            put("text", text)
        }

        val request = Request.Builder()
            .url("$backendUrl/check-scam")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (response.code != 200) {
            throw Exception("Backend returned HTTP ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty response body")
        val json = JSONObject(body)

        val items = mutableListOf<FlaggedItem>()
        val itemsArray = json.optJSONArray("flagged_items")
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                items.add(FlaggedItem(
                    type = item.optString("type", ""),
                    value = item.optString("value", ""),
                    reason = item.optString("reason", "")
                ))
            }
        }

        val sources = mutableListOf<String>()
        val sourcesArray = json.optJSONArray("sources")
        if (sourcesArray != null) {
            for (i in 0 until sourcesArray.length()) {
                sources.add(sourcesArray.optString(i, ""))
            }
        }

        return ScamCheckResponse(
            verdict = json.optString("verdict", "Unknown"),
            riskScore = json.optInt("risk_score", 0),
            flaggedItems = items,
            explanation = json.optString("explanation", "No explanation."),
            sources = sources,
            shouldBlur = json.optBoolean("should_blur", false)
        )
    }
}
