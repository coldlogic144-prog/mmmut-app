# MMMUT native Android app — build & release

No JDK / Android SDK in this container, so the Gradle build must run on a dev machine.
Steps verified by inspection (file walk + logic mirror test):

1. Install Android Studio (or JDK 17 + Android SDK cmdline-tools, platform-34, build-tools 34).
2. Copy Firebase config:
   `cp google-services.json app/android/app/google-services.json`
   (template: `app/android/google-services.json.example`; real file is gitignored).
3. Build:
   `cd app/android && ./gradlew :app:assembleDebug` (use `gradlew.bat` on Windows)
   Tests: `./gradlew :app:testDebugUnitTest`
   Release: `./gradlew :app:bundleRelease` (APK via `:app:assembleRelease`).
4. Logic mirror (runs without Android SDK):
   `node app/android/logic-mirror-check.cjs` → must print ALL CHECKS PASSED.
   (Already passing in this repo; see Phase 7 summary.)

Manual review checklist (AGENT 4) — done in-repo:
- Manifest: single activity, FCM service, POST_NOTIFICATIONS, mmmut:// scheme.
- No service-account keys / private keys committed (only .example template).
- Firestore/Storage rules untouched; app uses only allowed paths.
- Compose never touches Firebase directly (VM → Repository).
- UiState Loading/Empty/Error on every remote screen; offline = cached Firestore + error+retry.
