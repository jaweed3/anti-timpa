package com.factlens.network

import android.util.Log
import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FactLens.Verdict"

class VerdictEngine {

    private val searchClient = SearchClient()

    companion object {
        var backendUrl: String = "http://10.0.2.2:8000"
        var geminiApiKey: String = ""
        var USE_MOCK: Boolean = false
    }

    suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "VERDICT ENGINE STARTED")
        Log.d(TAG, "Input: ${text.take(80)}... | USE_MOCK=$USE_MOCK")

        if (USE_MOCK) return@withContext mockVerdict(text)

        val claim = extractClaim(text)
        val sources = searchClient.search(claim)

        try {
            return@withContext BackendClient().verify(claim, sources)
        } catch (e: Exception) {
            Log.w(TAG, "Backend failed: ${e.message}")
        }

        if (geminiApiKey.isNotBlank()) {
            try {
                return@withContext GeminiClient().verify(claim, sources)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini failed: ${e.message}")
            }
        }

        Log.w(TAG, "No backends available, using fallback")
        fallbackVerdict(claim, sources)
    }

    private fun extractClaim(text: String): String {
        val cleaned = text
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("@\\w+"), "")
            .replace(Regex("#\\w+"), "")
            .trim()
        return cleaned.take(500).ifBlank { text.take(500) }
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
        if (sources.isEmpty()) {
            return VerificationResponse(claim, "Insufficient Evidence", 0.0, "No evidence found.", emptyList())
        }

        val claimLower = claim.lowercase()
        val rumorWords = listOf("hoax", "fake", "false", "bohong", "tipu")
        val confirmWords = listOf("true", "real", "fact", "benar", "fakta")
        val hasRumor = rumorWords.any { claimLower.contains(it) }
        val hasConfirm = confirmWords.any { claimLower.contains(it) }

        val verdict = if (hasRumor && !hasConfirm) "Contradicted"
        else if (hasConfirm && !hasRumor) "Supported"
        else "Insufficient Evidence"

        return VerificationResponse(
            claim = claim,
            verdict = verdict,
            confidence = 0.5,
            explanation = "Found ${sources.size} potential source(s). Keyword-based analysis.",
            sources = sources
        )
    }
}
