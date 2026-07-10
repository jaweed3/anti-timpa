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
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

private const val TAG = "FactLens.Verdict"

class VerdictEngine {

    private val searchClient = SearchClient()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        // Backend URL — switch between emulator and physical device
        // Emulator: "http://10.0.2.2:8000"
        // Physical device: "http://<YOUR_LAPTOP_IP>:8000"
        var backendUrl: String = "http://10.0.2.2:8000"

        // Gemini API key (fallback if backend is unreachable)
        var geminiApiKey: String = ""

        // When true, returns simulated data without network calls
        var USE_MOCK: Boolean = false
    }

    suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "VERDICT ENGINE STARTED")
        Log.d(TAG, "Input text (${text.length} chars): \"${text.take(80)}${if (text.length > 80) "..." else ""}\"")
        Log.d(TAG, "Backend: $backendUrl, USE_MOCK=$USE_MOCK, geminiKey=${geminiApiKey.isNotBlank()}")

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

        // Try backend first, fallback to Gemini, then fallback to keyword
        try {
            Log.d(TAG, "Trying backend at $backendUrl...")
            val backendResult = backendVerdict(claim, sources)
            Log.d(TAG, "Backend verdict: ${backendResult.verdict}")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "VERDICT ENGINE COMPLETE (backend)")
            Log.d(TAG, "═══════════════════════════════════════")
            return@withContext backendResult
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable: ${e.message}")
        }

        if (geminiApiKey.isNotBlank()) {
            Log.d(TAG, "Falling back to Gemini API...")
            try {
                val geminiResult = geminiVerdict(claim, sources)
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "VERDICT ENGINE COMPLETE (Gemini)")
                Log.d(TAG, "═══════════════════════════════════════")
                return@withContext geminiResult
            } catch (e: Exception) {
                Log.e(TAG, "Gemini failed: ${e.message}")
            }
        }

        Log.w(TAG, "All backends failed, using fallback verdict")
        val fallback = fallbackVerdict(claim, sources)
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "VERDICT ENGINE COMPLETE (fallback)")
        Log.d(TAG, "═══════════════════════════════════════")
        fallback
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

    private fun backendVerdict(claim: String, sources: List<Source>): VerificationResponse {
        Log.d(TAG, "Calling backend POST $backendUrl/verify")

        val jsonBody = JSONObject().apply {
            put("text", claim)
            put("language", "id")
        }

        val request = Request.Builder()
            .url("$backendUrl/verify")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val requestStartTime = System.currentTimeMillis()
        val response = httpClient.newCall(request).execute()
        val requestElapsed = System.currentTimeMillis() - requestStartTime
        Log.d(TAG, "Backend response: HTTP ${response.code} in ${requestElapsed}ms")

        if (response.code != 200) {
            throw Exception("Backend returned HTTP ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty response body")
        Log.d(TAG, "Backend body (${body.length} chars): ${body.take(200)}")

        val json = JSONObject(body)
        return VerificationResponse(
            claim = json.optString("claim", claim),
            verdict = json.optString("verdict", "Unknown"),
            confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95),
            explanation = json.optString("explanation", "No explanation."),
            sources = sources
        )
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

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                }
            ))
        }

        Log.d(TAG, "Sending request to Gemini API...")
        val requestStartTime = System.currentTimeMillis()
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$geminiApiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val requestElapsed = System.currentTimeMillis() - requestStartTime
        Log.d(TAG, "Gemini response: HTTP ${response.code} in ${requestElapsed}ms")

        val body = response.body?.string() ?: throw Exception("Empty Gemini response")
        Log.d(TAG, "Gemini body (${body.length} chars): ${body.take(200)}")

        val result = JSONObject(body)
        val text = result.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        Log.d(TAG, "Gemini raw response (${text.length} chars): ${text.take(200)}")

        val jsonStr = text.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
        val json = JSONObject(jsonStr)

        val verdict = json.optString("verdict", "Unknown")
        val confidence = json.optDouble("confidence", 0.5).coerceIn(0.0, 0.95)
        val explanation = json.optString("explanation", "No explanation.")

        Log.d(TAG, "Parsed verdict: $verdict, confidence: $confidence")

        return VerificationResponse(
            claim = claim,
            verdict = verdict,
            confidence = confidence,
            explanation = explanation,
            sources = sources
        )
    }

    private fun mockVerdict(text: String): VerificationResponse {
        val claim = extractClaim(text)
        val hasContent = claim.length > 15
        return VerificationResponse(
            claim = claim,
            verdict = if (hasContent) "Supported" else "Insufficient Evidence",
            confidence = if (hasContent) 0.87 else 0.0,
            explanation = if (hasContent)
                "This claim aligns with multiple credible sources including recent scientific publications."
            else
                "The scanned text is too short for meaningful analysis.",
            sources = if (hasContent) listOf(
                Source("Oxford University Study 2026", "https://example.com/oxford-study", "Research findings confirm the validity of this claim."),
                Source("WHO Global Health Report", "https://example.com/who-report", "International health organization data corroborates the main assertions."),
                Source("Nature Scientific Review", "https://example.com/nature-review", "Comprehensive meta-analysis supports the factual basis.")
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
                explanation = "No evidence found. Backend or Gemini API required.",
                sources = emptyList()
            )
        }

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
                append("Backend or Gemini API required for detailed analysis.")
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
