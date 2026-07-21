# FactLens (antiTimpa) — Status & Deep Analysis

> **Project:** AI-powered Android overlay fact-checker (Gemini AI Hackathon 2025)
> **Tagline:** "Verify information directly from your screen"
> **Status:** MVP / Proof of Concept (v1.1)

---

## 1. Project Overview

FactLens enables users to verify suspicious information directly from their screen **without leaving the current app**. A floating overlay button accessible from any app (Instagram, TikTok, WhatsApp, X, etc.) captures the screen, extracts text via on-device OCR, retrieves evidence from trusted sources, and delivers an AI-backed fact-check verdict.

---

## 2. Features

### 2.1 Core Features

| Feature | Description |
|---------|-------------|
| **Floating Overlay Button** | Always-accessible "F" button hovering over all apps, draggable with snap-to-edge behavior |
| **Screen Capture** | One-tap long-press -> Android `MediaProjection API` captures current screen |
| **On-Device OCR** | Google ML Kit text recognition (English + Indonesian) |
| **Claim Detection** | Extracts factual claims from OCR text, filters opinions/jokes/ads via heuristics |
| **Evidence Retrieval** | Searches trusted sources (Reuters, AP, BBC, WHO, academic, government) via DuckDuckGo (no API key) |
| **AI Verdict** | 6 verdict types with confidence scores + source-backed explanations |
| **History** | Room database stores all scans locally |
| **No Login Required** | Zero friction, open and use |

### 2.2 Verdict Types

| Verdict | Meaning |
|---------|---------|
| **Supported** | Evidence supports the claim |
| **Contradicted** | Evidence contradicts the claim |
| **Misleading** | Partially true but presented deceptively |
| **Mixed** | Conflicting evidence |
| **Insufficient Evidence** | Not enough reliable sources |
| **Unknown** | Could not process |

### 2.3 UI Screens

| Screen | Purpose | Status |
|--------|---------|--------|
| **Setup** | First-launch wizard: overlay permission, screen recording permission | Done |
| **Home** | Dashboard with quick-scan, recent scans, stats | Mock data |
| **Scan Result** | Full verdict with claim, explanation, confidence bar, source list | Done |
| **History** | List of all past scans from Room database | Done |
| **Saved** | Bookmarked/favorite scans | Mock/stub |
| **History Detail** | Full scan detail view for a single history item | Done |
| **Settings** | Overlay permission toggle, Gemini API Key input, overlay visibility toggle, clear history | Partial |

---

## 3. Architecture

### 3.1 System Diagram

```
+------------------------------------------------------------------+
|                      ANDROID APP (Kotlin)                        |
|                                                                   |
|  +----------+   +------------+   +-----------+   +-----------+   |
|  | Overlay  |-> | Screen     |-> | ML Kit    |-> | Verdict   |   |
|  | Service  |   | Capture    |   | OCR       |   | Engine    |   |
|  | (FAB +   |   | Service    |   | Processor |   | --------- |   |
|  | indicator)|  | (MediaProj)|   |           |   | Search    |   |
|  +----------+   +------------+   +-----------+   | Gemini   |   |
|                                                   | Backend  |   |
|  +----------+                                    | Fallback |   |
|  | Result   |                                    +-----+-----+   |
|  | Overlay  |                                          |         |
|  | Helper   |                                          v         |
|  +----+-----+                                   +----------+     |
|       +-----------------------------------------> Room DB  |     |
|                save scan history                 | (SQLite) |     |
+--------------------------------------------------+----------+----+
                                                          |
                                                          v
+------------------------------------------------------------------+
|                      BACKEND (Python/FastAPI)                     |
|                                                                   |
|  +-------------+   +--------------+   +----------------------+    |
|  | Claim      |-> | DuckDuckGo   |-> | Ranker               |    |
|  | Detector   |   | Search       |   | (domain authority)   |    |
|  +-------------+   +--------------+   +----------+-----------+    |
|                                                    |              |
|                                                    v              |
|  +------------------------------------------------------------+   |
|  | LLM Client (NaraRouter/Gemini API)                         |   |
|  | Prompt: claim + sources -> verdict + confidence +          |   |
|  |           explanation                                      |   |
|  +------------------------------------------------------------+   |
+------------------------------------------------------------------+
```

### 3.2 Application Layers

| Layer | Technology | Components |
|-------|------------|------------|
| **Presentation** | Jetpack Compose + Material 3 | `AppNavigation.kt`, all `ui/screens/*.kt`, `ui/components/*.kt` |
| **Overlay (System Window)** | Plain Android Views (WindowManager) | `OverlayService.kt`, `OverlayFabHelper.kt`, `OverlayIndicatorHelper.kt`, `ResultOverlayHelper.kt` |
| **Capture** | MediaProjection API | `ScreenCaptureService.kt`, `ScreenCaptureHelper.kt`, `ScreenCaptureManager.kt` |
| **OCR** | Google ML Kit (TextRecognition) | `OCRProcessor.kt` (IntentService) |
| **Verification** | Custom orchestration | `VerdictEngine.kt`, `SearchClient.kt`, `BackendClient.kt`, `GeminiClient.kt` |
| **Persistence** | Room (SQLite) | `HistoryDatabase.kt`, `HistorySaver.kt` |
| **Notifications** | NotificationManager | `ScanNotifier.kt` |
| **Backend** | FastAPI (Python) + Docker | `main.py`, `verification_pipeline.py`, `search_engine.py`, `llm_client.py`, `claim_detector.py`, `ranker.py` |

---

## 4. Code Structure

### 4.1 Android App (`android/app/src/main/java/com/factlens/`)

```
com.factlens/
+-- FactLensApp.kt              # Application: sets USE_MOCK=true, crash logger
+-- MainActivity.kt             # Single Activity: intent handlers, permission launchers
+-- AppNavigation.kt            # Compose navigation: all screen routing + state management
+-- PermissionActivity.kt       # Transparent activity for screen recording permission
|
+-- capture/
|   +-- ScreenCaptureManager.kt # Stores/retrieves MediaProjection code+data via SharedPreferences
|   +-- ScreenCaptureService.kt # Foreground Service: starts virtual display -> captures screenshot
|   +-- ScreenCaptureHelper.kt  # Helper: creates VirtualDisplay, captures ImageReader->bitmap->file
|
+-- history/
|   +-- HistoryDatabase.kt      # Room @Database: ScanHistory entity, DAO
|
+-- model/
|   +-- Models.kt               # Data classes: VerificationRequest/Response, Source, ScanHistory
|
+-- network/
|   +-- SearchClient.kt         # DuckDuckGo HTML scraper (no API key), parses results
|   +-- BackendClient.kt        # Retrofit/HTTP client for FactLens FastAPI backend
|   +-- GeminiClient.kt         # Gemini API client (Google AI SDK)
|   +-- VerdictEngine.kt        # Orchestrator: mock -> search -> backend -> Gemini -> fallback
|
+-- ocr/
|   +-- OCRProcessor.kt         # IntentService: ML Kit OCR -> VerdictEngine -> result overlay
|   +-- HistorySaver.kt         # Saves ScanHistory to Room DB
|   +-- ScanNotifier.kt         # Shows Android notification with verdict result
|
+-- overlay/
|   +-- OverlayService.kt       # Foreground Service: lifecycle, broadcast receiver, show/hide FAB
|   +-- OverlayFabHelper.kt     # Creates and manages FAB view + layout params + touch handler
|   +-- OverlayFabViewFactory.kt# Static factory: creates FAB container + pulse + icon + animator
|   +-- OverlayFabActivator.kt  # Animates FAB color/state on long-press activation
|   +-- OverlayFabTouchHandler.kt# Handles drag (snap-to-edge), long-press (2s) activation
|   +-- OverlayIndicatorHelper.kt# Manages scanning indicator overlay (show/hide/timeout)
|   +-- OverlayIndicatorViewBuilder.kt # Static factory: scanning indicator + permission views
|   +-- OverlayViewFactory.kt   # Static factory: result overlay card (verdict, explanation, sources)
|   +-- OverlayCardViewFactory.kt # Sub-factories for card sections: header, explanation, sources
|   +-- ResultOverlayHelper.kt  # Singleton: shows/dismisses result overlay, launches View Details

ui/
+-- screens/
|   +-- SetupScreen.kt          # Permission request wizard (overlay + recording)
|   +-- HomeScreen.kt           # Dashboard with quick scan, recent items (mock)
|   +-- ScanResultScreen.kt     # Full scan result: verdict chip, confidence bar, explanation, sources
|   +-- HistoryScreen.kt        # All past scans list from Room
|   +-- SavedScreen.kt          # Favorite/bookmarked scans (mock)
|   +-- HistoryDetailScreen.kt  # Full scan detail from history
|   +-- SettingsScreen.kt       # Overlay permission, Gemini API key, overlay toggle, clear history
+-- components/
|   +-- Common.kt               # Shared composables: SectionHeader, ValueStat, EmptyHistoryView
+-- theme/
    +-- Theme.kt                # Colors, typography, spacing, shapes, FactLensTheme
```

### 4.2 Backend (`backend/`)

```
backend/
+-- main.py                     # FastAPI app: /health, /verify (POST/GET), rate limit, CORS, caching
+-- models.py                   # Pydantic models: VerificationRequest, Source, VerificationResponse
+-- config.py                   # Env-based config: NaraRouter API key, trusted domains, rate limit
+-- verification_pipeline.py    # Orchestrator: claim detection -> search -> rank -> LLM verdict
+-- claim_detector.py           # Regex-based claim extraction + opinion filtering
+-- search_engine.py            # DuckDuckGo HTML scraper (httpx + BeautifulSoup)
+-- llm_client.py               # OpenAI-compatible client for NaraRouter API (with retry)
+-- ranker.py                   # Score-based source ranking by domain authority + keyword overlap
+-- Dockerfile                  # Python 3.12-slim, uvicorn
+-- docker-compose.yml          # api service: builds backend, loads .env, port 8000
+-- requirements.txt            # fastapi, uvicorn, openai, httpx, beautifulsoup4, pydantic, ...
+-- .env.example                # NARA_ROUTER_API, SERVER_HOST, SERVER_PORT
+-- pyproject.toml              # Project metadata + dev dependencies (pytest)
+-- tests/
    +-- test_api.py             # FastAPI TestClient tests
    +-- test_claim_detector.py  # Unit tests for extract_claim / is_claim
```

---

## 5. Application Flow

### 5.1 App Startup

```
App Launch
    |
    v
MainActivity.onCreate()
    |
    +-- Check overlay permission (Settings.canDrawOverlays)
    |   +-- If denied -> show SetupScreen with permission buttons
    |   +-- If granted -> immediate navigate to "home"
    |   +-- Start OverlayService (startForegroundService)
    |
    +-- Check intent extras:
    |   +-- "trigger_capture" -> triggerCaptureRequested = true
    |   +-- "open_scan_result" -> navigateToScanResult = true
    |   +-- "open_history_detail" -> navigateToHistoryDetail = true
    |
    +-- Register permission launchers:
        +-- overlayPermissionLauncher
        +-- notificationPermissionLauncher
        +-- mediaProjectionLauncher
```

### 5.2 Floating Overlay Lifecycle

```
OverlayService.onCreate()
    |
    +-- Get WindowManager service
    +-- Create notification channel
    +-- Register broadcast receiver (SCAN_COMPLETE, PERMISSION_DENIED)
    +-- Set companion callbacks:
        +-- toggleOverlayCallback = { visible -> updateOverlayVisibility }
        +-- hideScanningCallback = { indicatorHelper?.hideScanningIndicator() }

OverlayService.onStartCommand()
    |
    +-- if (!overlayCreated) showOverlay()
            |
            +-- OverlayFabHelper.createOverlayView()
            |   +-- OverlayFabViewFactory.createOverlayView()
            |       +-- Create pulseView (oval background, pulsing animation)
            |       +-- Create iconView (text "F", blue circle)
            |       +-- Container FrameLayout
            |       +-- Start pulse ValueAnimator
            |
            +-- Create WindowManager.LayoutParams (TYPE_APPLICATION_OVERLAY, TOP|START)
            +-- windowManager.addView(container, params)
            |
            +-- OverlayFabTouchHandler.setupDraggable(container, params)
            |   +-- Touch DOWN: start 2s long-press timer
            |   +-- Touch MOVE (>slop): set isDragging=true, cancel activation, update position
            |   +-- Touch UP:
            |       +-- If dragging -> snapToEdge() (snap to nearest screen edge)
            |       +-- If activated (2s long-press) -> triggerCapture()
            |
            +-- Check SharedPreferences: if overlay_visible=false -> hideOverlayView()
```

### 5.3 Scan & Verification Flow

```
OverlayFabTouchHandler detects long-press (2s)
    |
    v
OverlayFabActivator.activateFab()
    +-- Change pulseView color -> green
    +-- Change iconView color -> green
    +-- Vibrate feedback
    |
    v
OverlayService.triggerCapture()
    |
    +-- Start PermissionActivity (transparent, System Alert Window)
    |   +-- PermissionActivity checks MediaProjection permission
    |       +-- If already granted -> directly start ScreenCaptureService
    |       +-- If not -> launch createScreenCaptureIntent()
    |
    +-- OverlayIndicatorHelper.showScanningIndicator()
    |   +-- OverlayIndicatorViewBuilder.createScanningIndicator()
    |   +-- windowManager.addView(indicator)
    |   +-- Set 30s auto-timeout -> hideScanningIndicator()
    |
    v
ScreenCaptureService.onStartCommand()
    |
    +-- Get MediaProjection code + data
    +-- ScreenCaptureHelper.captureScreenshot()
    |   +-- Create VirtualDisplay with ImageReader
    |   +-- Capture ImageReader surface -> bitmap
    |   +-- Save to file: cache/screenshots/factlens_<timestamp>.png
    |
    +-- Start OCRProcessor IntentService with image_path
    +-- Stop self
    |
    v
OCRProcessor.onHandleIntent(image_path)
    |
    +-- Decode bitmap from file
    +-- ML Kit TextRecognition.process(InputImage)
    |   +-- On success: extract text, call VerdictEngine.verify(text)
    |   +-- On failure: show error result
    |
    v
VerdictEngine.verify(claim)
    |
    +-- If USE_MOCK=true -> return mockVerdict (hardcoded)
    |
    +-- SearchClient.search(claim) -- DuckDuckGo HTML scrape
    |
    +-- Try BackendClient.verify(claim, sources)
    |   +-- POST to backend /verify
    |   +-- If fails -> fall through
    |
    +-- If geminiApiKey set -> Try GeminiClient.verify(claim, sources)
    |   +-- If fails -> fall through
    |
    +-- fallbackVerdict(claim, sources) -- keyword heuristic
    |
    v
OCRProcessor.showResult(historyId, claim, explanation, verdict, confidence, sources)
    |
    +-- ResultOverlayHelper.showResult()
    |   +-- OverlayViewFactory.createResultOverlayView()
    |       +-- Card with: verdict chip, explanation, confidence bar, sources, buttons
    |   +-- windowManager.addView(card, params)
    |
    +-- OverlayService.hideScanningCallback?.invoke()
    +-- ScanNotifier.showScanNotification()
    +-- HistorySaver.saveToHistory() -> Room DB insert
```

### 5.4 Backend Verification Pipeline

```
POST /verify { text, language }
    |
    v
VerificationPipeline.verify(text)
    |
    +-- 1. extract_claim(text)
    |   +-- Remove URLs, @mentions, #hashtags
    |   +-- Check is_claim(): filter opinion indicators
    |   +-- Return cleaned text or null
    |
    +-- 2. _retrieve_evidence(claim)
    |   +-- Generate search queries (up to 3 variants)
    |   +-- SearchEngine.search() -- DuckDuckGo HTML scrape
    |   +-- Deduplicate by URL
    |   +-- rank_sources() -- score-based:
    |       +-- +3.0 for trusted domains
    |       +-- +2.0 for .gov, +1.5 for .edu, +0.5 for .org
    |       +-- +0.5 per keyword overlap with claim
    |   +-- Return top 5 ranked sources
    |
    +-- 3. _llm_verdict(claim, sources)
    |   +-- Build system prompt specifying 6 verdict levels
    |   +-- Build user message with claim + source snippets
    |   +-- Call LLM (NaraRouter / OpenAI-compatible)
    |   +-- Parse JSON response: { verdict, confidence, explanation }
    |   +-- Return VerificationResponse
    |
    +-- Cache result (LRU, 1h TTL) for exact text match
```

---

## 6. Key Technical Details

### 6.1 Android

| Detail | Value |
|--------|-------|
| **minSdk** | 27 (Android 8.1 Oreo) |
| **targetSdk** | 34 (Android 14) |
| **Language** | 100% Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Overlay** | Plain Android Views via `WindowManager` |
| **State Management** | Compose `mutableStateOf`, `LaunchedEffect`, `remember` |
| **Navigation** | Manual string-based (`currentScreen` variable) |
| **DI** | No DI framework |
| **Networking (app)** | OkHttp + manual HTTP via `URLConnection` or OkHttp |
| **Networking (backend)** | httpx (async) + FastAPI |
| **OCR** | Google ML Kit `TextRecognition` |
| **Database** | Room (SQLite) with single `ScanHistory` entity |
| **Service IPC** | BroadcastReceiver + companion object callbacks |
| **Screen Capture** | `MediaProjectionManager` -> `VirtualDisplay` -> `ImageReader` -> bitmap |
| **Notifications** | Foreground Service notification + result notification |

### 6.2 Verification Paths

| Path | Endpoint | When Used |
|------|----------|-----------|
| **Mock** | Hardcoded local | `VerdictEngine.USE_MOCK = true` (default) |
| **Backend** | FastAPI on `10.0.2.2:8000` | Tried first if mock is off |
| **Gemini API** | Google AI SDK | Fallback if backend fails + API key set |
| **Fallback** | Heuristic keyword matching | Last resort |

### 6.3 Backend Configuration

| Config | Default | Source |
|--------|---------|--------|
| `NARA_ROUTER_API_KEY` | "" (from env) | `.env` file |
| `NARA_BASE_URL` | `https://router.bynara.id/v1` | Hardcoded |
| `NARA_MODEL` | `mimo-v2.5-hermes` | Hardcoded |
| `SERVER_PORT` | 8000 | Env variable |
| `RATE_LIMIT_PER_MINUTE` | 30 | Env variable |
| `CACHE_TTL_SECONDS` | 3600 (1h) | Env variable |
| **Trusted Domains** | reuters.com, apnews.com, bbc.com, nytimes.com, who.int, cdc.gov, un.org, kemkes.go.id, turnbackhoax.id, etc. | Hardcoded |

### 6.4 Trusted Sources Scoring

| Category | Score | Examples |
|----------|-------|----------|
| International news | +3.0 | reuters.com, apnews.com, bbc.com, nytimes.com |
| Health/UN bodies | +3.0 | who.int, cdc.gov, un.org |
| .gov domains | +2.0 | any .gov |
| .edu domains | +1.5 | any .edu |
| .org domains | +0.5 | any .org |
| Indonesian sources | +3.0 | kemkes.go.id, turnbackhoax.id, komdigi.go.id |
| Keyword overlap | +0.5/word | matching terms between claim and source |

---

## 7. Overlay Architecture

The overlay system uses **plain Android Views** (not Compose) because Compose views cannot be added to the system `WindowManager` reliably. There are three independent overlay layers:

| Layer | Purpose | Implementation |
|-------|---------|----------------|
| **FAB** | Floating "F" button | Draggable, snap-to-edge, long-press to scan. `GradientDrawable`, `TextView`, `FrameLayout` |
| **Scanning indicator** | Show while processing | Spinner + "FactLens Scanning..." text. Auto-hide after 30s timeout |
| **Result card** | Show verdict | Card with verdict, explanation, confidence bar, sources, actions |

---

## 8. Current State & Known Gaps

### 8.1 What's Complete

- [x] Full overlay FAB lifecycle (show/hide/drag/snap/long-press)
- [x] End-to-end capture -> OCR -> verify -> result overlay flow
- [x] Room database + history persistence + CRUD
- [x] Settings screen with overlay visibility toggle
- [x] Dual verification path (backend + Gemini + mock + fallback)
- [x] FastAPI backend with full pipeline
- [x] Docker deployment
- [x] Indonesian OCR + trusted sources support

### 8.2 What's Partial/Mock

- [ ] **HomeScreen** -- recent scans/stats are hardcoded mock data
- [ ] **SavedScreen** -- favorite scans are mock/hardcoded
- [ ] **History filter/search** -- not implemented
- [ ] **Bookmark/share** -- placeholder, no actual implementation
- [ ] **Dark mode** -- planned but only light theme defined
- [ ] **Backend endpoints** -- /verify has mock fallback when no API key set
- [ ] **Error handling for network failures** -- basic try-catch, no retry UI
- [ ] **Settings Gemini API key input** -- UI exists but no state persistence

### 8.3 Known Issues & Crash Causes

#### Critical (will crash or break core flow)

| # | Issue | Location |
|---|-------|----------|
| 1 | **Null WindowManager crash** | `OverlayService.kt:44` -- if `getSystemService(WINDOW_SERVICE)` returns null |
| 2 | **DDG HTML regex mismatch** | `SearchClient.kt:44-46` -- targets CSS classes that may change |
| 3 | **Bitmap miscalculation** | `ScreenCaptureService.kt:126-128` -- fractional pixelStride |
| 4 | **Hardcoded mock data** | `HomeScreen.kt`, `HistoryScreen.kt` -- fake data instead of Room queries |
| 5 | **No capture permission handling** | `ScreenCaptureService.kt` -- proceeds even if user denies |

#### Moderate (bugs or poor UX)

| # | Issue | Location |
|---|-------|----------|
| 6 | **No ViewModel / state loss on config change** | All screens |
| 7 | **Gemini API key is mutable static** | `VerdictEngine.kt` -- not persistent |
| 8 | **Conflicting Window flags** | `ResultOverlayHelper.kt` -- FLAG_DIM_BEHIND + FLAG_NOT_FOCUSABLE |
| 9 | **ScreenCaptureManager is unused** | `capture/ScreenCaptureManager.kt` -- dead code |
| 10 | **runBlocking in IntentService** | `OCRProcessor.kt:49` -- ANR risk |
| 11 | **No error handling for empty search results** | `VerdictEngine.kt` |
| 12 | **Cleartext traffic limited** | `network_security_config.xml` |

#### Minor

| # | Issue | Location |
|---|-------|----------|
| 13 | **No Room entity ProGuard keep rules** | `proguard-rules.pro` |
| 14 | **Dark mode not implemented** | `Theme.kt` |
| 15 | **ScanningOverlayScreen.kt is dead code** | `ui/screens/` |
| 16 | **Backend has no rate limiting or caching** | `backend/` |
| 17 | **Backend auto-detects available keys silently** | `llm_client.py` |

---

## 9. Technology Stack

### 9.1 Android Dependencies

| Category | Library | Version |
|----------|---------|---------|
| Core | Kotlin, Compose BOM, Core KTX | 2.0.21, 2024.02, 1.12.0 |
| UI | Material 3, Material Icons Extended, Foundation | BOM managed |
| OCR | Google ML Kit Text Recognition | 16.0.1 |
| Network | OkHttp, Retrofit + Gson | 4.12.0, 2.9.0 |
| Database | Room (runtime, ktx, compiler via kapt) | 2.6.1 |
| Async | Kotlinx Coroutines Android | 1.8.0 |
| Storage | DataStore Preferences | 1.0.0 |
| Build | AGP 8.13.2, Kotlin 2.0.21, Compose plugin | |

### 9.2 Backend Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| fastapi | >=0.109.0 | Async web framework |
| uvicorn | >=0.27.0 | ASGI server |
| openai | >=1.12.0 | LLM API client |
| httpx | >=0.26.0 | Async HTTP client |
| beautifulsoup4 | >=4.12.0 | HTML parsing |
| lxml | >=5.1.0 | XML parser |
| slowapi | >=0.1.9 | Rate limiting |
| python-dotenv | >=1.0.0 | .env file loader |

---

## 10. Coverage & Maturity

| Area | Status | Notes |
|------|--------|-------|
| **Core workflow** | Complete | End-to-end functional |
| **Floating overlay** | Complete | Drag, snap, long-press, toggle |
| **Result overlay** | Complete | Full screen with View Details |
| **Room history** | Complete | CRUD, favorites, Flow queries |
| **UI screens** | Partial | Some screens use mock data |
| **Bookmark / Share** | Partial | Share works, bookmark toggle needs wiring |
| **Dark mode** | Planned | Color palette supports it |
| **Settings -- API Key config** | Stub | Button exists, no actual input |
| **Settings -- Clear History** | Stub | Button exists, no Room delete wired |
| **Share result** | Complete | Intent.ACTION_SEND with text |
| **Backend tests** | Basic | 7 tests for API + claim detector |
| **Production readiness** | No | Needs auth, monitoring, error hardening |
| **Docker deployment** | Ready | Dockerfile + docker-compose |

---

## 11. Deployment

### Backend

```bash
cd backend
pip install -r requirements.txt
cp .env.example .env  # Add NARA_ROUTER_API key (optional)
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Docker:
docker-compose up --build
```

### Android

- Open `android/` in Android Studio
- Sync Gradle
- Run on device/emulator (minSdk 27, targetSdk 34)
- App connects to `10.0.2.2:8000` (emulator -> host) by default

### Environment Variables

```
NARA_ROUTER_API=sk-nry-your_key    # Optional for LLM verdicts
SERVER_HOST=0.0.0.0                # Default
SERVER_PORT=8000                   # Default
RATE_LIMIT_PER_MINUTE=30           # Default
```

---

## 12. Recommendations

**Priority order to stabilize the app:**

1. Fix null WindowManager crash in `OverlayService`
2. Fix bitmap dimension calculation in `ScreenCaptureService`
3. Wire up Room DB to Home and History screens (replace mock data)
4. Handle screen capture permission denial gracefully
5. Add ViewModel-based state management for config-change survival
6. Add ProGuard rules for Room entities
7. Add error handling for network failures in UI
8. Persist Gemini API key in Settings (DataStore)

---

*Generated from comprehensive source code inspection of the FactLens (antiTimpa) repository.*
