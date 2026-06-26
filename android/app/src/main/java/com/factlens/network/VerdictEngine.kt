package com.factlens.network

import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VerdictEngine {

    private val searchClient = SearchClient()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        // Set this in your app or via BuildConfig
        var geminiApiKey: String = ""
    }

    suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        val claim = extractClaim(text)
        val sources = searchClient.search(claim)

        if (geminiApiKey.isNotBlank()) {
            geminiVerdict(claim, sources)
        } else {
            fallbackVerdict(claim, sources)
        }
    }

    private fun extractClaim(text: String): String {
        val cleaned = text
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("@\\w+"), "")
            .replace(Regex("#\\w+"), "")
            .trim()
        return cleaned.take(500).ifBlank { text.take(500) }
    }

    private fun geminiVerdict(claim: String, sources: List<Source>): VerificationResponse {
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

        try {
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
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$geminiApiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val result = JSONObject(body)
                val text = result.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Extract JSON from response
                val jsonStr = text.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
                val json = JSONObject(jsonStr)

                return VerificationResponse(
                    claim = claim,
                    verdict = json.optString("verdict", "Unknown"),
                    confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95),
                    explanation = json.optString("explanation", "No explanation."),
                    sources = sources
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fallbackVerdict(claim, sources)
    }

    private fun fallbackVerdict(claim: String, sources: List<Source>): VerificationResponse {
        if (sources.isEmpty()) {
            return VerificationResponse(
                claim = claim,
                verdict = "Insufficient Evidence",
                confidence = 0.0,
                explanation = "No evidence found. Set GEMINI_API_KEY for AI-powered analysis.",
                sources = emptyList()
            )
        }

        // Simple keyword heuristic
        val claimLower = claim.lowercase()
        val rumorWords = listOf("hoax", "fake", "false", "bohong", "tipu")
        val confirmWords = listOf("true", "real", "fact", "benar", "fakta")

        val hasRumor = rumorWords.any { claimLower.contains(it) }
        val hasConfirm = confirmWords.any { claimLower.contains(it) }

        val verdict = if (hasRumor && !hasConfirm) "Contradicted"
        else if (hasConfirm && !hasRumor) "Supported"
        else "Insufficient Evidence"

        val explanation = buildString {
            append("Found ${sources.size} potential source(s). ")
            if (verdict == "Insufficient Evidence") {
                append("Set GEMINI_API_KEY for detailed AI analysis.")
            } else {
                append("Keyword-based analysis suggests this claim may be $verdict.")
            }
        }

        return VerificationResponse(
            claim = claim,
            verdict = verdict,
            confidence = 0.5,
            explanation = explanation,
            sources = sources
        )
    }
}
