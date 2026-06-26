# FactLens

> Verify information directly from your screen.

FactLens is an AI-powered Android application that lets you verify suspicious information **without leaving your current app**. Tap the floating overlay button while browsing Instagram, TikTok, X, WhatsApp, or any app — FactLens captures the screen, extracts claims using OCR, retrieves evidence from trusted sources, and presents an AI-powered verdict in seconds.

Built for **Gemini AI Hackathon 2025**.

---

## ✨ Features

- **Floating Overlay** — Always-accessible "F" button that works across any app
- **Screen Capture** — One-tap capture via Android MediaProjection API
- **On-Device OCR** — Google ML Kit text recognition (English + Indonesian)
- **Claim Detection** — Extracts factual claims, filters opinions/jokes/ads
- **Evidence Retrieval** — Searches trusted sources (Reuters, AP, BBC, WHO, academic, government)
- **AI Verdict** — 6 verdict types with confidence scores and source-backed explanations
- **History** — Room database stores all scans locally
- **No Login Required** — Zero friction, open and use

### Verdict Types

| Verdict | Meaning |
|---|---|
| **Supported** | Evidence supports the claim |
| **Contradicted** | Evidence contradicts the claim |
| **Misleading** | Claim is partially true but presented deceptively |
| **Mixed** | Evidence is conflicting |
| **Insufficient Evidence** | Not enough reliable sources found |
| **Unknown** | Could not process |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│                  Android App                     │
│  ┌─────────┐  ┌──────────┐  ┌────────────────┐ │
│  │ Overlay  │→│ Capture  │→│  ML Kit OCR    │ │
│  │ Service  │  │ Service  │  │  (on-device)   │ │
│  └─────────┘  └──────────┘  └───────┬────────┘ │
│                                      │           │
│  ┌───────────────────────────────────▼────────┐ │
│  │           Retrofit HTTP Client             │ │
│  └───────────────────┬───────────────────────┘ │
└──────────────────────┼──────────────────────────┘
                       │ POST /verify
┌──────────────────────▼──────────────────────────┐
│               FastAPI Backend                    │
│  ┌─────────┐  ┌──────────┐  ┌────────────────┐ │
│  │  Claim  │→│  Search  │→│  LLM Verdict   │ │
│  │ Detector│  │  Engine  │  │ (OpenAI/Gemini)│ │
│  └─────────┘  └──────────┘  └────────────────┘ │
│                ┌──────────┐                     │
│                │  Ranker  │                     │
│                └──────────┘                     │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Backend

```bash
cd backend

# Install dependencies
pip install -r requirements.txt

# Optional: set API key for AI verdicts
cp .env.example .env
# Edit .env and add OPENAI_API_KEY or GEMINI_API_KEY

# Run server
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

> **No API key?** The backend falls back to returning sources without AI analysis — still functional, just less polished.

### Docker

```bash
docker-compose up --build
```

### Android App

1. Open `android/` in Android Studio
2. Sync Gradle
3. Run on device/emulator (API 27+)
4. Grant overlay permission → tap "Allow"
5. Tap "Start Overlay" → the "F" button appears
6. Navigate to any app, tap "F" to verify

> **Note:** The app connects to `10.0.2.2:8000` (emulator → host). For a physical device, change `BASE_URL` in `android/app/src/main/java/com/factlens/network/FactLensApi.kt` to your server's IP.

---

## 🧪 Tests

```bash
cd backend
python -m pytest tests/ -v
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Mobile | Kotlin, Jetpack Compose, Coroutines |
| OCR | Google ML Kit (on-device) |
| Backend | FastAPI (Python) |
| LLM | OpenAI GPT-4o-mini / Gemini 1.5 Flash |
| Search | DuckDuckGo (no API key needed) |
| Storage | Room (Android), in-memory (backend) |
| Deploy | Docker, docker-compose |

---

## 📁 Project Structure

```
├── android/                    # Android app (Kotlin + Compose)
│   ├── app/src/main/java/com/factlens/
│   │   ├── MainActivity.kt           # Permission setup UI
│   │   ├── overlay/                  # Floating button + result card
│   │   ├── capture/                  # MediaProjection screen capture
│   │   ├── ocr/                      # ML Kit text recognition
│   │   ├── network/                  # Retrofit API client
│   │   ├── history/                  # Room database
│   │   └── model/                    # Data classes
│   └── build.gradle.kts
├── backend/                    # FastAPI verification engine
│   ├── main.py                 # API server
│   ├── verification_pipeline.py # End-to-end pipeline
│   ├── claim_detector.py       # Claim extraction
│   ├── search_engine.py        # DuckDuckGo search
│   ├── ranker.py               # Source ranking
│   ├── llm_client.py           # OpenAI / Gemini client
│   └── tests/                  # pytest tests
├── docker-compose.yml
└── README.md
```

---

## 👥 Team FactLens

Built for **Gemini AI Hackathon 2025**.

---

## 📄 License

MIT
