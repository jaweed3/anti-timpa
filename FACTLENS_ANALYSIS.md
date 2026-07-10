# FactLens — Deep Technical Analysis

> **Project:** AI-powered Android overlay fact-checker (Gemini AI Hackathon 2025)  
> **Last analyzed:** July 9, 2026

---

## 1. Project Overview

**FactLens** is an Android application (with a Python backend) that lets users verify suspicious information **without leaving the app they are using**. The core value proposition is zero friction: one tap on a floating overlay button captures the screen, extracts text via on-device OCR, searches for evidence from trusted sources, and delivers an AI-backed fact-check verdict within seconds.

**What it verifies:**
- News headlines and articles
- Social media posts (Instagram, TikTok, X/Twitter, WhatsApp, etc.)
- Health and medical claims
- Political statements
- Scientific claims
- Numeric/statistical data

**Verdict types:** `Supported`, `Contradicted`, `Misleading`, `Mixed`, `Insufficient Evidence`, `Unknown` — each with a confidence score (capped at 0.95) and source-backed explanation.

---

## 2. Repository Structure

```
D:\002. NGULIah\003. CODESOURCES\00A. ANDROID\
│
├── README.md                          # Project README
├── DETAILED.md                        # 436-line detailed project doc
├── .gitignore
├── docker-compose.yml                 # Docker Compose for FastAPI backend
├── FactLens-v1.0-hackathon.apk        # Hackathon build
├── FactLens-v1.1-poc.apk              # Updated PoC build
├── stitch_factlens_mobile_ai_verification.zip  # Design assets
│
├── android/                           # Android app (Kotlin + Compose)
│   ├── build.gradle.kts               # Project-level Gradle
│   ├── settings.gradle.kts            # rootProject.name = "FactLens"
│   ├── gradle.properties
│   ├── local.properties
│   ├── gradlew / gradlew.bat          # Gradle wrapper
│   ├── proguard-rules.pro             # Gson model keep rules
│   ├── gradle/wrapper/
│   │   └── gradle-wrapper.properties  # Gradle 8.13
│   ├── app/
│   │   ├── build.gradle.kts           # App-level deps
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── res/
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   └── colors.xml
│   │       │   ├── drawable/
│   │       │   │   ├── ic_launcher_foreground.xml
│   │       │   │   └── ic_launcher_background.xml
│   │       │   ├── mipmap-hdpi/
│   │       │   │   └── ic_launcher.xml
│   │       │   └── xml/
│   │       │       └── network_security_config.xml
│   │       └── java/com/factlens/
│   │           ├── FactLensApp.kt           # Application class (crash logger)
│   │           ├── MainActivity.kt          # Entry point, navigation
│   │           ├── model/
│   │           │   └── Models.kt            # Data classes, Room entity
│   │           ├── ui/
│   │           │   ├── theme/
│   │           │   │   └── Theme.kt         # Material 3 theme
│   │           │   ├── components/
│   │           │   │   └── Common.kt        # Reusable composables
│   │           │   └── screens/
│   │           │       ├── HomeScreen.kt
│   │           │       ├── ScanningScreen.kt
│   │           │       ├── ScanningOverlayScreen.kt  # Unused
│   │           │       ├── ScanResultScreen.kt
│   │           │       ├── FloatingResultOverlay.kt
│   │           │       ├── HistoryScreen.kt
│   │           │       ├── SavedScreen.kt
│   │           │       └── SettingsScreen.kt
│   │           ├── overlay/
│   │           │   ├── OverlayService.kt          # Floating F button
│   │           │   └── ResultOverlayHelper.kt      # WindowManager overlay
│   │           ├── capture/
│   │           │   ├── ScreenCaptureManager.kt    # Permission mgmt (unused)
│   │           │   └── ScreenCaptureService.kt    # MediaProjection capture
│   │           ├── ocr/
│   │           │   └── OCRProcessor.kt            # ML Kit OCR + verify
│   │           ├── network/
│   │           │   ├── SearchClient.kt            # DuckDuckGo scraper
│   │           │   └── VerdictEngine.kt           # Gemini API + fallback
│   │           └── history/
│   │               └── HistoryDatabase.kt         # Room DB + DAO
│
├── backend/                           # FastAPI backend (Python)
│   ├── main.py                        # FastAPI server, /verify endpoints
│   ├── models.py                      # Pydantic models
│   ├── verification_pipeline.py       # Orchestrator
│   ├── claim_detector.py             # Regex claim extraction
│   ├── search_engine.py              # Async DuckDuckGo search
│   ├── ranker.py                     # Source scoring
│   ├── llm_client.py                 # OpenAI + Gemini client
│   ├── requirements.txt
│   ├── pyproject.toml
│   ├── Dockerfile
│   ├── .env.example
│   └── tests/
│       ├── test_api.py
│       └── test_claim_detector.py
│
└── design_refs/
    └── stitch_factlens_mobile_ai_verification/
        └── factlens/
            ├── DESIGN.md              # Material 3 design spec
            └── screen_*/              # Screenshots + HTML code
```

---

## 3. Architecture & Data Flow

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Android App (Kotlin)                    │
│                                                          │
│  ┌────────────┐    ┌──────────────┐    ┌──────────────┐ │
│  │ Overlay    │───→│ Screen       │───→│ ML Kit OCR   │ │
│  │ Service    │    │ Capture Svc  │    │ Processor    │ │
│  └────────────┘    └──────────────┘    └──────┬───────┘ │
│                                                │         │
│  ┌────────────┐    ┌──────────────┐           │         │
│  │ Result     │←───│ Verdict      │←──────────┘         │
│  │ Overlay    │    │ Engine       │                      │
│  └────────────┘    └──────┬───────┘                      │
│                           │                              │
│                     ┌─────┴──────┐                      │
│                     │ DuckDuckGo │   ┌──────────────┐   │
│                     │ Scraper    │   │ Gemini API   │   │
│                     └────────────┘   │ (Direct HTTP)│   │
│                                       └──────────────┘   │
│  ┌──────────────────┐                                     │
│  │ Room DB (History)│                                     │
│  └──────────────────┘                                     │
└─────────────────────────────────────────────────────────┘
         │ (optional POST /verify)
         ▼
┌─────────────────────────────────────────────────────────┐
│                  Backend (FastAPI/Python)                │
│                                                          │
│  ┌────────────────┐   ┌──────────────┐   ┌───────────┐ │
│  │ Claim Detector │──→│ Search Engine│──→│  Ranker   │ │
│  │  (regex)       │   │ (DDG async)  │   │ (scoring) │ │
│  └────────────────┘   └──────────────┘   └─────┬─────┘ │
│                                                 │       │
│                                          ┌──────▼─────┐│
│                                          │ LLM Client ││
│                                          │(GPT/Gemini)││
│                                          └────────────┘│
└─────────────────────────────────────────────────────────┘
```

### End-to-End Data Flow

1. User taps floating **"F" button** in any app
2. `OverlayService` → launches `MainActivity` with `trigger_capture` intent
3. `MainActivity` → launches `MediaProjectionManager` for screen capture permission
4. `ScreenCaptureService` → captures screen as PNG via `VirtualDisplay` + `ImageReader`
5. `OCRProcessor` (IntentService) → runs **Google ML Kit** text recognition on image
6. Extracted text → `VerdictEngine.verify()`
   - **Path A (Direct):** Calls Google Gemini 2.0 Flash API directly via OkHttp
   - **Path B (Fallback):** Keyword-based rumor/confirm word matching
   - **Path C (Backend):** Retrofit-based FastAPI call (interface not implemented)
7. `SearchClient` → scrapes DuckDuckGo HTML for evidence (regex-parsed)
8. Result shown via `ResultOverlayHelper` → floating bottom sheet overlay (35% height)
9. Scan saved to **Room database** (`HistoryDatabase`)

---

## 4. Technology Stack

### Android App

| Layer | Technology | Version |
|---|---|---|
| **Language** | Kotlin | 1.9.22 |
| **UI** | Jetpack Compose + Material 3 | BOM 2024.02.00 |
| **Compose Compiler** | Kotlin Compiler Extension | 1.5.10 |
| **Min / Target SDK** | API 27 → API 34 | — |
| **Build** | Gradle + AGP | 8.13 / 8.13.2 |
| **OCR** | ML Kit Text Recognition | 16.0.0 |
| **HTTP** | OkHttp 4 + Retrofit 2 | 4.12.0 / 2.9.0 |
| **Database** | Room (with KAPT) | 2.6.1 |
| **Async** | Kotlinx Coroutines | 1.8.0 |
| **Preferences** | DataStore Preferences | 1.0.0 |
| **JSON** | Gson | (transitive) |
| **Splash** | AndroidX SplashScreen | 1.0.1 |
| **Camera** | CameraX | 1.3.1 |

### Backend (Python)

| Layer | Technology | Version |
|---|---|---|
| **Framework** | FastAPI | ≥0.109.0 |
| **Server** | Uvicorn | ≥0.27.0 |
| **Validation** | Pydantic v2 | ≥2.5.0 |
| **HTTP Client** | httpx (async) | ≥0.26.0 |
| **HTML Parsing** | BeautifulSoup4 + lxml | ≥4.12.0 / ≥5.1.0 |
| **LLM #1** | OpenAI SDK (GPT-4o-mini) | ≥1.12.0 |
| **LLM #2** | Google Generative AI (Gemini 1.5 Flash) | ≥0.3.0 |
| **Env** | python-dotenv | ≥1.0.0 |
| **Container** | Docker (Python 3.11-slim) | — |
| **Testing** | pytest + httpx | — |

### Infrastructure
- **Docker Compose** — single `api` service, port 8000
- **DuckDuckGo HTML Search** — no API key required (scrapes `html.duckduckgo.com`)

---

## 5. Key Files & Responsibilities

### Android — Core Infrastructure

| File | Responsibility |
|---|---|
| `FactLensApp.kt` | Application class; registers global `Thread.setDefaultUncaughtExceptionHandler` that writes crash logs to `filesDir/crashes/` |
| `MainActivity.kt` | Entry point; handles overlay permission, notification permission (Android 13+), media projection launch; renders `MainApp` composable with string-based screen routing (`setup`, `home`, `scanning`, `history`, `saved`, `settings`, `scan_result`) |
| `Models.kt` | Data classes: `VerificationRequest`, `VerificationResponse`, `Source` (with Gson `@SerializedName`), `ScanHistory` (Room `@Entity` with auto-generated ID, timestamp, claim, verdict, confidence, explanation, sourcesJson, screenshotPath, isFavorite) |

### Android — UI Layer

| File | Responsibility |
|---|---|
| `Theme.kt` | Full Material 3 design system: `FactLensColors` (dark backgrounds with accent teal/purple), `FactLensTypography`, `FactLensShapes`, `Spacing` system, `FactLensTheme` composable with `lightColorScheme` |
| `Common.kt` | Reusable composables: `VerdictBadge`, `ConfidenceBadge`, `PrimaryButton`, `SecondaryButton`, `FactCard`, `EvidenceCard`, `HistoryCard`, `SectionHeader` |
| `HomeScreen.kt` | Dashboard with Quick Scan button + recent/saved cards (uses **hardcoded mock data**) |
| `ScanningScreen.kt` | Scanning animation (3s delay, pulsing icon) |
| `ScanningOverlayScreen.kt` | Full-screen overlay scanning UI — **not referenced anywhere, appears unused** |
| `ScanResultScreen.kt` | Full verification result with evidence list, save/share buttons |
| `FloatingResultOverlay.kt` | Bottom sheet overlay (35% screen height) for instant results via WindowManager |
| `HistoryScreen.kt` | Searchable, day-grouped history (uses **hardcoded mock data**) |
| `SavedScreen.kt` | Bookmarked results — **permanent empty state, non-functional** |
| `SettingsScreen.kt` | Permissions UI, API key config stub (TODO), clear history stub |

### Android — Overlay & Capture

| File | Responsibility |
|---|---|
| `OverlayService.kt` | Foreground service; creates draggable floating "F" button with pulsing animation; launches capture intent on tap; potential NPE if `windowManager` is null |
| `ResultOverlayHelper.kt` | Singleton; shows/dismisses full-screen dimmed overlay with `FloatingResultOverlay` composable via `WindowManager.addView()` |
| `ScreenCaptureManager.kt` | Permission + capture request management — **exists but not wired into main flow** |
| `ScreenCaptureService.kt` | Foreground service; uses `MediaProjection` + `VirtualDisplay` + `ImageReader` to capture screen as PNG; launches `OCRProcessor`; contains a bitmap dimension calculation that may produce corrupted images |
| `OCRProcessor.kt` | IntentService; runs ML Kit text recognition → calls `VerdictEngine.verify()` → shows result via `ResultOverlayHelper` → saves to Room via `runBlocking` |

### Android — Business Logic

| File | Responsibility |
|---|---|
| `SearchClient.kt` | OkHttp-based DuckDuckGo HTML scraper; regex-parses `result__a`, `result__snippet`, `uddg` — **class names may not match actual DDG HTML** |
| `VerdictEngine.kt` | On-device verification: calls Gemini 2.0 Flash API directly via OkHttp with structured JSON prompt, or uses keyword-based fallback; API key is a mutable static |
| `HistoryDatabase.kt` | Room database with `HistoryDao`: CRUD operations, `Flow`-based queries, favorites toggle |

### Backend

| File | Responsibility |
|---|---|
| `main.py` | FastAPI app with CORS, lifespan, `/health` GET, `/verify` POST/GET (5-char minimum validation) |
| `models.py` | Pydantic models: `VerificationRequest`, `Source`, `VerificationResponse` |
| `verification_pipeline.py` | Orchestrator: `detect_claim()` → `retrieve_evidence()` (multi-query) → `llm_verdict()` with system prompt + JSON parsing, fallback on LLM failure |
| `claim_detector.py` | Regex-based: strips URLs/mentions/hashtags, filters opinions ("I think", "I feel") |
| `search_engine.py` | Async httpx DuckDuckGo client; BeautifulSoup parsing; trusted domain prioritization (Reuters, AP, BBC, WHO, CDC + Indonesian sources) |
| `ranker.py` | Score-based ranking: trusted domains +3.0, `.gov` +2.0, `.edu` +1.5, `.org` +0.5, keyword overlap +0.5 |
| `llm_client.py` | Unified client: OpenAI GPT-4o-mini or Gemini 1.5 Flash; auto-detects available API keys; `temperature=0.3` |

---

## 6. Dependencies

### Android (`app/build.gradle.kts`)

```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")

// Compose (BOM 2024.02.00)
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.compose.foundation:foundation")

// ML Kit
implementation("com.google.mlkit:text-recognition:16.0.0")

// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Splash
implementation("androidx.core:core-splashscreen:1.0.1")
```

### Backend (`requirements.txt`)

```
fastapi>=0.109.0
uvicorn[standard]>=0.27.0
pydantic>=2.5.0
httpx>=0.26.0
python-dotenv>=1.0.0
openai>=1.12.0
google-generativeai>=0.3.0
lxml>=5.1.0
beautifulsoup4>=4.12.0
```

---

## 7. Potential Crash Causes & Issues

### 🔴 Critical (Will Crash or Break Core Flow)

| # | Issue | Location | Impact |
|---|---|---|---|
| 1 | **Null WindowManager crash** | `OverlayService.kt:44` | If `getSystemService(WINDOW_SERVICE)` returns null, `onStartCommand` still calls `showOverlay()` which does `windowManager.addView()` → NPE crash |
| 2 | **DDG HTML regex mismatch** | `SearchClient.kt:44-46` | Targets CSS classes `result__a` and `result__snippet`; if DuckDuckGo changes their HTML, any search returns empty → "Insufficient Evidence" for everything |
| 3 | **Missing Retrofit API interface** | `app/build.gradle.kts` | Retrofit + Gson converter included, but no API interface class exists. The FastAPI backend path is not connected from Android. App relies solely on direct Gemini calls |
| 4 | **Bitmap miscalculation** | `ScreenCaptureService.kt:126-128` | `Bitmap.createBitmap(width + rowPadding / pixelStride, ...)` — `rowPadding/pixelStride` can be fractional; results in corrupted image or crash |
| 5 | **Hardcoded mock data on key screens** | `HomeScreen.kt:161-200`, `HistoryScreen.kt:104-147` | Home and History screens show fake data instead of querying Room DB. `SavedScreen.kt` is permanently empty |
| 6 | **No screen capture permission handling** | `ScreenCaptureService.kt` | If user denies permission (`resultCode != RESULT_OK`), service is still started and proceeds to call `startProjection()` with invalid data |

### 🟡 Moderate (Will Cause Bugs or Poor UX)

| # | Issue | Location | Impact |
|---|---|---|---|
| 7 | **No ViewModel / state loss on config change** | All screens | State managed with `remember { mutableStateOf() }`; scan results lost on Activity recreation (rotation, theme change) |
| 8 | **Gemini API key is mutable static** | `VerdictEngine.kt` | `VerdictEngine.geminiApiKey` is set via companion object, not persistent; Settings UI has a TODO stub for configuration |
| 9 | **`FloatingResultOverlay` has conflicting Window flags** | `ResultOverlayHelper.kt` | `FLAG_DIM_BEHIND` + `FLAG_NOT_FOCUSABLE` may prevent the overlay's buttons from receiving touches |
| 10 | **`ScreenCaptureManager` is unused** | `capture/ScreenCaptureManager.kt` | Class exists but `MainActivity` uses `mediaProjectionLauncher` directly; dead code |
| 11 | **`runBlocking` in IntentService** | `OCRProcessor.kt:49` | Database operations and network calls use `runBlocking` on worker thread — functional but blocks the thread; ANR risk if network hangs |
| 12 | **No error handling for empty search results** | `VerdictEngine.kt` | If `SearchClient.search()` returns empty, fallback returns "Insufficient Evidence" even if search failed due to network error |
| 13 | **Cleartext traffic limited** | `network_security_config.xml` | Only allows cleartext to `10.0.2.2` and `localhost`; custom backend IPs fail on Android 9+ |

### 🔵 Minor (Style, Performance, Maintainability)

| # | Issue | Location | Impact |
|---|---|---|---|
| 14 | **No Room entity ProGuard keep rules** | `proguard-rules.pro` | Only Gson models are kept; Room entities may get stripped/obfuscated |
| 15 | **Dark mode not implemented** | `Theme.kt` | Only `lightColorScheme` defined; `colors.xml` has dark colors but they're unused |
| 16 | **`ScanningOverlayScreen.kt` is dead code** | `ui/screens/` | File exists with a full composable but is never referenced |
| 17 | **Crash log directory has no cleanup** | `FactLensApp.kt` | Writes to `filesDir/crashes/` indefinitely with no size limit or rotation |
| 18 | **Backend has no rate limiting or caching** | `backend/` | Each request re-scrapes DDG; LLM API calls could incur unexpected costs |
| 19 | **Backend uses SDK, Android uses REST** | `llm_client.py` vs `VerdictEngine.kt` | Backend uses `google-generativeai` SDK; Android calls Gemini via raw HTTP; inconsistency |
| 20 | **Auto-detects available keys silently** | `llm_client.py` | If neither OpenAI nor Gemini keys are set, the LLM client silently returns None → falls back to basic response |

---

## 8. Architecture Patterns & Design Decisions

### What's Done Well

- **Clean separation of concerns** across overlay, capture, OCR, network, history, and UI packages
- **Material 3 design system** is thorough — colors, typography, shapes, spacing, all defined in a single `Theme.kt`
- **Dual verification path** (direct Gemini + backend API) provides flexibility
- **Global crash logger** in `FactLensApp` for debugging
- **Room database** with proper DAO abstraction and Flow-based reactive queries
- **Backend pipeline** has clear stages: claim detection → search → ranking → LLM verdict
- **Async operations** in backend (`httpx.AsyncClient`, `asyncio.gather`)
- **No API key required** for DuckDuckGo search (both Android and backend)

### What's Missing / Incomplete (Hackathon Reality)

- **No ViewModel or Repository pattern** — state management is ad-hoc with `mutableStateOf`
- **Manual string-based navigation** — fragile, no type safety, no deep linking
- **No dependency injection** (Hilt/Koin) — services are singletons or manual instances
- **No testing** on Android side (no `androidTest` or unit test files)
- **No error states in UI** — failure scenarios are largely unhandled
- **Retrofit backend path is disconnected** — library included but no API interface
- **Key screens show hardcoded mock data** — not integrated with Room DB
- **`ScreenCaptureManager` is unused** — capture logic is duplicated in `MainActivity`

---

## 9. Backend-Only Analysis

The backend is more polished than the Android app, with proper async patterns, structured logging, and unit tests:

- **FastAPI** with proper CORS, request validation (Pydantic), and health endpoint
- **Asynchronous search** using `asyncio.gather` for concurrent multi-query search
- **Trusted domain scoring** with configurable weights
- **Graceful degradation** — if LLM is unavailable, falls back to keyword-based verdict
- **Docker support** with Python 3.11-slim image

**Backend issues:**
- No authentication or rate limiting
- DuckDuckGo scraping may break without notice
- LLM API keys loaded from environment (securely handled via `python-dotenv`)

---

## 10. Summary & Recommendations

**FactLens** is a well-architected hackathon MVP that demonstrates a genuinely innovative overlay-based fact-checking experience. The Android app and Python backend use modern, appropriate technologies, and the design system is production-quality.

**Maturity:** Strong hackathon prototype with clear remaining work.

**To stabilize the app, prioritize fixing (in order):**
1. Null-check `windowManager` in `OverlayService`
2. Create the Retrofit API interface or remove Retrofit deps
3. Fix bitmap dimension calculation in `ScreenCaptureService`
4. Wire up Room DB to Home and History screens (replace mock data)
5. Handle screen capture permission denial gracefully
6. Add ViewModel-based state management for config-change survival
7. Implement crash log cleanup / rotation
8. Add ProGuard rules for Room entities

---

*Analysis generated from source code inspection of the FactLens repository.*
