# BlastScreen 3.0

BlastScreen 3.0 is a full-stack Android + backend baseline for:

- Screen recording (MediaProjection + foreground service)
- Random video matchmaking (backend API)
- Instant room handoff (Jitsi Meet URL)
- Modern Android architecture (Compose + ViewModel + Retrofit)

---

## 1) Tech stack (latest-oriented)

### Android app
- Kotlin `2.0.21`
- AGP `8.7.3`
- Jetpack Compose BOM `2025.01.00`
- Lifecycle/ViewModel + StateFlow
- Retrofit + Moshi for backend API
- minSdk `26`, targetSdk `35`

### Backend
- FastAPI + Uvicorn (Python 3.12)
- In-memory matchmaking queue by region
- Dockerized service

---

## 2) Monorepo structure

- `app/` Android application
- `backend/` FastAPI backend
- `docker-compose.yml` local backend runner

---

## 3) Run backend locally

### Option A: Docker (recommended)
```bash
docker compose up --build
```

Backend URL: `http://localhost:8080`

### Option B: Local Python
```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8080
```

Health check:
```bash
curl http://localhost:8080/health
```

---

## 4) Run Android app

1. Open project in Android Studio (latest stable).
2. Ensure SDK 35 is installed.
3. Start backend (above).
4. Run app on emulator/device.
   - Emulator default backend URL uses `10.0.2.2:8080`.

> Backend URL is set via `BuildConfig.BACKEND_URL` in `app/build.gradle.kts`.

---

## 5) Implemented features

### Screen recording
- Runtime permission request for `RECORD_AUDIO`
- MediaProjection flow through system capture intent
- Foreground service recording with notification
- MP4 save to `Movies/BlastScreen` through `MediaStore`

### Random matchmaking flow
- `GET /health` to verify backend availability
- `POST /match` to allocate/return room URL
- In-app match UI state: loading/success/error
- Open room in browser/Jitsi URL instantly

---

## 6) Production-hardening roadmap

- Replace in-memory queue with Redis/PostgreSQL persistence
- Add auth tokens + abuse prevention + rate limiting
- Add WebSocket signaling for richer real-time states
- Replace browser handoff with native WebRTC SDK
- Add analytics/crash reporting/privacy policy/legal docs
- Add CI (lint/test/build), unit tests, and instrumentation tests

---

## 7) Real platform constraints

BlastScreen follows official Android APIs.
Bypassing third-party `FLAG_SECURE` content is not supported in compliant consumer Android apps.

---

## 8) Reference sources

- MediaProjection: https://developer.android.com/media/grow/media-projection
- Foreground services: https://developer.android.com/develop/background-work/services/foreground-services
- MediaStore: https://developer.android.com/training/data-storage/shared/media
- Compose Material 3: https://developer.android.com/develop/ui/compose/designsystems/material3
- FastAPI docs: https://fastapi.tiangolo.com/
