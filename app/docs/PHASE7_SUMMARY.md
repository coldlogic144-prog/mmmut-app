# Phase 7 summary (AGENT 1 — coordinator)

- Completed work: full native app under `app/android` (10 screens, 7 viewmodels,
  8 repositories, FCM service + channels + prefs + history + deep links),
  docs (`ARCHITECTURE.md`, `TASKS.md`, `AGENT_RULES.md`, `BUILD.md`),
  Firebase docs (`SETUP.md`, `FIRESTORE_STRUCTURE.md`, `FCM.md`),
  unit tests (AuthEmail, roster pattern/map, schedule determinism, leave math,
  token id, deep links) + Compose smoke test + Node logic mirror.
- Files changed: ~55 new files under `app/`; zero modifications outside `app/`
  (web/ERP + backend + rules untouched).
- Tests performed: `node app/android/logic-mirror-check.cjs` → ALL CHECKS PASSED
  (mirrors the exact Kotlin formulas for email mapping, roll pattern, branch map,
  seeded RNG, leave math, token id, deep-link routing).
  Gradle build + JUnit not runnable here (no JDK/Android SDK in container) —
  must be run on a dev machine per `app/docs/BUILD.md`.
- Remaining work: place real `google-services.json`, run
  `gradlew :app:assembleDebug` / `testDebugUnitTest` / `bundleRelease`,
  register `com.mmmut.ero` in Firebase console, send a test FCM data message.
- Known problems: launcher icon is a system-drawable placeholder until an
  `ic_launcher` mipmap is added; examinations/results show empty states until
  admin-published data exists; API-base setting is UI-only in v1 (wired to
  `RosterRepositoryImpl` via default empty provider).
