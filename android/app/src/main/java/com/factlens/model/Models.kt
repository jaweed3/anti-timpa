package com.factlens.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class VerificationRequest(
    @SerializedName("text") val text: String,
    @SerializedName("language") val language: String = "en"
)

data class VerificationResponse(
    @SerializedName("claim") val claim: String,
    @SerializedName("verdict") val verdict: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("sources") val sources: List<Source>
)

data class Source(
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("snippet") val snippet: String
)

@Entity(tableName = "ScanHistory")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val claim: String,
    val verdict: String,
    val confidence: Double,
    val explanation: String,
    val sourcesJson: String,
    val screenshotPath: String? = null,
    val isFavorite: Boolean = false
)

data class ScanResultData(
    val claim: String = "",
    val verdict: String,
    val confidence: Double,
    val explanation: String,
    val sources: List<Source>
)

data class ScamCheckRequest(
    val text: String
)

data class ScamCheckResponse(
    val verdict: String,
    val riskScore: Int,
    val flaggedItems: List<FlaggedItem>,
    val explanation: String,
    val sources: List<String>,
    val shouldBlur: Boolean = false
)

data class FlaggedItem(
    val type: String,
    val value: String,
    val reason: String
)
