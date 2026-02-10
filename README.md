# BlastScreen (Android)

BlastScreen is a modern Android app starter focused on:

- Screen recording via `MediaProjection`
- Foreground-service-compliant capture flow (Android 14/15 friendly)
- One-tap random video-call handoff using Jitsi Meet rooms
- Jetpack Compose + Material 3 UI

## What this project does now

✅ Works as a real Android app project you can open in Android Studio and run.

✅ Lets users start/stop screen recording with microphone audio.

✅ Saves recordings to `Movies/BlastScreen` as `.mp4` using `MediaStore`.

✅ Launches a random Jitsi room URL for instant video-call matching.

## Important platform reality notes

- Android **does not legally/safely allow bypassing** other apps' `FLAG_SECURE` protections for normal Play-compliant apps.
- This project intentionally follows official Android APIs and policies.

## Stack

- Kotlin 2.0.21
- Android Gradle Plugin 8.7.3
- Jetpack Compose (BOM 2025.01.00)
- Material 3
- Min SDK 26, Target SDK 35

## Build

1. Open in latest Android Studio.
2. Install Android SDK 35 and matching build tools.
3. Sync Gradle.
4. Run `app` on a real device (recommended for MediaProjection tests).

## Research-backed references

- MediaProjection overview: https://developer.android.com/media/grow/media-projection
- Foreground services guidance: https://developer.android.com/develop/background-work/services/foreground-services
- MediaStore scoped storage: https://developer.android.com/training/data-storage/shared/media
- Material 3 for Compose: https://developer.android.com/develop/ui/compose/designsystems/material3
- Jitsi Meet docs: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe

## Project structure

- `app/src/main/java/com/blastscreen/app/MainActivity.kt` — Compose UI + permission/launcher flows
- `app/src/main/java/com/blastscreen/app/recording/ScreenRecordService.kt` — foreground recording service
- `app/src/main/java/com/blastscreen/app/ui/Theme.kt` — app theme

## Next recommended upgrades

- Add in-app player/gallery for saved recordings.
- Add WebRTC-native client (instead of browser handoff) for full call UX control.
- Add end-to-end encryption strategy docs and privacy policy pages.
- Add UI tests and baseline profiles for startup performance.
