# AntiTimpa API

Backend: `anti_timpa/app.py` — FastAPI  
Base URL: `http://<host>:8000`  
OpenAPI spec: `openapi.yaml` (auto-generated)

---

## `GET /health`

Cek status server.

**Response `200`**
```json
{
  "status": "ok",
  "llm": true,
  "model": "mimo-v2.5-hermes"
}
```

---

## `POST /check-scam`

Endpoint utama. Kirim teks hasil OCR, dapet verdict + blur flag.

**Request**
```json
{
  "text": "Dapatkan jackpot slot gacor maxwin! Transfer sekarang ke BCA 1234567890 bit.ly/x"
}
```

**Response `200`**

| Field | Type | Description |
|---|---|---|
| `verdict` | string | `"Terindikasi Penipuan"` / `"Mencurigakan"` / `"Aman"` |
| `risk_score` | int | 0–100 |
| `flagged_items` | array | Item-item mencurigakan yang ditemukan |
| `explanation` | string | Penjelasan dalam Bahasa Indonesia |
| `sources` | array | Referensi (OJK, dll) |
| `should_blur` | bool | **`true`** kalau konten harus di-blur |

**Contoh response (scam terdeteksi)**
```json
{
  "verdict": "Terindikasi Penipuan",
  "risk_score": 70,
  "flagged_items": [
    {"type": "account", "value": "1234567890", "reason": "Nomor rekening terdeteksi"},
    {"type": "url", "value": "bit.ly/x", "reason": "URL shortener — sulit dilacak"},
    {"type": "pattern", "value": "transfer sekarang", "reason": "Pola bahasa penipuan"}
  ],
  "explanation": "Mengandung kata kunci judi online",
  "sources": [],
  "should_blur": true
}
```

**Contoh response (aman)**
```json
{
  "verdict": "Aman",
  "risk_score": 0,
  "flagged_items": [],
  "explanation": "Tidak ditemukan indikasi penipuan yang jelas.",
  "sources": [],
  "should_blur": false
}
```

**Error `400`**
```json
{"error": "Text is empty", "code": "BAD_REQUEST"}
```

---

## `POST /verify`

Legacy FactLens endpoint — LLM-based fact checking. Butuh `NARA_ROUTER_API` di `.env`.

**Request**
```json
{
  "text": "Vaksin mRNA bisa mengubah DNA manusia",
  "language": "id"
}
```

**Response `200`**
```json
{
  "claim": "...",
  "verdict": "Contradicted",
  "confidence": 0.92,
  "explanation": "...",
  "sources": [...]
}
```

---

## `GET /verify`

Sama kaya POST, tapi lewat query param.

```
GET /verify?q=Vaksin+mRNA+bisa+mengubah+DNA+manusia
```

---

## Catatan untuk Android

1. **Semua field pake `snake_case`** (contoh: `risk_score`, `flagged_items`, `should_blur`)
2. **Minify request** — `text` maksimal 5000 karakter
3. **Error handling** — error selalu format `{"error": "...", "code": "..."}`
4. **Rate limit** — 30 request/menit per IP (409 Too Many Requests)
5. **CORS** — allow all origins (`*`)
6. **Dokumentasi interaktif** tersedia di `/docs` (Swagger) dan `/redoc`
