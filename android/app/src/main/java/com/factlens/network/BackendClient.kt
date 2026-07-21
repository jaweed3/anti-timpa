package com.factlens.network

import android.util.Log
import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "FactLens.Backend"

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
}
