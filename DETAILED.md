# FactLens — Detailed Project Overview

> **Tagline:** Verify information directly from your screen.
> **Built for:** Gemini AI Hackathon 2025
> **Status:** Hackathon MVP / Proof of Concept (v1.1)

---

## 1. What Is FactLens?

FactLens is an **AI-powered Android application** that enables users to verify suspicious information in real time **without leaving the app they are currently using**. It acts as a floating overlay that can be summoned from any screen — social media (Instagram, TikTok, X/Twitter), messaging (WhatsApp, Telegram), news apps, or browsers — to capture what's on screen, extract text, and deliver an AI-backed fact-check verdict within seconds.

The core value proposition is **zero friction**: no switching apps, no copy-pasting, no typing URLs. One tap → verdict.

---

## 2. Scope — What Kind of Information Does It Verify?

FactLens is **general-purpose** and **not specialized** to any single domain. It can handle any factual claim that appears as text on a screen, including:

- **News headlines & articles** — verifying breaking news, statistics, quotes
- **Social media posts** — claims in tweets, captions, comments
- **WhatsApp / messaging forwards** — chain messages, viral warnings
- **Health & medical claims** — treatment efficacy, nutrition advice
- **Political statements** — policy claims, debate assertions
- **Scientific claims** — research findings, environmental data
- **Numeric / statistical claims** — data points, percentages, economic figures

It avoids processing opinions, jokes, advertisements, and purely subjective content through a built-in claim detector.

### Supported Languages

- **English** (primary)
- **Indonesian** (OCR + search sources include Indonesian fact-checking sites)

---

## 3. Features

### 3.1 Core Features

| Feature | Description |
|---|---|
| **Floating Overlay Button** | Draggable "F" button persists across all apps with a subtle pulsing animation |
| **One-Tap Screen Capture** | Uses Android `MediaProjection API` to capture exactly what's on screen |
| **On-Device OCR** | Google ML Kit text recognition runs locally — no internet needed for text extraction |
| **Claim Detection** | Extracts factual claims from text; filters out opinions, jokes, ads, noise |
| **Multi-Engine Search** | Searches DuckDuckGo (no API key required) for evidence from trusted sources |
| **Source Ranking** | Prioritizes authoritative sources: `.gov`, `.edu`, major news agencies, scientific publishers |
| **AI-Powered Verdict** | GPT-4o-mini or Gemini 1.5 Flash analyzes claim vs. evidence and produces structured verdict |
| **6 Verdict Types** | Supported, Contradicted, Misleading, Mixed, Insufficient Evidence, Unknown |
| **Confidence Scoring** | Float 0.0–0.95 with explanation (never claims 100% certainty) |
| **Local History** | Room database stores all past scans with full evidence, searchable and grouped by day |
| **Save / Bookmark** | Mark important results for later reference |
| **No Login Required** | Zero friction — open app, grant permissions, and use immediately |

### 3.2 Verdict System

| Verdict | Meaning | Color |
|---|---|---|
| **Supported** | Evidence supports the claim | Emerald (green) |
| **Contradicted** | Evidence contradicts the claim | Red |
| **Misleading** | Partially true but presented deceptively | Amber |
| **Mixed** | Conflicting evidence | Gray |
| **Insufficient Evidence** | Not enough reliable sources found | Gray |
| **Unknown** | Could not process | Gray |

Every verdict is **always accompanied by supporting evidence sources** and a human-readable explanation. Confidence is capped at **0.95** to avoid false certainty.

### 3.3 Graceful Degradation

FactLens works **without any API keys**:

- **No LLM key (OpenAI / Gemini):** Falls back to keyword-based heuristic analysis (rumor/confirm word matching) with source listing
- **No search API key:** DuckDuckGo HTML scraping works out of the box
- **No internet (for OCR):** Google ML Kit runs fully on-device

This makes the app functional immediately after installation.

---

## 4. Architecture

### 4.1 High-Level Flow

```
┌─────────────────────────────────────────────────────┐
│                   Android App                        │
│                                                       │
│  ┌──────────┐   ┌──────────────┐   ┌──────────────┐ │
│  │  Overlay  │──→│  Capture     │──→│  ML Kit OCR  │ │
│  │  Service  │   │  Service     │   │  (on-device)  │ │
│  └──────────┘   └──────────────┘   └──────┬───────┘ │
│                                            │         │
│                    ┌───────────────────────▼───────┐  │
│                    │  Two Verification Paths:      │  │
│                    │                                │  │
│                    │  Path A: Retrofit → FastAPI    │  │
│                    │  Path B: OkHttp → Gemini API  │  │
│                    │  (direct, no backend needed)     │
│                    └───────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │
           ┌─────────────┴─────────────┐
           │                           │
     ┌─────▼─────┐            ┌────────▼────────┐
     │  FastAPI   │            │  Gemini API     │
     │  Backend   │            │  (direct call)  │
     └─────┬─────┘            └─────────────────┘
           │
     ┌─────▼─────────────────────────────┐
     │  Verification Pipeline            │
     │  ┌────────────┐                   │
     │  │ Claim      │                   │
     │  │ Detector   │                   │
     │  └─────┬──────┘                   │
     │        ▼                          │
     │  ┌────────────┐                   │
     │  │ Search     │── DuckDuckGo      │
     │  │ Engine     │── HTML scrape     │
     │  └─────┬──────┘                   │
     │        ▼                          │
     │  ┌────────────┐                   │
     │  │ Ranker     │── domain score    │
     │  └─────┬──────┘                   │
     │        ▼                          │
     │  ┌────────────┐                   │
     │  │ LLM        │── GPT-4o-mini    │
     │  │ Client     │── Gemini 1.5     │
     │  └────────────┘                   │
     └───────────────────────────────────┘
```

### 4.2 Android App (Kotlin + Jetpack Compose)

| Layer | Technology | Details |
|---|---|---|
| **UI** | Jetpack Compose, Material 3 | Full declarative UI with custom theme |
| **Overlay** | WindowManager + ComposeView | Draggable floating button, independent of activity lifecycle |
| **Capture** | MediaProjection API + ImageReader | Foreground service captures full-screen screenshot |
| **OCR** | Google ML Kit Text Recognition | On-device, supports English + Indonesian |
| **Network** | OkHttp + Retrofit + Gson | Dual path: FastAPI backend OR direct Gemini API call |
| **Database** | Room (SQLite) | Stores scan history with full evidence JSON |
| **State** | Compose state + singleton helpers | No ViewModel used (hackathon simplicity) |

### 4.3 Backend (Python FastAPI)

| Component | Technology | Details |
|---|---|---|
| **API Server** | FastAPI | Async, CORS enabled, health endpoint |
| **Claim Detector** | Regex + heuristics | Strips URLs, mentions, hashtags; detects opinion indicators |
| **Search Engine** | httpx + BeautifulSoup | Scrapes DuckDuckGo HTML results, no API key |
| **Ranker** | Domain + keyword scoring | Trusted domains get +3, `.gov` +2, `.edu` +1.5, keyword overlap bonus |
| **LLM Client** | OpenAI + Gemini SDKs | Unified interface, falls back to keyword heuristic |
| **Containerization** | Docker + docker-compose | Single API service with env var config |

### 4.4 Trusted Source Prioritization

Sources are ranked using a scoring system:

| Criterion | Score |
|---|---|
| Reuters, AP, BBC, NYT, Guardian, WashPost, Nature, Science, WHO, CDC, UN, PubMed | +3.0 |
| `.gov` domain | +2.0 |
| `.edu` domain | +1.5 |
| `.org` domain | +0.5 |
| Title overlap with claim keywords | +0.5 per matching word |

Indonesian-specific trusted sources: `kemkes.go.id`, `kmov.id`, `turnbackhoax.id`

---

## 5. Design System

Based on the detailed `DESIGN.md` spec file:

### Visual Identity

| Element | Value |
|---|---|
| Primary Color | Blue (`#00497D`) |
| Success / Supported | Emerald (`#006D44`) |
| Warning / Misleading | Amber (`#924C00`) |
| Error / Contradicted | Red (`#BA1A1A`) |
| Background | Almost White (`#FDFBFF`) |
| Cards | Pure White (`#FFFFFF`) |
| Typography | **Inter** (single font family) |
| Dark Mode | Supported |
| Border Radius | 8dp / 16dp / 24dp (small / medium / large) |
| Overlay Max Height | **35%** of screen |

### Design Principles

- **Minimal, Modern, Fast, Professional, Trustworthy, Clean, AI-first**
- Not playful, not gamified
- Never require more than **three taps** for verification
- Never force account creation
- Never interrupt the user
- Every AI decision must provide **supporting evidence**
- Overlay experience is the **highest priority**

### Screens (Priority Order)

1. Overlay Verification (flagship feature)
2. Scan Result
3. Home
4. History
5. Saved
6. Settings

---

## 6. Tech Stack

| Layer | Technology |
|---|---|
| **Mobile Framework** | Kotlin, Jetpack Compose, Coroutines |
| **Android SDK Min** | API 27 (Android 8.1 Oreo) |
| **OCR** | Google ML Kit (on-device) |
| **Backend** | FastAPI (Python 3.x) |
| **LLM Providers** | OpenAI GPT-4o-mini / Gemini 1.5 Flash |
| **On-Device AI** | Gemini 2.0 Flash (via OkHttp direct call) |
| **Search** | DuckDuckGo HTML scraping (no API key) |
| **HTML Parsing** | BeautifulSoup (backend) / Regex (Android) |
| **Local Storage** | Room (Android), in-memory (backend) |
| **HTTP Clients** | OkHttp, Retrofit (Android) / httpx (backend) |
| **Deployment** | Docker, docker-compose |
| **Serialization** | Gson (Android) / Pydantic (backend) |

---

## 7. Project Structure

```
├── android/                              # Android application
│   ├── app/
│   │   └── src/main/java/com/factlens/
│   │       ├── MainActivity.kt           # Entry point, navigation, setup flow
│   │       ├── model/
│   │       │   └── Models.kt              # Data classes + Room entity
│   │       ├── ui/
│   │       │   ├── theme/
│   │       │   │   └── Theme.kt           # Colors, typography, shapes, spacing
│   │       │   ├── screens/
│   │       │   │   ├── HomeScreen.kt       # Dashboard with Quick Scan + recent/saved cards
│   │       │   │   ├── ScanningScreen.kt   # Scanning animation (3s) with pulsing icon
│   │       │   │   ├── ScanningOverlayScreen.kt  # Full-screen overlay scanning UI
│   │       │   │   ├── ScanResultScreen.kt       # Full verification result + evidence
│   │       │   │   ├── FloatingResultOverlay.kt  # Bottom sheet overlay (35% height)
│   │       │   │   ├── HistoryScreen.kt          # Searchable, day-grouped history
│   │       │   │   ├── SavedScreen.kt            # Bookmarked results (empty state)
│   │       │   │   └── SettingsScreen.kt         # Permissions, API key, clear history
│   │       │   └── components/
│   │       │       └── Common.kt          # Reusable: VerdictBadge, ConfidenceBadge,
│   │       │                              #   FactCard, EvidenceCard, HistoryCard,
│   │       │                              #   PrimaryButton, SecondaryButton, SectionHeader
│   │       ├── overlay/
│   │       │   ├── OverlayService.kt       # Foreground service with draggable F button
│   │       │   └── ResultOverlayHelper.kt  # WindowManager helper for result overlay
│   │       ├── capture/
│   │       │   ├── ScreenCaptureManager.kt # Permission + capture request management
│   │       │   └── ScreenCaptureService.kt # MediaProjection foreground capture service
│   │       ├── ocr/
│   │       │   └── OCRProcessor.kt         # IntentService: ML Kit OCR → verify text
│   │       ├── network/
│   │       │   ├── SearchClient.kt         # DuckDuckGo HTML scrape + parse (Android-side)
│   │       │   └── VerdictEngine.kt        # Gemini direct call + keyword fallback
│   │       └── history/
│   │           └── HistoryDatabase.kt      # Room DAO + Database singleton
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── backend/                               # FastAPI backend
│   ├── main.py                            # API server (POST/GET /verify, /health)
│   ├── models.py                          # Pydantic request/response models
│   ├── verification_pipeline.py            # End-to-end orchestration
│   ├── claim_detector.py                  # Claim extraction + filtering
│   ├── search_engine.py                   # DuckDuckGo search via httpx + BeautifulSoup
│   ├── ranker.py                          # Source scoring and sorting
│   ├── llm_client.py                      # OpenAI / Gemini unified client
│   ├── .env.example                       # API key template
│   ├── requirements.txt                   # Python dependencies
│   ├── pyproject.toml
│   ├── Dockerfile
│   └── tests/                             # pytest test suite
├── docker-compose.yml
├── design_refs/                           # Design specification
│   └── stitch_factlens_mobile_ai_verification/
│       └── factlens/DESIGN.md             # Comprehensive Material 3 design system
├── FactLens-v1.0-hackathon.apk            # Hackathon build
├── FactLens-v1.1-poc.apk                  # Updated PoC build
└── README.md                              # Original project README
```

---

## 8. Progress & Development Status

### Legend
- ✅ **Complete** — Fully implemented and functional
- ⚠️ **Partial / MVP** — Works but has limitations or hardcoded placeholders
- 🔲 **Planned / TODO** — Not yet implemented

### 8.1 Android App

| Feature | Status | Notes |
|---|---|---|
| Overlay Service (floating button) | ✅ | Draggable, pulsing animation, foreground service |
| Screen Capture (MediaProjection) | ✅ | Captures to PNG in cache, auto-triggers OCR |
| On-Device OCR (ML Kit) | ✅ | English + Indonesian, runs in IntentService |
| Claim Detection | ✅ | Regex-based URL/mention/hashtag stripping, opinion filter |
| Network — FastAPI Backend Path | ✅ | Retrofit client calls `POST /verify` |
| Network — Direct Gemini Path | ✅ | `VerdictEngine.kt` calls Gemini API directly via OkHttp |
| DuckDuckGo Search (Android-side) | ✅ | `SearchClient.kt` parses DuckDuckGo HTML |
| Local History (Room DB) | ✅ | Full CRUD, Flow-based queries, favorites toggle |
| Verdict UI (badge, confidence, evidence) | ✅ | `FactCard`, `EvidenceCard`, `VerdictBadge`, `ConfidenceBadge` |
| Floating Result Overlay | ✅ | Bottom sheet at 35% height with dim background |
| Setup Flow (permissions) | ✅ | Step-by-step overlay permission + service start |
| Bottom Navigation (4 tabs) | ✅ | Home, History, Saved, Settings |
| Home Screen Quick Scan | ✅ | Button with scanning animation (3s) |
| Scanning Animation | ✅ | Pulsing camera icon + progress bar |
| Scan Result Screen | ✅ | Full verdict display, evidence list, save/share actions |
| History Screen | ✅ | Search bar, day-grouped, mock history entries |
| Dark Mode | 🔲 | Design spec supports it, not yet implemented |
| Settings — API Key Configuration | ⚠️ | UI stub exists, `onAction` is a TODO |
| Settings — Clear History | ⚠️ | UI stub exists, `onAction` is a TODO |
| Bookmark / Saved Logic | ⚠️ | UI exists (`SavedScreen`, bookmark icons), actual toggle logic is TODO |
| Share Result | 🔲 | Share button exists but `onClick` is empty |
| Home Screen — Real Data | ⚠️ | Currently uses **hardcoded mock cards** |
| History Screen — Real Data | ⚠️ | Currently uses **hardcoded mock entries** (not querying Room) |
| Saved Screen — Real Data | ⚠️ | Shows empty state, no Room query integration |
| Overlay Result — Bookmark Logic | ⚠️ | `onClick` is a TODO stub |
| Notification Permission (Android 13+) | ✅ | Requests `POST_NOTIFICATIONS` |
| Adaptive Layouts (tablets) | 🔲 | Not addressed |
| Accessibility — Dynamic Text | ⚠️ | Basic support via Material 3, no explicit testing |
| Accessibility — Screen Reader | 🔲 | Not explicitly configured |

### 8.2 Backend (FastAPI)

| Feature | Status | Notes |
|---|---|---|
| API Server | ✅ | Health check, POST/GET `/verify` with CORS |
| Verification Pipeline | ✅ | End-to-end claim → search → rank → verdict |
| Claim Detector | ✅ | URL/mention/hashtag stripping, opinion detection |
| DuckDuckGo Search Engine | ✅ | Async httpx client, HTML scraping via BeautifulSoup |
| Source Ranker | ✅ | Domain authority + keyword overlap scoring |
| LLM Client (OpenAI) | ✅ | GPT-4o-mini, structured JSON output via system prompt |
| LLM Client (Gemini) | ✅ | Gemini 1.5 Flash, same interface |
| Fallback Verdict (no LLM) | ✅ | Keyword-heuristic fallback with source listing |
| Docker Support | ✅ | Dockerfile + docker-compose with env vars |
| Testing | ⚠️ | pytest directory exists (`tests/`), scope unknown |
| Rate Limiting | 🔲 | Not implemented |
| Authentication | 🔲 | Not needed (no login required by design) |
| Caching | 🔲 | Each request re-searches DuckDuckGo |
| Indonesian Source Support | ✅ | `kemkes.go.id`, `kmov.id`, `turnbackhoax.id` in trusted domains |
| Error Handling | ✅ | HTTP 400 for short text, exception-safe LLM parsing |

### 8.3 Overall Project Maturity

| Aspect | Status |
|---|---|
| **Core Workflow** (tap → capture → OCR → search → verdict → display) | ✅ Complete |
| **UI/UX** | ⚠️ Good structure, hardcoded data on main screens |
| **On-Device AI** (direct Gemini call) | ✅ Functional |
| **Backend AI** (FastAPI + LLM) | ✅ Functional |
| **Design System** (Material 3, branding) | ✅ Comprehensive spec, partially implemented |
| **Polish / Real Data Integration** | ⚠️ Remaining work: connect Room DB to UI, wire up bookmark/share |
| **Production Readiness** | ❌ Hackathon/PoC — needs testing, error handling hardening, monitoring |
| **Deployment** | ⚠️ Docker-ready, no cloud deployment config |

---

## 9. End-to-End User Flow

```
1. User installs FactLens
2. Opens app → sees setup screen
3. Grants overlay permission → allows system setting
4. Taps "Start Overlay" → "F" floating button appears
5. User navigates to any app (e.g., Instagram, X, WhatsApp)
6. Sees suspicious claim → taps "F" button
7. Screen capture triggers (brief notification shown)
8. OCR extracts text from screenshot image
9. Text is cleaned/analyzed for claim detection
10. HTTP request sent to:
    a. FastAPI backend (if configured) OR
    b. Directly to Gemini API (if API key set in app)
11. Search engine finds evidence from trusted sources
12. Sources ranked by authority + relevance
13. LLM evaluates claim vs evidence → structured verdict
14. Result overlay appears as bottom sheet (max 35% screen)
15. User sees: verdict badge + confidence % + explanation
16. Options: View Full Details, Save, Dismiss (swipe/tap away)
17. Scan auto-saved to local history
18. User can review history later, search, bookmark favorites
```

---

## 10. How to Run

### Backend
```bash
cd backend
pip install -r requirements.txt
cp .env.example .env   # Add OPENAI_API_KEY or GEMINI_API_KEY (optional)
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Docker
```bash
docker-compose up --build
```

### Android App
1. Open `android/` in Android Studio
2. Sync Gradle
3. Run on device/emulator (API 27+)
4. Grant overlay permission
5. Tap "Start Overlay"
6. Navigate to any app, tap "F" to verify

---

## 11. APK Builds

| File | Version | Description |
|---|---|---|
| `FactLens-v1.0-hackathon.apk` | v1.0 | Original hackathon submission build |
| `FactLens-v1.1-poc.apk` | v1.1 | Updated proof-of-concept with improvements |

---

## 12. License

MIT
