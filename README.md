# AntiTimpa

> **Blur scams before they blur your judgment.**
>
> Scam detector Android app. Tap & hold FAB → blur overlay → OCR → backend check → verdict.

A floating overlay button appears on top of any app (IG, WA, TikTok, etc.). Long-press 2 seconds → screen gets blurred → OCR reads the text → backend checks for scam patterns → result card shows verdict (Aman/Mencurigakan/Terindikasi Penipuan) with flagged items.

---

## Quick Start

**1. Install APK** — `./gradlew assembleDebug` → deploy via Android Studio or `adb install`

**2. Backend** (Person A):
```bash
cd backend/anti_timpa
pip install -r requirements.txt
uvicorn anti_timpa.app:app --host 0.0.0.0 --port 8000 --reload
```

**3. Run** — Grant overlay + screen recording permission → FAB appears → hold "F" button 2s

---

## Stack

| Layer | Tech |
|-------|------|
| Android | Kotlin, Compose, WindowManager, ML Kit OCR, Room, OkHttp |
| Backend | Python FastAPI, NaraRouter (OpenAI-compatible LLM) |
| Database | Room (local), none on backend (stateless) |

---

## Repo Structure

```
android/      → Android app (Kotlin)
backend/      → Python backend (FastAPI)
AGENTS.md     → Technical reference for AI agents (module map, architecture, gotchas)
specs.md      → Original product specification
```

---

For detailed architecture, module map, key decisions, and gotchas → see [`AGENTS.md`](AGENTS.md).

For backend API docs → see [`backend/API.md`](backend/API.md).
