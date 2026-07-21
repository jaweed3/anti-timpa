# Pivot Plan — FactLens → AntiTimpa

> **Goal:** Pivot dari general fact-checker ke **screen-blurring scam detector** spesifik Indonesia.
> **USP:** Bukan cuma deteksi — langsung **ngaburin konten scam** di layar biar mata lo gak kena.
> **Tagline:** *"Blur scams before they blur your judgment."*
> **Tim:** 2 Developer (parallel)

---

## Product Flow (MVP)

```
User di IG/WA/TikTok
  ↓  lihat postingan/chat mencurigakan (pinjol, transfer, rekening)
  ↓  tap & hold floating button 2 detik
  ↓  [haptic feedback + progress ring]
  ↓
ScreenCapture (MediaProjection API)
  ↓
On-Device OCR (ML Kit) — teks diekstrak
  ↓
┌──────────────────────────────────────────────┐
│  BLUR OVERLAY — langsung muncul              │
│                                              │
│  ┌────────────────────────────────────┐      │
│  │  [IG content — BLURRED]            │      │
│  │  ████████████████████████████████  │      │
│  │  ██ semua dikaburin █████████████  │      │
│  │  ████████████████████████████████  │      │
│  │                              ╳     │      │
│  └────────────────────────────────────┘      │
│                                              │
│  Semua screenshot processing di background:  │
│  • OCR → scam check → verdict               │
│                                              │
│  ┌── Result Bottom Sheet ─────────────────┐  │
│  │  🚨 TERINDIKASI PENIPUAN      Skor: 70 │  │
│  │                                        │  │
│  │  "Teks mengandung nomor rekening       │  │
│  │   yang terdaftar di database           │  │
│  │   penipuan cekrekening.id"             │  │
│  │                                        │  │
│  │  Item:                                 │  │
│  │  • 1234567890 — Rekening terindikasi   │  │
│  │  • "transfer sekarang" — Scam keyword  │  │
│  │                                        │  │
│  │  [✕ Tutup]       [Lihat Detail]       │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  Tap dismiss → blur ilang → balik ke IG      │
└──────────────────────────────────────────────┘
```

**Kenapa blur dulu baru hasil?**
- Scam content berbahaya — makin lama kena mata, makin berisiko
- Blur langsung kasih安全感: "Aman, AntiTimpa udah handle"
- Visual impact gila pas demo — screen langsung blur merah 🚨
- Privacy: orang sekitar juga gak bisa liat nomor rekening lo

---

## Timeline

```
Day 1 (Malam ini)
  20:00 - 20:15  │  [BOTH] API Contract + Blur spec
  20:15 - 23:00  │  [A] Backend — scam_extractor, scam_checker, risk_scorer, endpoint
                   │  [B] Android — blur overlay, trigger flow, scam UI
  23:00 - 01:00  │  [A] Test backend + LLM prompt (opsional)
                   │  [B] Integrasi scan → blur → result flow
  01:00 - 02:00  │  [BOTH] End-to-end test + bug fix

Day 2 (Siang/malem)
  18:00 - 21:00  │  [A] Fix bug, deploy backend (Railway/ngrok)
                   │  [B] Fix bug, test on real device, tuning blur
  21:00 - 00:00  │  [BOTH] Demo recording, pitch deck finalisasi

Day 3+
  Proposal, buffer, final APK, deploy
```

---

## Person A — Backend (Python)

### Step 1: `backend/scam_extractor.py` **NEW**

Extract scam indicators dari raw OCR text — phone, bank account, URL, bank name.

```python
import re
from typing import List

BANKS = {
    "bca": "BCA", "mandiri": "Mandiri", "bri": "BRI",
    "bni": "BNI", "bsi": "BSI", "cimb": "CIMB Niaga",
    "danamon": "Danamon", "permata": "Permata",
    "maybank": "Maybank", "panin": "Panin",
    "uob": "UOB", "ocbc": "OCBC NISP",
    "btn": "BTN", "bukopin": "Bukopin",
    "jenius": "Jenius", "digibank": "Digibank",
    "blu": "Blu by BCA Digital",
}

def extract_phones(text: str) -> List[str]:
    phones = re.findall(r'(?:\+62|62|0)8[1-9]\d{7,11}', text)
    return list(set(phones))

def extract_accounts(text: str) -> List[str]:
    accounts = re.findall(r'\b\d{10,16}\b', text)
    return list(set(accounts))

def extract_urls(text: str) -> List[dict]:
    urls = re.findall(r'https?://[^\s]+|bit\.ly/\S+|tinyurl\.com/\S+|wa\.me/\S+', text)
    domains = []
    for u in urls:
        match = re.search(r'https?://([^/\s]+)', u)
        domain = match.group(1) if match else u
        is_shortener = any(d in domain for d in ["bit.ly", "tinyurl", "shorturl", "s.id", "rb.gy"])
        domains.append({"url": u, "domain": domain, "is_shortener": is_shortener})
    return domains

def extract_banks(text: str) -> List[str]:
    text_lower = text.lower()
    found = [name for key, name in BANKS.items() if key in text_lower]
    return list(set(found))

def extract_all(text: str) -> dict:
    return {
        "phones": extract_phones(text),
        "accounts": extract_accounts(text),
        "urls": extract_urls(text),
        "banks": extract_banks(text),
    }
```

### Step 2: `backend/scam_checker.py` **NEW**

Cross-check dengan database resmi.

```python
import httpx
import re
import logging
from typing import List

logger = logging.getLogger("antitimpa.scam_check")

OJK_PINJOL_ILEGAL = [
    "danacepat", "uangteman", "rupiahcepat", "danaaku",
    "pinjamonline", "cepatkaya", "uanginstan", "danasyariah",
    "pinjamsekarang", "danatunai", "uangcepat", "cepatdana",
    "danamudah", "akuaku", "rupiahcepatid", "danacepatid",
    "temanuang", "kawanmoney", "cepatuang",
]

SCAM_KEYWORDS = [
    "transfer sekarang", "batas waktu", "hadiah", "terpilih",
    "klik link", "verifikasi akun", "undian", "pemenang",
    "ubah jadi", "gandakan", "modal kecil", "return besar",
    "jaminan", "bebas agunan", "cair sekarang", "tanpa BI checking",
    "tanpa riba", "halal", "dana darurat",
]

URGENCY_MARKERS = ["segera", "hari ini", "jam lagi", "batas", "terakhir", "jangan lewatkan"]

async def check_cekrekening(account: str, bank: str) -> dict | None:
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            url = f"https://cekrekening.id/api/reports/{bank}/{account}"
            resp = await client.get(url)
            if resp.status_code == 200:
                data = resp.json()
                return {
                    "account": account,
                    "bank": bank,
                    "reports": data.get("total_reports", 0),
                    "status": data.get("status", "unknown"),
                }
    except Exception as e:
        logger.warning("cekrekening API failed for %s/%s: %s", bank, account, e)
    return None

def check_ojk_pinjol(text: str) -> List[str]:
    text_lower = text.lower()
    return [name for name in OJK_PINJOL_ILEGAL if name in text_lower]

def check_url_pattern(urls: List[dict]) -> List[dict]:
    flagged = []
    for u in urls:
        reasons = []
        if u["is_shortener"]:
            reasons.append("URL shortener — sulit dilacak")
        flagged.append({"url": u["url"], "domain": u["domain"], "reasons": reasons})
    return flagged

def check_scam_patterns(text: str) -> dict:
    text_lower = text.lower()
    found_keywords = [kw for kw in SCAM_KEYWORDS if kw in text_lower]
    found_urgency = [m for m in URGENCY_MARKERS if m in text_lower]
    return {
        "scam_keywords": found_keywords,
        "urgency_markers": found_urgency,
        "total": len(found_keywords) + len(found_urgency),
    }
```

### Step 3: `backend/risk_scorer.py` **NEW**

```python
from typing import List
from scam_checker import check_scam_patterns

def calculate_score(
    cekrekening_results: List[dict],
    ojk_found: List[str],
    url_flagged: List[dict],
    text: str,
) -> dict:
    score = 0
    reasons = []

    for result in cekrekening_results:
        if result and result.get("reports", 0) > 0:
            score += 50
            reasons.append(f"Rekening {result['bank']}/{result['account']} terdaftar di cekrekening.id")

    if ojk_found:
        score += 40
        reasons.append(f"Entitas terdaftar di OJK pinjol ilegal: {', '.join(ojk_found)}")

    shorteners = [u for u in url_flagged if u.get("reasons")]
    if shorteners:
        score += 20
        reasons.append("Menggunakan URL shortener yang mencurigakan")

    patterns = check_scam_patterns(text)
    if patterns["total"] >= 3:
        score += 20
        reasons.append(f"Mengandung {patterns['total']} pola bahasa penipuan")
    elif patterns["total"] >= 1:
        score += 10
        reasons.append(f"Mengandung {patterns['total']} pola bahasa penipuan")

    if patterns["urgency_markers"]:
        score += 5
        reasons.append("Mengandung urgensi palsu (batas waktu/segera)")

    if score >= 51:
        verdict = "Terindikasi Penipuan"
    elif score >= 21:
        verdict = "Mencurigakan"
    else:
        verdict = "Aman"

    return {
        "score": min(score, 100),
        "verdict": verdict,
        "reasons": reasons,
        "patterns": patterns,
    }
```

### Step 4: Update `backend/models.py`

```python
from pydantic import BaseModel, Field
from typing import List

# Tambah setelah class existing:

class ScamCheckRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=5000)

class FlaggedItem(BaseModel):
    type: str       # "phone" | "account" | "url" | "bank" | "pattern"
    value: str
    reason: str

class ScamCheckResponse(BaseModel):
    verdict: str
    riskScore: int = Field(ge=0, le=100)
    flaggedItems: List[FlaggedItem]
    explanation: str
    sources: List[str]
    shouldBlur: bool = False  # NEW: flag buat Android, blur kalo >= Mencurigakan
```

### Step 5: Update `backend/main.py`

```python
# Tambah import:
from models import ScamCheckRequest, ScamCheckResponse, FlaggedItem
from scam_extractor import extract_all
from scam_checker import check_cekrekening, check_ojk_pinjol, check_url_pattern
from risk_scorer import calculate_score

# Tambah endpoint:
@app.post("/check-scam", response_model=ScamCheckResponse)
async def check_scam(body: ScamCheckRequest):
    if not body.text or len(body.text.strip()) < 1:
        raise HTTPException(status_code=400, detail="Text is empty")

    text = body.text[:5000]

    # 1. Extract indicators
    extracted = extract_all(text)

    # 2. Cross-check
    cekrekening_results = []
    for account in extracted["accounts"][:3]:
        for bank in extracted["banks"]:
            result = await check_cekrekening(account, bank)
            if result:
                cekrekening_results.append(result)

    ojk_found = check_ojk_pinjol(text)
    url_flagged = check_url_pattern(extracted["urls"])

    # 3. Calculate risk score
    risk = calculate_score(cekrekening_results, ojk_found, url_flagged, text)

    # 4. Build flagged items
    flagged_items = []
    for a in extracted["accounts"]:
        flagged_items.append(FlaggedItem(type="account", value=a, reason="Nomor rekening terdeteksi"))
    for p in extracted["phones"]:
        flagged_items.append(FlaggedItem(type="phone", value=p, reason="Nomor telepon terdeteksi"))
    for u in url_flagged:
        if u["reasons"]:
            flagged_items.append(FlaggedItem(type="url", value=u["url"], reason="; ".join(u["reasons"])))
    for kw in risk["patterns"]["scam_keywords"]:
        flagged_items.append(FlaggedItem(type="pattern", value=kw, reason="Pola bahasa penipuan"))

    # 5. Generate explanation (LLM optional)
    explanation = risk["reasons"][0] if risk["reasons"] else "Tidak ditemukan indikasi penipuan."
    if pipeline and pipeline.llm.available:
        try:
            llm_explanation = pipeline.llm.chat(
                system_prompt=SCAM_SYSTEM_PROMPT,
                user_prompt=f"Text: {text}\nFlagged items: {[f'{f.type}: {f.value}' for f in flagged_items]}\nRisk score: {risk['score']}\nVerdict: {risk['verdict']}",
            )
            if llm_explanation:
                explanation = llm_explanation
        except Exception as e:
            logger.warning("LLM scam explanation failed: %s", e)

    return ScamCheckResponse(
        verdict=risk["verdict"],
        riskScore=risk["score"],
        flaggedItems=flagged_items,
        explanation=explanation,
        sources=[r["reason"] for r in cekrekening_results] + ojk_found,
        shouldBlur=risk["verdict"] in ("Terindikasi Penipuan", "Mencurigakan"),
    )
```

### Step 6: Update `backend/llm_client.py`

```python
SCAM_SYSTEM_PROMPT = """You are AntiTimpa, an AI scam detection assistant for Indonesia.

Your task is to explain why a text is flagged as potential scam in simple Indonesian.

Context provided:
- The OCR text
- Flagged items (phone, account, URL, bank, suspicious patterns)
- Risk score and verdict

Rules:
- Explain in 2-3 sentences max
- Use simple Indonesian, easy for general public to understand
- Tell the user what action to take (e.g. "Jangan transfer", "Laporkan ke bank")
- Be factual, reference the flagged items
- Never scare-monger — just state facts
"""
```

---

## Person B — Android (Kotlin)

### Step 7: Screen Blur Overlay **NEW** — `ScreenBlurOverlay.kt`

**Ini fitur utama.** Begitu screen capture selesai, screenshot langsung di-blur dan ditampilkan sebagai fullscreen overlay. User liat konten scam dalam keadaan blur — baru hasil analisis muncul di bawah.

```kotlin
package com.factlens.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

object ScreenBlurOverlay {

    private var currentView: View? = null

    /**
     * Blur screenshot bitmap pakai RenderScript (fast) atau fallback Canvas.
     * Munculin sebagai fullscreen overlay biar konten scam langsung kabur.
     * Processing murni on-device — gak ada data dikirim ke mana-mana.
     */
    fun showBlur(context: Context, screenshot: Bitmap) {
        dismiss()

        val blurred = blurBitmap(context, screenshot, 25f)  // blur radius 25
        screenshot.recycle()  // free memory — blurred version udah cukup

        val imageView = ImageView(context).apply {
            setImageBitmap(blurred)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            dimAmount = 0.3f
        }

        currentView = imageView
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(imageView, params)
    }

    private fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        // Prioritaskan RenderScript — lebih cepet untuk realtime blur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: pakai RenderScript bluring via intrinsic
            return try {
                val rs = android.renderscript.RenderScript.create(context)
                val input = android.renderscript.Allocation.createFromBitmap(rs, bitmap)
                val output = android.renderscript.Allocation.createTyped(rs, input.type)
                val blur = android.renderscript.ScriptIntrinsicBlur.create(rs, android.renderscript.Element.U8_4(rs))
                blur.setRadius(radius.coerceIn(1f, 25f))
                blur.setInput(input)
                blur.forEach(output)
                output.copyTo(bitmap)
                rs.destroy()
                bitmap
            } catch (e: Exception) {
                // Fallback ke Canvas blur kalo RenderScript gagal
                canvasBlur(bitmap, radius)
            }
        } else {
            canvasBlur(bitmap, radius)
        }
    }

    private fun canvasBlur(bitmap: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return output
    }

    fun dismiss() {
        currentView?.let { view ->
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(view)
            } catch (_: Exception) {}
            currentView = null
        }
    }
}
```

### Step 8: Update `Models.kt`

```kotlin
// Tambah setelah data class existing:

data class ScamCheckRequest(val text: String)

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
```

### Step 9: Update `BackendClient.kt`

```kotlin
fun checkScam(text: String): ScamCheckResponse {
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
    val itemsArray = json.optJSONArray("flaggedItems")
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
        riskScore = json.optInt("riskScore", 0),
        flaggedItems = items,
        explanation = json.optString("explanation", "No explanation."),
        sources = sources,
        shouldBlur = json.optBoolean("shouldBlur", false)
    )
}
```

### Step 10: Update `VerdictEngine.kt` — Scam Detection + Blur Logic

```kotlin
// 1. Tambah auto-detection di verify():
suspend fun verify(text: String): VerificationResponse = withContext(Dispatchers.IO) {
    val scamIndicators = detectScamIndicators(text)

    if (scamIndicators) {
        return@withContext checkScam(text)
    }

    if (USE_MOCK) return@withContext mockVerdict(text)
    // ... existing fact-check logic unchanged ...
}

// 2. Deteksi scam indicators:
private fun detectScamIndicators(text: String): Boolean {
    val hasPhone = Regex("""(?:\+62|62|0)8[1-9]\d{7,11}""").containsMatchIn(text)
    val hasAccount = Regex("""\b\d{10,16}\b""").containsMatchIn(text)
    val hasScamKeywords = listOf(
        "transfer", "rekening", "pinjol", "pinjaman", "dana",
        "hadiah", "undian", "terpilih"
    ).any { text.lowercase().contains(it) }

    return (hasPhone && hasScamKeywords) || (hasAccount && hasScamKeywords)
}

// 3. Scam check — panggil backend, return VerificationResponse + blur flag
private suspend fun checkScam(text: String): VerificationResponse = withContext(Dispatchers.IO) {
    try {
        val response = BackendClient().checkScam(text)

        val mappedVerdict = when (response.verdict) {
            "Terindikasi Penipuan" -> "Contradicted"
            "Mencurigakan" -> "Misleading"
            "Aman" -> "Supported"
            else -> "Insufficient Evidence"
        }

        val flaggedSummary = response.flaggedItems.joinToString("\n") {
            "• ${it.value} — ${it.reason}"
        }

        val explanationText = if (flaggedSummary.isNotEmpty()) {
            "${response.explanation}\n\nItem terdeteksi:\n$flaggedSummary"
        } else {
            response.explanation
        }

        // Simpan flag blur — akan dipake sama OCRProcessor/OverlayService
        shouldBlurResult = response.shouldBlur

        VerificationResponse(
            claim = text.take(200),
            verdict = mappedVerdict,
            confidence = (response.riskScore / 100.0).coerceIn(0.0, 0.95),
            explanation = explanationText,
            sources = emptyList()
        )
    } catch (e: Exception) {
        fallbackVerdict(text, emptyList())
    }
}

// 4. extractClaim — preserve numbers for scam detection
private fun extractClaim(text: String): String {
    val cleaned = text
        .replace(Regex("@\\w+"), "")
        .replace(Regex("#\\w+"), "")
        .trim()
    return cleaned.take(500).ifBlank { text.take(500) }
}

companion object {
    var shouldBlurResult: Boolean = false  // NEW: flag buat blur
}
```

### Step 11: Update `OCRProcessor.kt` — Blur Screen Dulu, Baru Proses

```kotlin
// Di onHandleIntent — screen capture udah selesai, bitmap di cache
override fun onHandleIntent(intent: Intent?) {
    val imagePath = intent?.getStringExtra("image_path") ?: return
    val file = File(imagePath)
    if (!file.exists()) return

    val bitmap = BitmapFactory.decodeFile(imagePath) ?: run { file.delete(); return }

    // BLUR DULU — tampilin blurred screenshot sebelum processing
    // Biar user langsung liat kontennya di-blur, feel secure
    ScreenBlurOverlay.showBlur(this, bitmap.copy(bitmap.config, false))

    // Baru OCR di background
    val image = InputImage.fromBitmap(bitmap, 0)

    recognizer.process(image)
        .addOnSuccessListener { result ->
            val text = result.text
            if (text.isNotBlank()) {
                verifyText(text)
            } else {
                showResult(-1L, "Tidak ada teks terdeteksi.",
                    "Tidak ada teks terdeteksi.", "Unknown", 0.0, emptyList())
            }
            bitmap.recycle()
            file.delete()
        }
        .addOnFailureListener { e ->
            showResult(-1L, "OCR error: ${e.localizedMessage}",
                "OCR error: ${e.localizedMessage}", "Error", 0.0, emptyList())
            bitmap.recycle()
            file.delete()
        }
}

// Update verifyText — cek blur flag
private fun verifyText(text: String) {
    val engine = VerdictEngine()
    try {
        val response = runBlocking { engine.verify(text) }
        val historyId = HistorySaver.saveToHistory(this, text, response)

        // Kalo backend bilang shouldBlur = false (Aman), dismiss blur
        if (!VerdictEngine.shouldBlurResult) {
            ScreenBlurOverlay.dismiss()
        }
        // Kalo true, blur tetep tampil sampe user dismiss manual
        // Atau dismiss otomatis pas result overlay muncul

        showResult(historyId, response.claim, response.explanation,
            response.verdict, response.confidence, response.sources)
    } catch (e: Exception) {
        showResult(-1L, text, "Error: ${e.localizedMessage ?: "Unknown error"}", "Error", 0.0, emptyList())
    }
}
```

### Step 12: Update `ResultOverlayHelper.kt` — Dismiss Blur Saat Result Muncul

```kotlin
fun showResult(
    context: Context,
    historyId: Long,
    claim: String,
    explanation: String,
    verdict: String,
    confidence: Double,
    sources: List<Source>,
    flaggedItems: List<FlaggedItem> = emptyList()
) {
    // Dismiss blur overlay — result udah siap, ganti sama bottom sheet
    ScreenBlurOverlay.dismiss()

    val wrapper = OverlayViewFactory.createResultOverlayView(
        context = context,
        explanation = explanation,
        verdict = verdict,
        confidence = confidence,
        sources = sources,
        onDismiss = { dismiss() },
        onViewDetails = { /* open detail screen */ },
        flaggedItems = flaggedItems
    )
    // ... existing code ...
}
```

### Step 13: Update `OverlayCardViewFactory.kt` — Scam Labels

```kotlin
fun getVerdictColor(verdict: String): Int = when (verdict.lowercase()) {
    "supported" -> 0xFF00AA44.toInt()
    "misleading", "unsupported" -> 0xFFFF8C00.toInt()
    "contradicted" -> 0xFFDD3333.toInt()
    else -> 0xFFFF8C00.toInt()
}

fun createBadge(context: Context, verdict: String, color: Int, px: (Int) -> Int) = createTextView(context) {
    val label = when (verdict.lowercase()) {
        "supported" -> "\u2705 Aman"
        "misleading" -> "\u26A0\uFE0F Mencurigakan"
        "contradicted" -> "\uD83D\uDEA8 Terindikasi Penipuan"
        else -> verdict
    }
    text = label
    setTextColor(0xFFFFFFFF.toInt())
    textSize = 12f
    typeface = Typeface.DEFAULT_BOLD
    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        setCornerRadius(px(6).toFloat())
    }
    setPadding(px(8), px(4), px(8), px(4))
}
```

### Step 14: Update `OverlayViewFactory.kt` — Flagged Items in Overlay

```kotlin
fun createResultOverlayView(
    context: Context,
    explanation: String,
    verdict: String,
    confidence: Double,
    sources: List<Source>,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    flaggedItems: List<FlaggedItem> = emptyList()
): View {
    val density = context.resources.displayMetrics.density
    val px = { v: Int -> (v * density).toInt() }

    val card = OverlayCardViewFactory.createCard(context, px)
    val verdictColor = OverlayCardViewFactory.getVerdictColor(verdict)

    card.addView(OverlayCardViewFactory.createBadge(context, verdict, verdictColor, px))
    card.addView(OverlayCardViewFactory.createConfidenceText(context, confidence, verdictColor, px))
    card.addView(OverlayCardViewFactory.createVerdictLabel(context, verdict, px))

    if (flaggedItems.isNotEmpty()) {
        card.addView(OverlayCardViewFactory.createFlaggedItemsView(context, flaggedItems, px))
    }

    if (explanation.isNotEmpty()) {
        card.addView(OverlayCardViewFactory.createExplanationView(context, explanation, px))
    }
    card.addView(OverlayCardViewFactory.createSourcesText(context, sources.size, px))
    card.addView(OverlayCardViewFactory.createButtonRow(context, px, onDismiss, onViewDetails))

    return OverlayCardViewFactory.createWrapper(context, card, px, onDismiss)
}
```

### Step 15: Update `strings.xml`

```xml
<resources>
    <string name="app_name">AntiTimpa</string>
</resources>
```

### Step 16: Update `FactLensApp.kt`

```kotlin
VerdictEngine.USE_MOCK = false
```

### Step 17: `ScanResultScreen.kt` — Scam Flagged Items Section

```kotlin
if (scanResult.claim.contains("•")) {
    val items = scanResult.explanation.split("\n")
        .filter { it.trim().startsWith("•") }
    if (items.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = "Item Terdeteksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(Spacing.sm))
                items.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = item.trim().removePrefix("• "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }
            }
        }
        Spacer(Modifier.height(Spacing.lg))
    }
}
```

### Step 18: Update OverlayService.kt — Long-Press Trigger + Blur Flow

```kotlin
// Di FloatingTriggerButton — ubah tap jadi long-press 2 detik
@Composable
fun FloatingTriggerButton(onTap: () -> Unit) {
    var pressStartTime by remember { mutableStateOf(0L) }
    var isPressing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    Box {
        // Pulse ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color(0xFF00497D).copy(alpha = 0.3f))
        )

        // Main button with long-press
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF00497D))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            // Haptic feedback
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(100)
                            }
                            onTap()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Progress ring — drawArc dari 0 ke 360 selama 2 detik
            Canvas(modifier = Modifier.size(56.dp)) {
                val sweep = (progress * 360).coerceAtMost(360f)
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Icon(
                Icons.Filled.Security,
                contentDescription = "Verify",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
```

---

## API Contract

### Backend → Android

```
POST /check-scam
Request:  { "text": "Halo kak, kami dari BCA... transfer ke 1234567890" }

Response: {
  "verdict": "Terindikasi Penipuan",
  "riskScore": 70,
  "shouldBlur": true,
  "flaggedItems": [
    { "type": "account", "value": "1234567890", "reason": "Nomor rekening terdeteksi" },
    { "type": "pattern", "value": "transfer sekarang", "reason": "Pola bahasa penipuan" }
  ],
  "explanation": "Pesan ini mengandung indikasi penipuan...",
  "sources": ["Rekening terdaftar di cekrekening.id"]
}
```

### Verdict Mapping

| Scam Verdict | Mapped To | Color | Blur? |
|---|---|---|---|
| Terindikasi Penipuan | Contradicted | Merah 🚨 | ✅ Ya |
| Mencurigakan | Misleading | Oranye ⚠️ | ✅ Ya |
| Aman | Supported | Hijau ✅ | ❌ Tidak |

---

## User Flow (Final)

```
1. User scroll IG/WA/TikTok — liat postingan soal pinjol/rekening
2. Hold floating button 2 detik → [haptic buzz]
3. Screen capture → BLUR OVERLAY LANGSUNG MUNCUL
   ─ User lihat konten scam dalam keadaan blur
   ─ Processing jalan di background
4. Result bottom sheet muncul → blur hilang
   a. 🚨 SCAM → blur merah + warning
   b. ✅ AMAN → tidak blur, hasil "Aman"
5. Tap dismiss → balik ke IG normal
```

---

## Privacy & Security

| Tahap | Data | Jaringan | Keterangan |
|---|---|---|---|
| Screen capture | Bitmap di memory | ❌ No | Langsung diproses, auto-delete |
| Blur overlay | Bitmap di memory | ❌ No | Pure on-device |
| OCR (ML Kit) | Teks dari gambar | ❌ No | On-device, gak keluar |
| Scam check | Teks (bukan gambar) | ✅ Yes | Cuma teks OCR, minimal |
| History (Room DB) | Claim, verdict | ❌ No | Lokal, gak diupload |
| **Screenshot** | **Tidak pernah dikirim** | — | **Zero upload policy** |

---

## Persiapan Sebelum Coding

1. **Test cekrekening.id API:**
   ```bash
   curl https://cekrekening.id/api/reports/BCA/1234567890
   ```

2. **Update OJK pinjol ilegal list** — cek web OJK, update `OJK_PINJOL_ILEGAL`

3. **Backend LLM key** (opsional — fallback tetep jalan):
   ```
   GEMINI_API_KEY=your-key-here
   ```

---

## Files Changed Summary

| File | Action |
|---|---|
| `backend/scam_extractor.py` | **NEW** |
| `backend/scam_checker.py` | **NEW** |
| `backend/risk_scorer.py` | **NEW** |
| `android/.../overlay/ScreenBlurOverlay.kt` | **NEW** — blur overlay |
| `backend/models.py` | EDIT — +ScamCheck*, FlaggedItem, shouldBlur |
| `backend/main.py` | EDIT — +/check-scam endpoint |
| `backend/llm_client.py` | EDIT — +SCAM_SYSTEM_PROMPT |
| `android/.../model/Models.kt` | EDIT — +ScamCheck*, FlaggedItem |
| `android/.../network/BackendClient.kt` | EDIT — +checkScam() |
| `android/.../network/VerdictEngine.kt` | EDIT — +scam detection |
| `android/.../ocr/OCRProcessor.kt` | EDIT — blur before processing |
| `android/.../overlay/OverlayService.kt` | EDIT — long-press trigger |
| `android/.../overlay/ResultOverlayHelper.kt` | EDIT — dismiss blur on result |
| `android/.../overlay/OverlayCardViewFactory.kt` | EDIT — scam labels |
| `android/.../overlay/OverlayViewFactory.kt` | EDIT — flagged items |
| `android/.../ui/screens/ScanResultScreen.kt` | EDIT — flagged items section |
| `android/.../res/values/strings.xml` | EDIT — app name |
| `android/.../FactLensApp.kt` | EDIT — USE_MOCK=false |
