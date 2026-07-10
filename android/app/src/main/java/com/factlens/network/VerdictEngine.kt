package com.factlens.network

import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "FactLens.Verdict"

class VerdictEngine {

    private val searchClient = SearchClient()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        // Set this in your app or via BuildConfig
        var geminiApiKey: String = ""
        // When true, returns simulated data without network calls
        var USE_MOCK: Boolean = false
    }

    suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "VERDICT ENGINE STARTED")
        Log.d(TAG, "Input text (${text.length} chars): \"${text.take(80)}${if (text.length > 80) "..." else ""}\"")
        Log.d(TAG, "USE_MOCK=$USE_MOCK, geminiApiKey configured=${geminiApiKey.isNotBlank()}")

        if (USE_MOCK) {
            Log.d(TAG, "Using MOCK mode, returning simulated data")
            return@withContext mockVerdict(text)
        }
        val claim = extractClaim(text)
        Log.d(TAG, "Extracted claim (${claim.length} chars): \"${claim.take(80)}${if (claim.length > 80) "..." else ""}\"")

        Log.d(TAG, "Starting search for evidence...")
        val searchStartTime = System.currentTimeMillis()
        val sources = searchClient.search(claim)
        val searchElapsed = System.currentTimeMillis() - searchStartTime
        Log.d(TAG, "Search completed in ${searchElapsed}ms, found ${sources.size} sources")
        sources.forEachIndexed { i, s ->
            Log.d(TAG, "  Source ${i + 1}: ${s.title.take(60)} — ${s.url.take(80)}")
        }

        if (geminiApiKey.isNotBlank()) {
            Log.d(TAG, "Gemini API key available, calling Gemini for verdict...")
            geminiVerdict(claim, sources)
        } else {
            Log.w(TAG, "No Gemini API key, using fallback verdict")
            fallbackVerdict(claim, sources)
        }
    }

    private fun extractClaim(text: String): String {
        Log.d(TAG, "Extracting claim from text...")
        val cleaned = text
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("@\\w+"), "")
            .replace(Regex("#\\w+"), "")
            .trim()
        val claim = cleaned.take(500).ifBlank { text.take(500) }
        Log.d(TAG, "Claim extracted: ${claim.length} chars")
        return claim
    }

    private fun geminiVerdict(claim: String, sources: List<Source>): VerificationResponse {
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

        try {
            Log.d(TAG, "Sending request to Gemini API...")
            val requestStartTime = System.currentTimeMillis()
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
            val requestElapsed = System.currentTimeMillis() - requestStartTime
            Log.d(TAG, "Gemini API response received in ${requestElapsed}ms, HTTP ${response.code}")

            val body = response.body?.string()
            if (body != null) {
                Log.d(TAG, "Response body length: ${body.length} chars")
                val result = JSONObject(body)
                val text = result.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                Log.d(TAG, "Gemini raw response (${text.length} chars):")
                Log.d(TAG, text.take(300))

                // Extract JSON from response
                val jsonStr = text.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
                val json = JSONObject(jsonStr)

                val verdict = json.optString("verdict", "Unknown")
                val confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95)
                val explanation = json.optString("explanation", "No explanation.")

                Log.d(TAG, "Parsed verdict: $verdict, confidence: $confidence")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "VERDICT ENGINE COMPLETE (Gemini)")
                Log.d(TAG, "═══════════════════════════════════════")

                return VerificationResponse(
                    claim = claim,
                    verdict = verdict,
                    confidence = confidence,
                    explanation = explanation,
                    sources = sources
                )
            } else {
                Log.e(TAG, "Gemini response body is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed: ${e.message}", e)
        }

        return fallbackVerdict(claim, sources)
    }

    private fun mockVerdict(text: String): VerificationResponse {
        val claim = extractClaim(text)
        val hasContent = claim.length > 15
        return VerificationResponse(
            claim = claim,
            verdict = if (hasContent) "Supported" else "Insufficient Evidence",
            confidence = if (hasContent) 0.87 else 0.0,
            explanation = if (hasContent)
                "This claim aligns with multiple credible sources including recent scientific publications. The evidence consistently supports the statement across peer-reviewed studies."
            else
                "The scanned text is too short for meaningful analysis. Please try scanning a longer passage.",
            sources = if (hasContent) listOf(
                Source(
                    "Oxford University Study 2026",
                    "https://example.com/oxford-study",
                    "Research findings confirm the validity of this claim with 92% confidence across multiple controlled trials."
                ),
                Source(
                    "WHO Global Health Report",
                    "https://example.com/who-report",
                    "International health organization data corroborates the main assertions made in the analyzed text."
                ),
                Source(
                    "Nature Scientific Review",
                    "https://example.com/nature-review",
                    "Comprehensive meta-analysis of 47 studies supports the factual basis of this information."
                )
            ) else emptyList()
        )
    }

    private fun fallbackVerdict(claim: String, sources: List<Source>): VerificationResponse {
        Log.d(TAG, "Using fallback verdict (keyword-based)...")
        if (sources.isEmpty()) {
            Log.d(TAG, "No sources, returning Insufficient Evidence")
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

        Log.d(TAG, "Keyword check: hasRumor=$hasRumor, hasConfirm=$hasConfirm → verdict=$verdict")

        val explanation = buildString {
            append("Found ${sources.size} potential source(s). ")
            if (verdict == "Insufficient Evidence") {
                append("Set GEMINI_API_KEY for detailed AI analysis.")
            } else {
                append("Keyword-based analysis suggests this claim may be $verdict.")
            }
        }

        Log.d(TAG, "Fallback verdict: $verdict")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "VERDICT ENGINE COMPLETE (fallback)")
        Log.d(TAG, "═══════════════════════════════════════")

        return VerificationResponse(
            claim = claim,
            verdict = verdict,
            confidence = 0.5,
            explanation = explanation,
            sources = sources
        )
    }
}
