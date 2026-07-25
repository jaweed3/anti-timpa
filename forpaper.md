# AntiTimpa — Scam Detection Overlay untuk Aplikasi Mobile Android

> **Blur scams before they blur your judgment.**
>
> Hackathon MVP — 2 Dev (A = Backend Python, B = Android Kotlin)

---

## BAGIAN 1: COVER

**Judul:** AntiTimpa — Sistem Deteksi Penipuan Real-Time Berbasis Overlay untuk Platform Android

**Subjudul:** *"Blur scams before they blur your judgment."*

**Tagline:** Proteksi instan dari konten penipuan di layar ponsel Anda — tanpa meninggalkan aplikasi.

**Tim Pengembang:**
- Dev A (Backend) — Python FastAPI, NaraRouter LLM, Search Engine
- Dev B (Android) — Kotlin, Jetpack Compose, ML Kit OCR, WindowManager Overlay

**Afiliasi:** Hackathon Project

**Tanggal:** Juli 2026

---

## BAGIAN 2: ABSTRAK

Maraknya konten penipuan (scam, judi online, phising) yang menyebar melalui media sosial dan aplikasi perpesanan instan menuntut adanya alat verifikasi yang cepat dan tidak mengganggu alur kerja pengguna. AntiTimpa hadir sebagai solusi overlay Android yang memungkinkan pengguna men-scan teks di layar secara instan — cukup long-press tombol FAB (Floating Action Button) selama 2 detik, maka sistem akan mengambil screenshot, membaca teks melalui OCR (Optical Character Recognition), dan menjalankan deteksi pola penipuan secara offline maupun verifikasi lanjutan melalui backend berbasis LLM (Large Language Model). Hasil deteksi ditampilkan dalam bentuk blur overlay pada konten berbahaya dan kartu hasil yang berisi verdict, item terdeteksi, serta tingkat kepercayaan. Seluruh proses berlangsung tanpa pengguna harus meninggalkan aplikasi yang sedang digunakan.

**Teknologi utama:** Kotlin Jetpack Compose, ML Kit Text Recognition, WindowManager overlay, Python FastAPI, NaraRouter (OpenAI-compatible LLM), DuckDuckGo Search.

---

## BAGIAN 3: LATAR BELAKANG & URGENSI MASALAH

### 3.1 Fenomena Penipuan Online di Indonesia

Penipuan online di Indonesia mengalami peningkatan signifikan dalam beberapa tahun terakhir. Modus yang umum ditemukan meliputi:

- **Social engineering:** Pelaku menyamar sebagai pihak bank, kurir, atau instansi resmi
- **Judol (judi online):** Menjanjikan keuntungan instan melalui tautan mencurigakan
- **Phising:** Tautan palsu yang mengarahkan korban ke halaman login tiruan
- **Pinjaman online ilegal:** Menawarkan pinjaman dengan bunga tidak wajar dan ancaman

### 3.2 Gap Solusi yang Ada

| Solusi | Kekurangan |
|--------|------------|
| Google Safe Browsing | Hanya untuk URL, tidak mencakup teks chat |
| Turnbackhoax.id | Website manual, tidak real-time |
| Aplikasi security suite (Avast, Kaspersky) | Berat, bukan tools spesifik scam detection |
| ScamAdviser | Khusus URL, tidak support teks Indonesia |
| Report-based (Kominfo, Twitter) | Reaktif — korban sudah tertipu sebelum lapor |

### 3.3 Urgensi

Korban penipuan online sering mengambil keputusan dalam hitungan detik — mentransfer uang, mengeklik tautan, atau membagikan data pribadi. Waktu respons kritis ini membutuhkan alat verifikasi yang:

1. **Real-time** — hasil dalam detik, bukan menit
2. **Non-intrusive** — tidak memaksa pengguna keluar dari aplikasi
3. **Proaktif** — mendeteksi SEBELUM korban mengambil tindakan
4. **Offline-capable** — tetap bisa mendeteksi pola dasar tanpa koneksi internet

AntiTimpa menjawab gap ini dengan pendekatan overlay-first dan dual detection pipeline.

---

## BAGIAN 4: TUJUAN, MANFAAT, & DAMPAK

### 4.1 Tujuan

1. Menyediakan alat verifikasi konten real-time yang dapat diakses dari aplikasi mana pun tanpa interrupt workflow pengguna.
2. Mendeteksi indikator penipuan — nomor rekening, nomor telepon, URL mencurigakan, pola bahasa berbahaya — secara offline (pattern matching) maupun online (LLM + search).
3. Melindungi pengguna dari konten berbahaya dengan blur overlay otomatis.

### 4.2 Manfaat

| Pihak | Manfaat |
|-------|---------|
| **Pengguna akhir** | Proteksi instan dari penipuan, edukasi melalui penjelasan deteksi |
| **Developer** | Arsitektur dual detection yang bisa dikembangkan menjadi API |
| **Komunitas** | Dataset pola scam Indonesia yang terkumpul secara anonim |

### 4.3 Dampak

Mengurangi jumlah korban penipuan online dengan menyediakan frictionless verification tool. Tidak seperti aplikasi keamanan tradisional yang membutuhkan pengguna untuk secara sadar membuka aplikasi terpisah, AntiTimpa bekerja di latar belakang dan siap digunakan kapan pun dengan satu long-press.

---

## BAGIAN 5: NILAI INOVASI & ORISINALITAS

### 5.1 Overlay-First Architecture

Tidak ada aplikasi di kelas yang sama yang melakukan screen capture + OCR + scam detection langsung dari **overlay sistem**. Pendekatan ini memungkinkan:

- Scan tanpa meninggalkan aplikasi target
- FAB yang bisa di-drag ke posisi mana pun
- Trigger long-press 2 detik untuk mencegah false trigger

### 5.2 Dual Detection Pipeline

```
/check-scam (offline)
  → pattern matching (regex + keywords)
  → instant response (< 100ms)
  → tanpa koneksi internet

/verify (online, fallback)
  → DuckDuckGo search
  → LLM analysis (NaraRouter)
  → deep verification dengan source citation
```

Pipeline pertama memberikan respons instan untuk pola yang jelas. Pipeline kedua memberikan analisis mendalam untuk konten yang lebih kompleks.

### 5.3 Blur-as-Warning

Bukan sekadar notifikasi — konten berbahaya langsung diblur secara visual, memberikan efek psikologis yang lebih kuat dibanding peringatan teks biasa.

### 5.4 Drag & Hold Gesture

FAB anti-misfire: tombol tidak bisa di-tap biasa, harus long-press 2 detik. Ini mencegah aktivasi tidak sengaja saat scrolling atau navigasi biasa.

---

## BAGIAN 6: ANALISIS KOMPETITOR & BUSINESS VIABILITY

### 6.1 Peta Kompetitor

| Aplikasi | Deteksi Teks | Real-time Overlay | Offline | Bahasa Indonesia |
|----------|-------------|-------------------|---------|-----------------|
| **AntiTimpa** | ✅ OCR + Pattern | ✅ Overlay FAB | ✅ /check-scam | ✅ |
| Google Safe Browsing | ❌ URL only | ❌ | ✅ | ❌ |
| Turnbackhoax.id | ❌ Manual | ❌ | ❌ | ✅ |
| ScamAdviser | ❌ URL only | ❌ | ❌ | ❌ |
| Avast Mobile Security | ❌ Tidak spesifik | ❌ | ✅ | ❌ |

### 6.2 Diferensiasi Utama

1. **Overlay + OCR** — Tidak ada kompetitor yang menggabungkan screen capture dari overlay dengan OCR dan scam detection
2. **Dual pipeline** — Pattern matching instan + LLM deep analysis
3. **Blur protection** — Konten berbahaya langsung diblur secara visual
4. **Indonesian-first** — Pattern library dan LLM prompt dioptimalkan untuk bahasa Indonesia

### 6.3 Business Viability

**Model MVP (saat ini):** Open-source, gratis.

**Potensi Monetisasi:**
- **Freemium:** Batas scan harian untuk pengguna gratis, unlimited untuk premium
- **Enterprise API:** API deteksi untuk marketplace atau platform e-commerce
- **White-label:** Solusi deteksi untuk perusahaan keamanan lokal

**Target Market:**
- Pengguna individu yang aktif di media sosial dan aplikasi chat
- Orang tua yang ingin melindungi anak dari judi online dan phising
- UMKM yang sering bertransaksi via WhatsApp

---

## BAGIAN 7: TEKNOLOGI YANG DIGUNAKAN

### 7.1 Android (Dev B)

| Komponen | Teknologi | Fungsi |
|----------|-----------|--------|
| Bahasa | Kotlin | Bahasa utama Android modern |
| UI Framework | Jetpack Compose | Deklaratif, reactive UI |
| Overlay | WindowManager (TYPE_APPLICATION_OVERLAY) | FAB + blur + result card di atas semua app |
| Screen Capture | MediaProjection + VirtualDisplay + ImageReader | Screenshot dari layar |
| OCR | ML Kit Text Recognition v2 | Ekstraksi teks dari screenshot |
| Network | OkHttp | HTTP client untuk komunikasi backend |
| Database | Room | Penyimpanan riwayat scan lokal |
| State Management | mutableStateOf, LaunchedEffect | Navigasi sederhana berbasis state |

### 7.2 Backend (Dev A)

| Komponen | Teknologi | Fungsi |
|----------|-----------|--------|
| Framework | Python FastAPI | REST API server |
| Validasi | Pydantic v2 | Request/response model |
| LLM | NaraRouter (OpenAI SDK, model agnes-2.0-flash) | Analisis klaim dan verifikasi |
| Search | DuckDuckGo (httpx) | Pencarian sumber referensi |
| Rate Limiting | slowapi | Proteksi dari abuse |
| Config | python-dotenv | Environment variables |

---

## BAGIAN 8: BATASAN PERANGKAT LUNAK

### 8.1 Teknis

| Batasan | Detail |
|---------|--------|
| **Android 14 VD limitation** | VirtualDisplay hanya bisa dibuat 1x per MediaProjection token. Re-grant permission diperlukan tiap 2 scan. |
| **Recording indicator** | Android 14+ menampilkan orange indicator selama MediaProjection aktif — tidak bisa dihilangkan tanpa stop projection. |
| **Backend dependency** | `/verify` endpoint membutuhkan koneksi internet dan API key NaraRouter yang valid. |
| **Tidak support iOS** | WindowManager overlay tidak tersedia di iOS. |
| **Bahasa terbatas** | Pattern library dan LLM prompt dioptimalkan untuk bahasa Indonesia dan Inggris. |

### 8.2 Fungsional

- Tidak mendeteksi gambar (hanya teks dari OCR)
- Tidak memblokir konten secara otomatis (pengguna tetap bisa dismiss blur)
- Database pola scam diperbarui secara manual (belum ada auto-update)

---

## BAGIAN 9: METODOLOGI PENGEMBANGAN

### 9.1 Metode Agile (Scrum — 2 Dev)

Metodologi Agile dengan pendekatan Scrum yang diadaptasi untuk tim kecil (2 orang). Sprint durasi 3-5 hari.

### 9.2 Sprint Progression

| Sprint | Fokus | Hasil |
|--------|-------|-------|
| **Sprint 1** | FAB Overlay + Screen Capture | FAB muncul, drag, long-press, screen capture via MediaProjection |
| **Sprint 2** | OCR + Result Display | ML Kit OCR, blur overlay, result card, dismiss flow |
| **Sprint 3** | Backend API | FastAPI server, /check-scam pattern matching, /verify LLM |
| **Sprint 4** | Integration + Bug Fixes | Android-baackend koneksi, Android 14 workaround, crash fixes |
| **Sprint 5** | Polish | Back navigation, settings UI, error handling, docs |

### 9.3 Tools

- **Version Control:** Git + GitHub
- **IDE:** Android Studio (Dev B), VS Code (Dev A)
- **Testing:** Real device (Android 14 Tecno/Transsion), curl, logcat
- **Komunikasi:** Tatap muka / chat

---

## BAGIAN 10: ARSITEKTUR SISTEM & DESAIN

### 10.1 Arsitektur Sistem

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ANDROID APP                                  │
│                                                                     │
│  ┌─────────┐   ┌────────────────┐   ┌───────────────────────────┐  │
│  │   FAB   │──→│ OverlayService │──→│ MediaProjection + VD      │  │
│  │ (drag)  │   │                │   │ → ImageReader → Bitmap    │  │
│  │ (hold)  │   │ ScreenBlur     │   │ → ML Kit OCR → text       │  │
│  └─────────┘   │ ResultOverlay  │   └───────────┬───────────────┘  │
│                └────────────────┘               │                   │
│                                                  ▼                   │
│                                         ┌────────────────┐          │
│                                         │ VerdictEngine  │          │
│                                         │ /check-scam    │          │
│                                         │ /verify        │          │
│                                         │ fallback lokal │          │
│                                         └───────┬────────┘          │
│                                                  │                   │
└──────────────────────────────────────────────────┼───────────────────┘
                                                   │ HTTP
┌──────────────────────────────────────────────────┼───────────────────┐
│                        BACKEND                   │                   │
│                                         ┌───────▼────────┐          │
│  FastAPI Server ─── /check-scam ───────→│ Extractor      │          │
│                    /verify               │ Checker        │          │
│                    /health               │ Scorer         │          │
│                                         │ Pipeline:      │          │
│                                         │ Search→Rank→   │          │
│                                         │ LLM→Verdict    │          │
│                                         └────────────────┘          │
└─────────────────────────────────────────────────────────────────────┘
```

### 10.2 Data Flow (Sequence)

```
FAB hold 2s → triggerCapture()
  ↓
hasProjection()?
  YES → startCapture() langsung
  NO  → system dialog → GRANT → moveTaskToBack + sendBroadcast(START_CAPTURE)
  ↓
startCapture():
  1. ScreenBlurOverlay.showScanningOverlay()  // merah + spinner
  2. ensureMediaProjection()                   // getMediaProjection
  3. ensureVirtualDisplay()                    // createVirtualDisplay + ImageReader
  4. capture → releaseVirtualDisplay()         // VD dilepas
  ↓
processAndSaveImage():
  1. Bitmap → blur overlay
  2. Save PNG → start OCRProcessor
  ↓
OCRProcessor → VerdictEngine.verify(text)
  → /check-scam (pattern, offline)
  → jika "Aman" → /verify (search + LLM)
  → HistorySaver → ResultOverlayHelper.showResult()
```

### 10.3 Component Diagram

```
OverlayService
├── OverlayFabHelper
│   ├── OverlayFabTouchHandler (drag + long-press)
│   ├── OverlayFabActivator (vibrate + visual feedback)
│   └── OverlayFabViewFactory (pulse animation)
├── ScreenBlurOverlay (red → blurred screenshot)
├── ResultOverlayHelper (result card on top)
│   └── OverlayCardViewFactory (verdict badge, items, buttons)
└── ScreenCaptureManager (projection token lifecycle)

VerdictEngine
├── BackendClient (OkHttp → /check-scam, /verify)
├── SearchClient (DuckDuckGo fallback)
└── GeminiClient (unused backend fallback)
```

---

## BAGIAN 11: IMPLEMENTASI TEKNIS

### 11.1 Permission Handling

Aplikasi membutuhkan 3 izin secara berurutan:

1. **POST_NOTIFICATIONS** (Android 13+) — Notifikasi untuk foreground service
2. **SYSTEM_ALERT_WINDOW** — Overlay FAB di atas aplikasi lain
3. **MediaProjection** — Screen capture untuk screenshot

Ketiga izin diatur dalam Setup Screen pertama kali aplikasi dijalankan.

### 11.2 Overlay System

- **FAB:** WindowManager dengan parameter `TYPE_APPLICATION_OVERLAY`, dapat di-drag ke posisi mana pun di layar.
- **Long-press:** Touch handler dengan `Handler.postDelayed(2000ms)`. Jika gesture drag terdeteksi sebelum 2 detik, timer di-cancel.
- **Vibrate feedback:** Aktivasi getar saat hold 2s complete sebagai konfirmasi haptic.

### 11.3 Screen Capture & OCR

- **MediaProjection + VirtualDisplay:** Membuat salinan layar dengan resolusi perangkat.
- **ImageReader:** Format RGBA_8888, dikonversi ke ARGB_8888 bitmap.
- **ML Kit OCR:** Text recognition v2, berjalan di background thread via IntentService.
- **Screenshot:** Disimpan ke `filesDir/screenshots/` dalam format PNG. Path disimpan di Room database.

### 11.4 Blur Flow

```
FAB hold → ShowScanningOverlay (merah + spinner di tengah)
  → Capture complete → updateToBlurredScreenshot (ganti merah dengan bitmap blur)
  → OCR selesai → hideProgress (spinner hilang)
  → showResult (card result DI ATAS blur)
  → User dismiss → remove result + dismiss blur (keduanya hilang)
```

### 11.5 Android 14 Workaround

- **VirtualDisplay per-scan:** VD di-release setelah setiap capture → recording indicator ilang di Android ≤13.
- **Android 14:** VD creation gagal setelah 1x per projection → `releaseMediaProjection()` + `clearProjection()` → trigger re-grant dialog.
- **`startForeground()` guard:** `onStartCommand()` hanya memanggil `startForeground()` sekali via flag `!overlayCreated` — mencegah crash saat projection sudah mati.

### 11.6 Backend API

**Endpoint `/check-scam` (pattern-based):**
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

**Endpoint `/verify` (search + LLM):**
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

---

## BAGIAN 12: ANALISIS UI/UX & ACCESSIBILITY

### 12.1 Prinsip Desain

- **Minimal interference:** FAB kecil (48dp) bisa di-drag ke mana pun, tidak blocking konten penting.
- **Intent-based trigger:** Long-press 2 detik — bukan tap — mengurangi false trigger.
- **Progressive disclosure:** Informasi muncul bertahap — merah → blur → card result.
- **Consistent feedback:** Vibrate + animasi pulse saat FAB teraktivasi.

### 12.2 Visual Hierarchy

1. **Scanning state:** Overlay merah penuh + spinner putih di tengah — memberi sinyal bahwa proses sedang berlangsung.
2. **Capture complete:** Overlay merah diganti screenshot blur — pengguna bisa melihat konten samar.
3. **Result:** Card result muncul di atas blur — verdict badge dengan emoji + color coding.
4. **Dismiss:** Kedua layer hilang — pengguna kembali ke konten normal.

### 12.3 Color Coding

| Verdict | Warna | Emoji |
|---------|-------|-------|
| Aman | Hijau | ✅ |
| Mencurigakan | Kuning | ⚠️ |
| Terindikasi Penipuan | Merah | 🚨 |

### 12.4 Accessibility

- **Content description:** Semua icon memiliki deskripsi untuk TalkBack.
- **Haptic feedback:** Vibrasi saat long-press complete — membantu pengguna dengan limited visual.
- **High contrast:** Verdict badge dengan warna kontras tinggi.
- **Bottom navigation:** Navigasi utama dengan label teks + icon, mudah dijangkau.

---

## BAGIAN 13: PENGUJIAN (TESTING)

### 13.1 Metode Pengujian

- **Real device testing:** Perangkat Android 14 (Tecno/Transsion) — seluruh fitur diuji secara end-to-end.
- **Backend testing via curl:** Setiap endpoint diuji dengan curl sebelum integrasi Android.
- **Logcat monitoring:** Filter tag `AntiTimpa.*` dan `FactLens.*` untuk melacak alur proses.

### 13.2 Skenario Pengujian

| Skenario | Hasil |
|----------|-------|
| Setup flow (3 permission) | ✅ Berhasil |
| FAB muncul + drag | ✅ Berhasil |
| Long-press → blur merah | ✅ Berhasil |
| Screen capture + OCR | ✅ Berhasil |
| Backend /check-scam | ⚠️ Butuh backend running |
| Result card muncul di atas blur | ✅ Berhasil |
| Dismiss → blur + result hilang | ✅ Berhasil |
| Re-scan (Android 14) → re-grant | ✅ Berhasil |
| Back navigation hierarchy | ✅ Berhasil |
| Crash loop setelah crash | ✅ Teratasi |

### 13.3 Crash Fixes Terverifikasi

- **lateinit ordering** — `registerForActivityResult()` sebelum intent checks
- **startForeground SecurityException** — guard dengan `!overlayCreated`
- **getMediaProjection SecurityException** — `foregroundServiceType` include `mediaProjection`
- **onStop() callback crash** — tidak memanggil `mediaProjection.stop()` lagi dari callback

---

## BAGIAN 14: DOKUMENTASI PENGGUNAAN

### 14.1 Instalasi

1. Build APK: `./gradlew assembleDebug`
2. Install ke perangkat Android via Android Studio atau `adb install`
3. Jalankan backend: `uvicorn anti_timpa.app:app --host 0.0.0.0 --port 8000`

### 14.2 Setup Awal (Pertama Kali)

```
1. Buka aplikasi → Setup screen muncul
2. Grant izin Notifikasi (Allow)
3. Grant izin Overlay (Setting > Display over other apps)
4. Grant Screen Recording (Start now → Share entire screen)
5. Tap "Mulai Overlay" → FAB muncul
```

### 14.3 Cara Scan

```
1. Buka aplikasi target (Facebook, WhatsApp, Instagram, browser, dll.)
2. Cari konten yang mencurigakan
3. Hold FAB ("F") selama 2 detik → perangkat bergetar
4. Layar menjadi merah dengan spinner → proses scanning
5. Hasil muncul:
   - ✅ Aman: konten aman
   - ⚠️ Mencurigakan: hati-hati
   - 🚨 Terindikasi Penipuan: konten berbahaya diblur
6. Tap "Dismiss" untuk menutup hasil
7. Tap "View Details" untuk melihat penjelasan lengkap
```

### 14.4 Fitur Lain

- **History:** Semua hasil scan tersimpan dan bisa dilihat kapan pun
- **Saved:** Tandai scan penting sebagai favorit
- **Settings:** Atur visibilitas FAB, ganti alamat server backend

---

## BAGIAN 15: DAFTAR PUSTAKA & REFERENSI

1. Google ML Kit — Text Recognition. https://developers.google.com/ml-kit/vision/text-recognition
2. Android Developers — MediaProjection. https://developer.android.com/media/projection
3. Android Developers — Foreground Services. https://developer.android.com/develop/background-work/services/foreground-services
4. FastAPI Documentation. https://fastapi.tiangolo.com/
5. Pydantic Documentation. https://docs.pydantic.dev/
6. NaraRouter API — OpenAI-compatible LLM API. (Lihat `backend/docs_nara.md`)
7. OkHttp Documentation. https://square.github.io/okhttp/
8. Room Database — Android Developers. https://developer.android.com/training/data-storage/room
9. DuckDuckGo Search API. https://duckduckgo.com/api
10. Kominfo — Data Penipuan Online Indonesia. https://www.kominfo.go.id/
11. BSSN — Laporan Keamanan Siber Indonesia. https://bssn.go.id/
12. Siberkreasi — Literasi Digital Indonesia. https://siberkreasi.id/
