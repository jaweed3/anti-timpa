package com.factlens.network

import android.util.Log
import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "FactLens.Gemini"

class GeminiClient(
    private val apiKey: String = VerdictEngine.geminiApiKey
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun verify(claim: String, sources: List<Source>): VerificationResponse {
        Log.d(TAG, "Building Gemini verdict request...")
        val prompt = buildString {
            appendLine("You are FactLens, an AI fact-verification assistant.")
            appendLine("Evaluate this claim against the evidence and return JSON only.")
            appendLine()
            appendLine("Claim: $claim")
            appendLine()
            if (sources.isNotEmpty()) {
                appendLine("Evidence:")
                sources.forEachIndexed { i, s ->
                    appendLine("${i + 1}. ${s.title} — ${s.snippet}")
                }
            }
            appendLine()
            appendLine("""Return JSON: {"verdict": "Supported|Contradicted|Misleading|Insufficient Evidence", "confidence": 0.0-0.95, "explanation": "..."}""")
        }

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                }
            ))
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty Gemini response")

        val result = JSONObject(body)
        val text = result.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val jsonStr = text.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
        val json = JSONObject(jsonStr)

        val verdict = json.optString("verdict", "Unknown")
        val confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95)
        val explanation = json.optString("explanation", "No explanation.")

        return VerificationResponse(
            claim = claim,
            verdict = verdict,
            confidence = confidence,
            explanation = explanation,
            sources = sources
        )
    }
}
