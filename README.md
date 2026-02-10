# BlastScreen 3.0

Production-oriented structure for random video calling (Android + Node backend), with Firebase, admin-scoped hidden controls, Play Billing foundations, and modular architecture.

## Architecture
- Android modules: `app`, `core/designsystem`, `core/network`, `core/webrtc`
- Backend: Node.js + TypeScript (`backend/`) for auth, queue, reconnect cooldown, usage, TURN/STUN config, WS signaling stub
- Infra: Docker + GitHub Actions CI

## Admin system
- Only server-provisioned admin identities are allowed: `milkyplump`, `monster`.
- Provision endpoint requires: phone=`0`, password=`deveg`, and valid username.
- Admin JWT stores in encrypted local storage.
- Recording service checks secure admin token before starting.

## Firebase
- Analytics, Crashlytics, Performance, and Remote Config dependencies/plugins integrated.
- `app/google-services.json` package name is `com.blastscreen` (replace placeholder values with real Firebase project values).

## Backend quick start
```bash
cd backend
cp .env.example .env
npm install
npm run dev
```

## Android quick start
1. Open project in latest Android Studio.
2. Sync Gradle.
3. Ensure `google-services.json` uses your real Firebase project and package `com.blastscreen`.
4. Run backend on `http://10.0.2.2:8080` for emulator.

## Compliance note
This repo does not provide policy-violating instructions. Any privileged/admin-only capabilities must be validated server-side, audited, and compliant with platform law/policy.
