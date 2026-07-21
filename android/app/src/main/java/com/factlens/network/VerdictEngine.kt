package com.factlens.network

import android.util.Log
import com.factlens.model.Source
import com.factlens.model.VerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AntiTimpa.Verdict"

class VerdictEngine {

    private val searchClient = SearchClient()

    companion object {
        var backendUrl: String = "http://192.168.120.75:8000"
        var geminiApiKey: String = ""
        var USE_MOCK: Boolean = false
        var shouldBlurResult: Boolean = false
    }

    suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "VERDICT ENGINE STARTED")
        Log.d(TAG, "Input: ${text.take(80)}... | USE_MOCK=$USE_MOCK")

        shouldBlurResult = false

        if (USE_MOCK) return@withContext mockVerdict(text)

        try {
            val scamResult = checkScam(text)
            if (scamResult.verdict != "Supported") {
                Log.d(TAG, "/check-scam returned non-Supported verdict, using it")
                return@withContext scamResult
            }
            Log.d(TAG, "/check-scam returned Aman, falling through to /verify")
        } catch (e: Exception) {
            Log.w(TAG, "/check-scam failed: ${e.message}")
        }

        val claim = extractClaim(text)
        val sources = searchClient.search(claim)

        try {
            return@withContext BackendClient().verify(claim, sources)
        } catch (e: Exception) {
            Log.w(TAG, "Backend verify failed: ${e.message}")
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

    private fun detectScamIndicators(text: String): Boolean {
        val hasPhone = Regex("""(?:\+62|62|0)8[1-9]\d{7,11}""").containsMatchIn(text)
        val hasAccount = Regex("""\b\d{10,16}\b""").containsMatchIn(text)
        val hasScamKeywords = listOf(
            "transfer", "rekening", "pinjol", "pinjaman", "dana",
            "hadiah", "undian", "terpilih", "pinjam"
        ).any { text.lowercase().contains(it) }

        return (hasPhone && hasScamKeywords) || (hasAccount && hasScamKeywords)
    }

    private suspend fun checkScam(text: String): VerificationResponse = withContext(Dispatchers.IO) {
        try {
            val response = BackendClient().checkScam(text)

            val mappedVerdict = when (response.verdict) {
                "Terindikasi Penipuan" -> "Contradicted"
                "Mencurigakan" -> "Misleading"
                "Aman" -> "Supported"
                else -> "Insufficient Evidence"
            }

            shouldBlurResult = response.shouldBlur

            val flaggedSummary = response.flaggedItems.joinToString("\n") {
                "\u2022 ${it.value} \u2014 ${it.reason}"
            }

            val explanationText = if (flaggedSummary.isNotEmpty()) {
                "${response.explanation}\n\nItem terdeteksi:\n$flaggedSummary"
            } else {
                response.explanation
            }

            VerificationResponse(
                claim = text.take(200),
                verdict = mappedVerdict,
                confidence = (response.riskScore / 100.0).coerceIn(0.0, 0.95),
                explanation = explanationText,
                sources = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Scam check failed: ${e.message}")
            shouldBlurResult = false
            fallbackScamVerdict(text)
        }
    }

    private fun fallbackScamVerdict(text: String): VerificationResponse {
        val hasPhone = Regex("""(?:\+62|62|0)8[1-9]\d{7,11}""").containsMatchIn(text)
        val hasAccount = Regex("""\b\d{10,16}\b""").containsMatchIn(text)

        val reason = when {
            hasAccount && hasPhone -> "Nomor rekening dan telepon terdeteksi dalam teks."
            hasAccount -> "Nomor rekening terdeteksi dalam teks."
            hasPhone -> "Nomor telepon terdeteksi dalam teks."
            else -> "Teks mengandung kata kunci terkait scam."
        }

        return VerificationResponse(
            claim = text.take(200),
            verdict = "Misleading",
            confidence = 0.3,
            explanation = "Terjadi kesalahan saat memeriksa ke backend. $reason Harap periksa secara manual.",
            sources = emptyList()
        )
    }

    private fun extractClaim(text: String): String {
        val cleaned = text
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
