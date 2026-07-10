# FactLens — Product Specification Document (MVP v1.0)

> **Tagline:** Verify information directly from your screen. Zero friction. Zero app switching.

---

## 1. Product Vision

FactLens is an AI-powered Android overlay application that lets users verify suspicious information **without leaving their current app**. A user reading a post on TikTok, Instagram, X, WhatsApp, or any app can trigger FactLens with a single long-press gesture on a floating button. The app captures the screen, extracts text via on-device OCR, runs it through a claim-detection + evidence-retrieval + LLM verdict pipeline, and displays a floating result card — all without the user ever switching context.

**Core promise:** Truth in one gesture. No screenshots. No copy-paste. No app switching.

---

## 2. User Flow (End-to-End)

```
┌──────────────────────────────────────────────────────────────────────┐
│  1. USER IN TIKTOK (or any app)                                      │
│                                                                      │
│  ┌──────────────────────────────────────────────┐                   │
│  │  TikTok feed with a claim post               │                   │
│  │                                              │                   │
│  │  "Vaksin COVID menyebabkan autisme pada      │                   │
│   │  anak-anak" [scrolling]                       │                   │
│  │                                              │                   │
│  │         ┌────────────┐                       │                   │
│  │         │ 🔒 FAB     │  ← floating button    │                   │
│  │         └────────────┘    always on top       │                   │
│  └──────────────────────────────────────────────┘                   │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│  2. LONG-PRESS FLOATING BUTTON (2 detik)                             │
│                                                                      │
│  ● Haptic feedback (vibrate) on press-down                           │
│  ● Circular progress ring animates around button (2s fill)           │
│  ● On completion → double vibrate → triggers capture                 │
│  ● Prevents accidental taps (tap ≠ hold)                             │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│  3. SCREEN CAPTURE                                                   │
│                                                                      │
│  Android MediaProjection API captures current screen as Bitmap       │
│  Saved to app cache as PNG (auto-deleted after processing)           │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│  4. TRANSPARENT ANALYZING INDICATOR (Overlay Layer)                  │
│                                                                      │
│  ┌──────────────────────────────────────────────┐                   │
│  │  [TikTok content visible behind]             │                   │
│  │                                              │                   │
│  │  ┌──── overlay dim (alpha 0.2) ────┐        │                   │
│  │  │                                  │        │                   │
│  │  │   ┌──────────────────────┐      │        │                   │
│  │  │   │ 🔍 FactLens AI       │      │        │                   │
│  │  │   │ Menganalisis...      │      │        │                   │
│  │  │   │ ████████░░░░ 68%     │      │        │                   │
│  │  │   └──────────────────────┘      │        │                   │
│  │  │                                  │        │                   │
│  │  └──────────────────────────────────┘        │                   │
│  │                                              │                   │
│  └──────────────────────────────────────────────┘                   │
│                                                                      │
│  ● Semi-transparent dim overlay (backdrop content still visible)     │
│  ● Compact glassmorphic card at center or bottom-sheet               │
│  ● Animated pulse on scanning icon                                   │
│  ● Progress bar (determinate if steps known, indeterminate if not)   │
│  ● Text: "Menganalisis klaim..." / "Mencari sumber..." / "AI verdict"│
│  ● Dismissible at any point (tap outside or swipe down)              │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│  5. PROCESSING PIPELINE (Backend + On-Device)                        │
│                                                                      │
│  ┌──────────┐    ┌──────────┐    ┌────────────┐    ┌────────────┐  │
│  │ ML Kit   │───→│ Claim    │───→│ Search     │───→│ LLM        │  │
│  │ OCR      │    │ Detector │    │ Engine     │    │ Verdict    │  │
│  └──────────┘    └──────────┘    └────────────┘    └────────────┘  │
│  (on-device)     (on-device)     (backend)          (backend)        │
│                                                                      │
│  Steps (each updates progress bar):                                  │
│    1. OCR — Mengenali teks dari layar (25%)                          │
│    2. Claim Detection — Mengekstrak klaim faktual (40%)               │
│    3. Evidence Search — Mencari sumber terpercaya (65%)              │
│    4. AI Analysis — Menganalisis dengan AI (85%)                     │
│    5. Selesai! (100%)                                                │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│  6. FLOATING RESULT OVERLAY                                          │
│                                                                      │
│  ┌──────────────────────────────────────────────┐                   │
│  │  [TikTok content visible behind, dimmed]      │                   │
│  │                                              │                   │
│  │  ┌──────────────── bottom sheet ──────────┐  │                   │
│  │  │ ─── (drag handle)                       │  │                   │
│  │  │                                         │  │                   │
│  │  │  ⚠️ Menyesatkan                   72%  │  │                   │
│  │  │  (Misleading)                    (conf) │  │                   │
│  │  │                                         │  │                   │
│  │  │  "Klaim ini mengandung kebenaran        │  │                   │
│  │  │   parsial tetapi menghilangkan konteks  │  │                   │
│  │  │   penting..."                           │  │                   │
│  │  │                                         │  │                   │
│  │  │  📄 Sumber:                             │  │                   │
│  │  │  • Reuters — Fact Check: COVID vax     │  │                   │
│  │  │  • WHO — Vaccine Safety Statement      │  │                   │
│  │  │                                         │  │                   │
│  │  │  [Lihat Detail]              [🔖]      │  │                   │
│  │  └─────────────────────────────────────────┘  │                   │
│  │                                              │                   │
│  └──────────────────────────────────────────────┘                   │
│                                                                      │
│  ● Bottom sheet: max 35%–50% screen height                           │
│  ● Rounded top corners (24dp)                                        │
│  ● Draggable (vertical drag to expand/dismiss)                       │
│  ● Dismiss via swipe-down, tap backdrop, or close button             │
│  ● Verdict color-coded: Green/Red/Amber/Gray                        │
│  ● Tapping "Lihat Detail" opens full ScanResult screen in app       │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. Verdict System

| Verdict (EN) | Verdict (ID) | Color | Meaning |
|---|---|---|---|
| **Supported** | Terbukti | Green (`#006D44`) | Evidence supports the claim |
| **Contradicted** | Terbantahkan | Red (`#BA1A1A`) | Evidence contradicts the claim |
| **Misleading** | Menyesatkan | Amber (`#924C00`) | Claim is partially true but presented deceptively |
| **Mixed** | Campuran | Amber (`#924C00`) | Evidence is conflicting |
| **Insufficient Evidence** | Bukti Tidak Cukup | Gray (`#717782`) | Not enough reliable sources found |
| **Unknown** | Tidak Diketahui | Gray (`#717782`) | Could not process |

- Confidence score: `0.0` – `0.95` (never 100%)
- Every verdict must include explanation + sources
- Never rely on color alone — always include text label

---

## 4. Technical Architecture

### 4.1 System Overview

```
┌────────────────────────────────────────────────────────────────────┐
│                        ANDROID APP                                 │
│                                                                    │
│  ┌──────────────┐   ┌──────────────────┐   ┌───────────────────┐  │
│  │ OverlayService│──→│ ScreenCapture    │──→│ OCRProcessor      │  │
│  │ (FAB + result)│   │ (MediaProjection)│   │ (ML Kit on-device)│  │
│  └──────────────┘   └──────────────────┘   └────────┬──────────┘  │
│                                                      │             │
│  ┌───────────────────────────────────────────────────▼──────────┐  │
│  │                 VerdictEngine (HTTP Client)                   │  │
│  │  POST /verify  ────────────────────────────────  Backend     │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Room DB (HistoryDao)    │    Compose UI (MainActivity)      │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
                           │ POST /verify
                           ▼
┌────────────────────────────────────────────────────────────────────┐
│                      FASTAPI BACKEND                               │
│                                                                    │
│  ┌──────────────┐   ┌──────────────────┐   ┌───────────────────┐  │
│  │ ClaimDetector│──→│ SearchEngine     │──→│ LLMClient         │  │
│  │ (heuristic)  │   │ (DuckDuckGo API) │   │ (OpenAI/Gemini)   │  │
│  └──────────────┘   └──────┬───────────┘   └───────────────────┘  │
│                            │                                       │
│                     ┌──────▼───────┐                               │
│                     │  Ranker      │                               │
│                     │  (trust      │                               │
│                     │   scoring)   │                               │
│                     └──────────────┘                               │
└────────────────────────────────────────────────────────────────────┘
```

### 4.2 Android Layer

| Component | Technology | Purpose |
|---|---|---|
| OverlayService | Kotlin + Compose | Floating button + result overlay via `WindowManager` |
| ScreenCaptureManager | MediaProjection API | Capture screen as Bitmap |
| OCRProcessor | Google ML Kit (on-device) | Text recognition (EN + ID) |
| VerdictEngine | OkHttp + Gemini API | Calls backend or direct Gemini |
| SearchClient | OkHttp + HTML parsing | DuckDuckGo fallback search |
| HistoryDatabase | Room | Local scan history |
| UI | Jetpack Compose + Material 3 | Main app + overlay UI |

### 4.3 Backend Layer

| Component | Technology | Purpose |
|---|---|---|
| FastAPI server | Python + uvicorn | REST API endpoint `/verify` |
| VerificationPipeline | Python | Orchestrates claim→search→verdict flow |
| ClaimDetector | Regex + heuristics | Extract factual claims, filter noise |
| SearchEngine | httpx + BeautifulSoup | Scrape DuckDuckGo HTML results |
| Ranker | Scoring algorithm | Prioritize trusted domains (.gov, .edu, major news) |
| LLMClient | OpenAI / Gemini API | AI-powered verdict generation |

### 4.4 Data Models

```python
# Backend -> Android
VerificationResponse {
    claim: str           # Extracted claim text
    verdict: str         # Supported|Contradicted|Misleading|Mixed|Insufficient Evidence|Unknown
    confidence: float    # 0.0 - 0.95
    explanation: str     # 2-4 sentence AI explanation
    sources: [Source]    # 0-5 evidence sources
}

Source {
    title: str
    url: str
    snippet: str
}
```

```kotlin
// Android Room Entity
@Entity(tableName = "ScanHistory")
ScanHistory {
    id: Long (auto)
    timestamp: Long
    claim: String
    verdict: String
    confidence: Double
    explanation: String
    sourcesJson: String (JSON serialized)
    screenshotPath: String?
    isFavorite: Boolean
}
```

---

## 5. UI/UX Specifications

### 5.1 Floating Trigger Button (FAB)

| Property | Value |
|---|---|
| Shape | Circle (56dp) |
| Icon | Shield/Security (`Icons.Filled.Security`) |
| Color | Primary Blue (`#00497D`) |
| Position | Right edge, vertically centered (draggable) |
| Badge | Red dot ("New" indicator) when first launched |
| Pulse | Infinite scale animation (0.7→1.4, 2500ms) |
| Interaction | Hold 2s → capture; tap → nothing (prevents accidental) |
| Drag | Full vertical + horizontal (constrained within bounds) |
| Layer | `TYPE_APPLICATION_OVERLAY` (API 23+) |

**Long-press mechanics:**
- `ACTION_DOWN` → haptic feedback, start progress ring animation
- Progress ring fills over 2000ms (circular `Canvas` drawArc)
- `ACTION_UP` before 2s → cancel, no action
- `ACTION_UP` after 2s → trigger capture, double haptic
- Progress ring drawn as `strokeCap = Round` on button border

### 5.2 Scanning Indicator Overlay

| Property | Value |
|---|---|
| Dim amount | 0.2 alpha (content visible behind) |
| Card style | Glassmorphic (blur + semi-transparent, if supported) |
| Max height | 35% screen |
| Position | Bottom sheet or centered |
| Animation | Scanning icon spins/pulses; progress bar animates |
| Dismiss | Tap outside or swipe down |
| Progress stages | OCR (25%), Claim Detection (40%), Search (65%), AI (85%), Done (100%) |

**Scanning states:**
```
State 1: "Mengenali teks dari layar..."    ████████░░░░ 25%
State 2: "Mengekstrak klaim faktual..."    ████████████░░ 40%
State 3: "Mencari sumber terpercaya..."    ████████████████░░ 65%
State 4: "Menganalisis dengan AI..."       ████████████████████░░ 85%
State 5: "Verifikasi selesai!"            ████████████████████████ 100%
```

**Transitions (200-300ms each):**
- Fade in dim overlay: 200ms
- Slide up bottom card: 300ms (ease-out)
- Progress bar fill: animated per state
- Staggered text updates

### 5.3 Result Overlay

| Property | Value |
|---|---|
| Style | Bottom sheet (rounded top 24dp) |
| Max height | 35%–50% (expandable) |
| Background | Pure White (`#FFFFFF`) |
| Elevation | Soft shadow |
| Drag handle | 4dp × 32dp rounded bar at top center |
| Verdict header | Icon + label + confidence badge |
| Summary | 2-4 sentence explanation |
| Sources | Max 3 displayed with title + domain |
| Actions | "Lihat Detail" (primary), bookmark icon, share icon |
| Dismiss | Swipe down, tap backdrop, or close (x) button |
| Duration | Auto-dismiss after 30s (configurable) |
| Edge case | If no sources: show "Bukti Tidak Cukup" with suggestion to try again |

### 5.4 Main App Screens

| Screen | Purpose | Key Elements |
|---|---|---|
| **Setup** | First-launch permission flow | Step 1: Overlay permission → Step 2: Start service |
| **Home** | Dashboard | Quick Scan button, recent scans (3 latest), saved section |
| **Scan Result** | Full detail view | Verdict card, evidence list, "Open Source" links, save/share |
| **History** | All scans | Search bar, grouped by day, filterable, swipe to delete |
| **Saved** | Bookmarked scans | Same as history but filtered to favorites |
| **Settings** | App preferences | Overlay permission toggle, API key config, about, theme toggle |

---

## 6. Backend API Specification

### `POST /verify`

**Request:**
```json
{
    "text": "Vaksin COVID menyebabkan autisme pada anak-anak",
    "language": "id"
}
```

**Response (200):**
```json
{
    "claim": "Vaksin COVID menyebabkan autisme pada anak-anak",
    "verdict": "Contradicted",
    "confidence": 0.92,
    "explanation": "Klaim ini telah terbantahkan oleh banyak studi ilmiah...",
    "sources": [
        {
            "title": "WHO: No link between vaccines and autism",
            "url": "https://www.who.int/...",
            "snippet": "Extensive research shows no connection..."
        }
    ]
}
```

**Error (400):** `{"detail": "Text too short"}`
**Error (500):** `{"detail": "Internal server error"}`

### `GET /health`

Response: `{"status": "ok"}`

### Offline/Fallback Mode

If no backend connection:
1. OCR + on-device keyword heuristic verdict
2. Search via direct DuckDuckGo HTML scraping from Android
3. Direct Gemini API call from Android (if API key configured)
4. Show "Offline Mode — hasil terbatas" indicator

---

## 7. Android Implementation Details

### 7.1 Permissions

| Permission | Purpose | Required At |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Overlay drawing | Runtime (Settings intent) |
| `FOREGROUND_SERVICE` | Keep overlay alive | Manifest |
| `POST_NOTIFICATIONS` (API 33+) | Foreground service notification | Runtime |
| MediaProjection | Screen capture | Runtime (intent) |
| `INTERNET` | Backend communication | Manifest |

### 7.2 Key Technical Decisions

- **Compose for overlay UI**: `ComposeView` inside `WindowManager` — enables reactive UI in overlay
- **Long-press over tap**: Prevents accidental triggers during normal scrolling
- **Progress stages from client**: Android estimates progress stages (not real backend progress) — backend is black box
- **ML Kit on-device OCR**: No network needed for text extraction; privacy-preserving
- **Room for history**: Local-first; all scans saved even without network
- **Gemini API key optional**: Falls back to keyword heuristics if no LLM configured

### 7.3 States & Edge Cases

| Scenario | Behavior |
|---|---|
| No text detected in screenshot | Show "Tidak ada teks terdeteksi" overlay |
| Text too short (< 5 chars) | Show "Teks terlalu pendek untuk dianalisis" |
| No internet connection | Offline mode indicator; fallback to on-device heuristics |
| Backend timeout (30s+) | Show "Waktu habis — coba lagi" with retry button |
| MediaProjection denied | Show toast "Izin capture diperlukan" |
| Overlay permission revoked | Pause service; show notification to re-grant |
| Screen rotated during capture | Capture whatever orientation is current |
| Very long text (>1000 chars) | Truncate to first 1000 chars for analysis |
| No sources found | Verdict = "Insufficient Evidence"; suggest manual search |

---

## 8. Design System

### 8.1 Color Palette

| Token | Light | Dark |
|---|---|---|
| Primary | `#00497D` | `#9FCAFF` |
| Background | `#F8F9FF` | `#111318` |
| Surface | `#FFFFFF` | `#1D2024` |
| Success | `#006D44` | `#80D8A6` |
| Warning | `#924C00` | `#FFB784` |
| Error | `#BA1A1A` | `#FFB4AB` |

### 8.2 Typography

- Font: **Inter** (single font throughout)
- Scale: Display (57px) → Headline (32/24px) → Title (22/16px) → Body (16/14px) → Caption (12px)

### 8.3 Spacing

- `xs: 4px`, `sm: 8px`, `md: 12px`, `lg: 16px`, `xl: 24px`, `2xl: 32px`, `3xl: 48px`
- No arbitrary spacing values

### 8.4 Border Radius

- `sm: 4px`, `DEFAULT: 8px`, `md: 12px`, `lg: 16px`, `xl: 24px`, `full: 9999px`
- Overlay card top corners: `24px`
- Buttons: `16px`

---

## 9. MVP Feature Checklist

### Core (Must-Have for Showcase)

| # | Feature | Status |
|---|---|---|
| 1 | Floating overlay button (draggable) | ✅ Done |
| 2 | Long-press 2s trigger (not tap) | ✅ Done |
| 3 | Haptic feedback on trigger | ⬜ TODO |
| 4 | Progress ring animation on button | ⬜ TODO |
| 5 | Screen capture via MediaProjection | ✅ Done |
| 6 | On-device OCR (ML Kit) | ✅ Done |
| 7 | Scanning indicator overlay with progress bar | ⬜ TODO |
| 8 | Backend claim → search → verdict pipeline | ✅ Done |
| 9 | Floating result bottom sheet | ✅ Done |
| 10 | Verdict: Supported/Contradicted/Misleading | ✅ Done |
| 11 | Confidence score display | ✅ Done |
| 12 | Source evidence display | ✅ Done |
| 13 | Dismiss overlay (swipe/tap outside) | ⬜ TODO |
| 14 | History saved locally (Room) | ✅ Done |
| 15 | Main app Home/History/Saved/Settings | ✅ Done |
| 16 | Indonesian language support (UI + prompt) | ⬜ TODO |
| 17 | Dark mode | ⬜ TODO |
| 18 | Setup screen with permission flow | ✅ Done |

### Polished (Nice-to-Have for Showcase)

| # | Feature | Status |
|---|---|---|
| 19 | Glassmorphic scanning card | ⬜ TODO |
| 20 | Smooth entrance/exit animations (200-300ms) | ⬜ TODO |
| 21 | Skeleton loading for scan result | ⬜ TODO |
| 22 | Bookmark/favorite scans | ⬜ TODO |
| 23 | Share scan result | ⬜ TODO |
| 24 | Empty states for History/Saved | ⬜ TODO |
| 25 | Error state overlays | ⬜ TODO |
| 26 | Demo video recording capability | ⬜ TODO |
| 27 | App icon + branding | ✅ Done |
| 28 | Docker deployment for backend | ✅ Done |

---

## 10. Demo Script (For Competition Showcase)

```
1. Open TikTok → find a post with a viral claim
2. Show the FactLens FAB floating on screen
3. Press and hold the FAB for 2 seconds
4. Haptic feedback → capture takes effect
5. Transparent overlay appears: "Menganalisis..."
6. Progress bar advances through stages
7. Result slides up: "Menyesatkan — 72%"
8. Tap "Lihat Detail" → full Scan Result screen
9. Show History → all previous scans saved
10. Show Saved → bookmarked scans
```

---

## 11. File Structure (Android)

```
android/app/src/main/java/com/factlens/
├── MainActivity.kt              # Entry point, navigation
├── capture/
│   ├── ScreenCaptureManager.kt  # MediaProjection wrapper
│   └── ScreenCaptureService.kt  # Foreground service for capture
├── history/
│   └── HistoryDatabase.kt       # Room DB + DAO
├── model/
│   └── Models.kt                # Data classes + Room Entity
├── network/
│   ├── SearchClient.kt          # DuckDuckGo direct search
│   └── VerdictEngine.kt         # Backend/Gemini API client
├── ocr/
│   └── OCRProcessor.kt          # ML Kit text recognition
├── overlay/
│   ├── OverlayService.kt        # FAB overlay service
│   └── ResultOverlayHelper.kt   # Result bottom sheet helper
└── ui/
    ├── components/
    │   └── Common.kt            # Shared composables
    ├── screens/
    │   ├── HomeScreen.kt
    │   ├── HistoryScreen.kt
    │   ├── SavedScreen.kt
    │   ├── SettingsScreen.kt
    │   ├── ScanningScreen.kt
    │   ├── ScanningOverlayScreen.kt
    │   ├── ScanResultScreen.kt
    │   └── FloatingResultOverlay.kt
    └── theme/
        └── Theme.kt             # Material 3 theme
```

---

## 12. Performance Targets

| Metric | Target |
|---|---|
| FAB display latency | < 100ms after overlay service start |
| Capture → OCR | < 2s |
| OCR → Backend response | < 10s (with LLM) |
| Overlay animation | 200-300ms |
| App cold start | < 3s |
| Memory usage (overlay idle) | < 50MB |
| APK size | < 15MB |

---

## 13. Known Limitations (MVP)

1. **No real-time streaming:** Progress bar estimates stages; backend doesn't stream progress
2. **DuckDuckGo scraping:** Fragile; depends on HTML structure; rate limits possible
3. **Indonesian language:** OCR ML Kit supports ID, but LLM prompts need optimization for ID claims
4. **Single-screen capture:** Cannot scroll-capture or video-capture
5. **No auth:** No multi-device sync; all data is local
6. **No push notifications:** No background re-verification
7. **Accessibility:** Screen reader support needs validation

---

## 14. Future Roadmap (Post-MVP)

- Real-time progress streaming via WebSocket
- Multi-screen scroll capture
- Custom trusted source lists per user
- On-device LLM (Gemini Nano / MLCC)
- Browser extension (Chrome/Safari)
- iOS version
- Community fact-check contribution
- API for third-party integration
