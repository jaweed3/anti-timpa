# AGENTS.md — AntiTimpa

> Scam detection Android app: FAB overlay → screen capture → OCR → pattern-based
> backend check → result overlay with blur.
> **Tagline:** *"Blur scams before they blur your judgment."*
> **Tim:** 2 Dev (A = Backend Python, B = Android Kotlin)
> **Status:** Hackathon MVP

---

## Module Map

```
root/
├── android/                          # Android app (Kotlin, Compose)
│   └── app/src/main/java/com/factlens/
│       ├── overlay/                  # WindowManager overlay system
│       ├── ocr/                      # ML Kit OCR + history persistence
│       ├── network/                  # Backend client + fallback
│       ├── capture/                  # MediaProjection token management
│       ├── history/                  # Room database
│       ├── model/                    # Data classes + entities
│       ├── ui/                       # Compose screens + components + theme
│       ├── MainActivity.kt           # Activity + permission launchers
│       ├── AppNavigation.kt          # Compose navigation + state
│       └── FactLensApp.kt            # Application class
│
├── backend/                          # Python backend
│   └── anti_timpa/
│       ├── app.py                    # FastAPI server
│       ├── config.py                 # Environment config
│       ├── extractor.py              # Regex extraction (phone, account, URL)
│       ├── checker.py                # URL safety checker
│       ├── patterns.py               # Scam keyword patterns
│       ├── scorer.py                 # Risk scoring + verdict mapping
│       ├── llm.py                    # NaraRouter LLM client
│       ├── search.py                 # DuckDuckGo search
│       ├── pipeline.py               # Search → rank → LLM → verdict
│       ├── claim.py                  # Claim extractor
│       ├── ranker.py                 # Source ranking
│       ├── models.py                 # Pydantic models
│       └── requirements.txt          # Python dependencies
│
├── AGENTS.md                         # ← this file
├── README.md                         # For humans
├── specs.md                          # Original product spec (keep as ref)
├── design_refs/                      # Design references
└── backend/docs_nara.md              # NaraRouter API docs
```

---

## Android — File-by-File

### `overlay/` — WindowManager Overlay System

| File | Role | Key Detail |
|------|------|------------|
| `OverlayService.kt` | **Entry point.** Service yang manage FAB, projection, capture | `startCapture()` → blur → screenshot → OCR. Persistent `MediaProjection` + `VirtualDisplay`. Broadcast receiver for `START_CAPTURE`, `SCAN_COMPLETE`. |
| `ScreenBlurOverlay.kt` | Fullscreen blur/red overlay di atas semua app | `showScanningOverlay()` → red + spinner. `updateToBlurredScreenshot()` → ganti red jadi blurred bitmap. `hideProgress()` → spinner ilang. `dismiss()` → remove. |
| `ResultOverlayHelper.kt` | Result card overlay (WindowManager) | Muncul di **atas** blur. `dismiss()` → remove result + dismiss blur. User-initiated dismiss = blur ikut ilang. |
| `OverlayFabHelper.kt` | FAB lifecycle + drag handling | Wires touch handler, view factory, activator. |
| `OverlayFabTouchHandler.kt` | Drag + long-press 2s → `triggerCapture()` | `Handler.postDelayed(2000)` → `activator.activate()`. Kalo drag detected sebelum 2s → cancel. |
| `OverlayFabActivator.kt` | Visual feedback (vibrate + color hijau) | Dipanggil pas hold 2s complete. |
| `OverlayFabViewFactory.kt` | Layout FAB (pulse ring + icon "F") | Animasi pulse infinite. |
| `OverlayViewFactory.kt` | Layout result card | Delegates ke `OverlayCardViewFactory`. |
| `OverlayCardViewFactory.kt` | Komponen result card (badge, confidence, flagged items, buttons) | Verdict → emoji: ✅ Aman, ⚠️ Mencurigakan, 🚨 Terindikasi Penipuan. |
| `OverlayIndicatorHelper.kt` | Bottom scanning indicator (kecil di bawah layar) | Mostly unused setelah blur overhaul. Aman dihapus nanti. |
| `OverlayIndicatorViewBuilder.kt` | Layout untuk indicator di atas | — |

### `ocr/` — OCR & History

| File | Role | Key Detail |
|------|------|------------|
| `OCRProcessor.kt` | IntentService. Baca screenshot → ML Kit OCR → VerdictEngine → show result | Jalan di background thread. Intent extra: `image_path`. |
| `HistorySaver.kt` | Save scan result ke Room DB | `saveToHistory(context, text, response, screenshotPath)` |

### `network/` — Backend Communication

| File | Role | Key Detail |
|------|------|------------|
| `VerdictEngine.kt` | Orchestrator verifikasi | `verify(text)`: `/check-scam` dulu. Kalo "Aman" → `/verify` (search + LLM). Kalo error → fallback local. `USE_MOCK = false`. `backendUrl` changeable via Settings. |
| `BackendClient.kt` | OkHttp client | `checkScam()` → POST `/check-scam`. `verify()` → POST `/verify`. Field names **snake_case** (`risk_score`, `flagged_items`, `should_blur`). |
| `GeminiClient.kt` | Fallback LLM (unused?) | — |
| `SearchClient.kt` | Local search fallback | — |

### `capture/` — Projection Token

| File | Role |
|------|------|
| `ScreenCaptureManager.kt` | Simpan projection result code + intent data (companion object + SharedPreferences) |
| `ScreenCaptureHelper.kt` | Likely unused |

### `history/` & `model/`

| File | Role |
|------|------|
| `HistoryDatabase.kt` | Room DB + DAO (getAll, getFavorites, insert, delete, toggleFavorite) |
| `Models.kt` | `ScanHistory` entity, `VerificationResponse`, `ScamCheckResponse`, `FlaggedItem`, `Source` |

### `ui/` — Compose Screens

| File | Screen |
|------|--------|
| `screens/HomeScreen.kt` | Home with recent scans |
| `screens/SetupScreen.kt` | First-run: overlay + recording permission |
| `screens/SettingsScreen.kt` | Overlay toggle, permission, Gemini key, **Server URL**, clear history |
| `screens/HistoryScreen.kt` | All scan history |
| `screens/ScanResultScreen.kt` | Full scan result detail |
| `screens/ScanningScreen.kt` | Scanning progress (in-app, unused in overlay flow) |
| `screens/SavedScreen.kt` | Favorited scans |
| `screens/FloatingResultOverlay.kt` | Compose version of result overlay? |
| Others | Various components |

### Config & Entry

| File | Role |
|------|------|
| `MainActivity.kt` | Single Activity. Permission launchers (overlay, notification, media projection). Launch service. |
| `AppNavigation.kt` | Navigation state (setup, home, scanning, history, settings, etc.). `BackHandler` intercepts system back — only exits app from `home` or `setup`. Backend URL loaded from SharedPreferences. |
| `FactLensApp.kt` | Application class |

---

## Android — Architecture Flow

```
FAB hold 2s → triggerCapture()
  ↓
hasProjection()?
  YES → startCapture() langsung
  NO  → launch MainActivity → system dialog
         → GRANT → moveTaskToBack + sendBroadcast(START_CAPTURE)
  ↓
startCapture():
  1. ScreenBlurOverlay.showScanningOverlay(this)  // merah + spinner tengah
  2. ensureMediaProjection()                       // getMediaProjection (companion token)
  3. ensureVirtualDisplay()                        // createVirtualDisplay + ImageReader
  4. setOnImageAvailableListener + 500ms fallback  // capture screenshot
  ↓ timeout 30s → releaseVirtualDisplay() + ScreenBlurOverlay.dismiss()
  ↓
completeCapture → releaseVirtualDisplay()          // VD dilepas, recording indicator ilang (≤13)
  ↓
processAndSaveImage():
  1. Image planes → ARGB_8888 bitmap
  2. ScreenBlurOverlay.updateToBlurredScreenshot()  // merah → blurred screenshot
  3. Save PNG to filesDir/screenshots/
  4. Start OCRProcessor IntentService
  ↓
OCRProcessor:
  1. Load bitmap, ML Kit OCR → text
  2. VerdictEngine.verify(text)
     → /check-scam (pattern-based, no internet)
       ↓                     ↓
     "Terindikasi/"        "Aman" → /verify (search + LLM)
     "Mencurigakan"                 ↓ → fallback
       ↓                     ↓
     return as result       return as result
  3. HistorySaver.saveToHistory(text, response, screenshotPath)
  4. ScreenBlurOverlay.hideProgress()  // spinner hilang
  5. ResultOverlayHelper.showResult()  // result card DI ATAS blur
  ↓
User tap Dismiss:
  ResultOverlayHelper.dismiss()
  → remove result card → sendBroadcast(SCAN_COMPLETE)
  → ScreenBlurOverlay.dismiss()  // blur ilang

┌── Android 14 Re-scan ─────────────────────────────────────────┐
│ ensureVirtualDisplay() gagal (VD exhausted per projection)     │
│   → releaseMediaProjection() + clearProjection()               │
│   → triggerCapture() → system dialog re-grant                  │
│   → new MediaProjection + new VirtualDisplay → capture works   │
│   → tiap 2 scan sekali re-grant (unavoidable)                  │
└────────────────────────────────────────────────────────────────┘
```

---

## Backend — Person A Reference

### Endpoints (FastAPI)

| Method | Path | Auth | Function |
|--------|------|------|----------|
| `GET` | `/health` | No | `{"status":"ok","llm":true/false,"nara_model":"agnes-2.0-flash"}` |
| `POST` | `/check-scam` | No | Pattern-based scam detection. No LLM, no internet needed. |
| `POST` | `/verify` | No | Search + LLM fact-check. Falls back if search/LLM fails. |
| `GET` | `/verify?text=...` | No | Same as POST (for curl testing). |

### Endpoint Details

**`POST /check-scam`**
```
Request:  {"text": "string"}
Response: {
  "verdict": "Aman" | "Mencurigakan" | "Terindikasi Penipuan",
  "risk_score": int (0-100),
  "flagged_items": [{"type": "account"|"phone"|"url"|"pattern", "value": "...", "reason": "..."}],
  "explanation": "string",
  "sources": ["string"],
  "should_blur": bool
}
```

**`POST /verify`**
```
Request:  {"text": "string", "language": "id" | "en"}
Response: {
  "claim": "string",
  "verdict": "Supported" | "Contradicted" | "Misleading" | "Mixed" | "Insufficient Evidence",
  "confidence": float (0.0-0.95),
  "explanation": "string",
  "sources": [{"title": "...", "url": "...", "snippet": "..."}]
}
```

### Scam Detection Pipeline (`/check-scam`)

1. **extract_indicators(text)** → phone numbers, account numbers, URLs, bank names
2. **match_patterns(text)** → scam keywords, urgency language, judol terms
3. **check_url_safety(urls)** → shortener detection, risky TLDs
4. **score(indicators, patterns, urls)** → risk score 0-100
5. **get_verdict(score)** → Aman (0-30), Mencurigakan (31-60), Terindikasi Penipuan (61-100)
6. **Build response** with flagged items + explanation

### Verification Pipeline (`/verify`)

1. `claim.extract_claim(text)` or fallback to `text[:500]`
2. `search.search(claim)` → DuckDuckGo, max 5 sources
3. `ranker.rank_sources(sources, claim)` → sort by relevance
4. `llm.chat(system_prompt, prompt)` → NaraRouter (OpenAI-compatible)
5. Parse JSON response → `VerificationResponse`
6. Fallback if LLM fails → `_fallback()` (Insufficient Evidence)

### NaraRouter (LLM)

```
Base URL: https://api.nararouter.com/v1
Model:    agnes-2.0-flash
Key:      sk-nry-unnwS-6OrlOlP7OHXF0ln_1HmuGO_oNbsMXOtQ6u2s8
SDK:      openai.OpenAI(base_url=base_url, api_key=api_key)
Endpoint: /chat/completions
Format:   ChatCompletion.choices[0].message.content
```

### Environment Config

File: `backend/anti_timpa/.env` (gitignored)

```
NARA_ROUTER_API=sk-nry-unnwS-...
nara_model=agnes-2.0-flash
```

Loaded in `config.py` via `python-dotenv`. Access via `config.nara_api_key` (env var `NARA_ROUTER_API`), `config.nara_model`.

### Testing

```bash
# Check-scam
curl -X POST http://localhost:8000/check-scam \
  -H "Content-Type: application/json" \
  -d '{"text":"Halo kak, saya dapat hadiah dari BCA. Transfer Rp 5.000.000 ke rek 1234567890 a/n Saya. Jangan lewatkan kesempatan ini!"}'

# Verify
curl -X POST http://localhost:8000/verify \
  -H "Content-Type: application/json" \
  -d '{"text":"Apakah bumi itu bulat?","language":"id"}'

# Health
curl http://localhost:8000/health
```

### Run Backend

```bash
cd backend/anti_timpa
python -m venv venv && source venv/bin/activate  # Linux/Mac
# or .\venv\Scripts\activate  # Windows
pip install -r requirements.txt
uvicorn anti_timpa.app:app --host 0.0.0.0 --port 8000 --reload
```

---

## Key Decisions

### Why Persistent MediaProjection (not per-scan)?
Android 14 `getMediaProjection()` crashes if called 2x with same token.
You can't destroy and recreate VirtualDisplay without re-granting permission.
**Trade-off:** orange recording indicator stays on while OverlayService runs.

### Why `/check-scam` before `/verify`?
`/check-scam` is pure pattern matching — no internet, instant response.
Only fall through to `/verify` (search + LLM) when result is "Aman" or on error.

### Why Red → Blurred → Result On Top?
Spec says blur must appear BEFORE the result, and user must dismiss blur manually.
Flow: red (scanning) → blurred screenshot (captured) → hide spinner → show result card on top → user dismiss → blur + result gone.

### Why snake_case in BackendClient?
Backend Python returns `risk_score`, `flagged_items`, `should_blur`.
NOT camelCase. Android client must match exactly.

### Why `foregroundServiceType` includes `mediaProjection` even if it causes crashes?
Android 14 requires `mediaProjection` type for BOTH:
- `getMediaProjection()` (to acquire the projection token)
- `startForeground()` (to stay in foreground while projecting)
The crash happens when `startForeground()` is called while MediaProjection is
already stopped. **Fix:** `onStartCommand()` only calls `startForeground()` once
(via `!overlayCreated` guard). Subsequent starts skip it — the service stays
in foreground from the first call.

---

## Android Gotchas

| Gotcha | File | Detail |
|--------|------|--------|
| Looper import | `OverlayService.kt` | `Handler(Looper.getMainLooper())` requires explicit `import android.os.Looper` |
| registerCallback order | `OverlayService.kt:166` | Must call BEFORE `createVirtualDisplay()` on Android 14 |
| ImageReader planes | `OverlayService.kt:238-243` | RGBA_8888 → ARGB_8888. `rowStride / pixelStride` assumes no padding |
| Token lifetime | `ScreenCaptureManager` | Stored in companion + SharedPreferences. Service restart → need re-grant |
| Broadcast NOT_EXPORTED | `OverlayService.kt:312` | Required on API 33+ for local-only broadcasts |
| WindowManager type | `ScreenBlurOverlay.kt` | `TYPE_APPLICATION_OVERLAY` required on API 26+ |
| Result overlay z-order | `ResultOverlayHelper.kt` | Added AFTER blur → WindowManager renders on top automatically |
| Android 14 `startForeground` crash | `AndroidManifest.xml:35` | `startForeground()` crashes after `MediaProjection.stop()` on API 34+. `onStartCommand()` guards it behind `!overlayCreated` to only call it once. But `mediaProjection` type MUST stay in manifest — `getMediaProjection()` needs it. |
| `lateinit` ordering in `onCreate` | `MainActivity.kt:37-51` | All `registerForActivityResult()` MUST be called BEFORE checking intent extras that trigger launcher usage |
| System back navigation | `AppNavigation.kt:98-105` | `BackHandler` intercepts back on all screens except `home` and `setup`. Only `home` → back exits app. |

## Backend Gotchas

| Gotcha | File | Detail |
|--------|------|--------|
| Search timeout | `search.py` | DuckDuckGo often blocked behind firewall / no-internet → `/verify` falls back |
| LLM response parsing | `pipeline.py:94-109` | `_parse_llm_response()` regex extracts `{...}` JSON. Can fail if LLM returns malformed JSON |
| .env location | `config.py` | Must be in `backend/anti_timpa/.env`, NOT root. Gitignored |
| NaraRouter key | `.env` | Key visible in code history. Rotate after hackathon |

## Conventions

- **Android:** Kotlin, OkHttp (not Retrofit), Room, ML Kit, Compose, WindowManager overlay
- **Backend:** Python FastAPI, pydantic, httpx (search), openai SDK (NaraRouter)
- **Package:** `com.factlens.*` (legacy name, not changed from FactLens era)
- **Broadcast actions:** `"com.factlens.START_CAPTURE"`, `"com.factlens.SCAN_COMPLETE"`, etc.
- **SharedPreferences file:** `"factlens_prefs"` (keys: `overlay_visible`, `backend_url`)
- **Screenshot storage:** `filesDir/screenshots/` (internal storage, permanent)

## Agent Instructions

When modifying code:
1. Read this file first for context
2. Check `Module Map` to find the right file
3. Check `Key Decisions` before changing architecture
4. Check `Gotchas` before touching sensitive areas
5. After changes, verify with logcat `AntiTimpa.*` + `FactLens.*`

For backend changes (Person A):
- Refer to `Backend — Person A Reference` section
- Test with curl commands provided
- Check `backend/API.md` for full API documentation
- Check `backend/docs_nara.md` for LLM API details
