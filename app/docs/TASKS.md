# TASKS.md — MMMUT native Android app

Maintained by AGENT 1 (coordinator). Updated every phase.

## Phase 1 — Audit + architecture + agent setup [DONE]
- [x] Audit web/ERP (README, TECHNICAL_MAP, js/modules, backend, firestore.rules, storage.rules, FCM SW, roster store, branches/periods/schedule engine).
- [x] Define Android architecture (UI→VM→Repo→Firebase), module structure, Firebase reuse table.
- [x] Agent ownership + rules (`AGENT_RULES.md`).
- [x] Create `app/docs/`, `app/firebase/`, `app/android/` layout.

## Phase 2 — Project + theme + navigation + auth [DONE]
- [x] Gradle project (settings/app/gradle.properties, versions: AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00, Firebase BOM 33.1.2, nav 2.7.7).
- [x] Manifest (single activity, FCM service, POST_NOTIFICATIONS, deep links `mmmut://`).
- [x] Theme (Material3 light/dark, MMMUT colors), common loading/empty/error components.
- [x] Navigation (Splash/Auth/VerifyRoll/Home/Academics/Notices/Detail/Profile/Hostel/Notifications + bottom bar).
- [x] Auth (username/roll login, signup, session persistence, logout, error mapping) + VerifyRoll (optional claim).

## Phase 3 — Home/Profile/Timetable/Attendance/Academics [DONE]
- [x] Home (branding, greeting, profile summary, semester, today's timetable, attendance summary, notices, events, quick actions).
- [x] Profile (name/roll/branch/year/sem/hostel, settings incl. API base + logout).
- [x] Academics (subjects, timetable week/today, attendance marking + leave calculator, results, examinations, academic calendar).

## Phase 4 — Notices/Exams/Results/Calendar/Hostel [DONE]
- [x] Notices (list/detail/categories/search/filter/urgent/attachments).
- [x] Examinations + Results (Firestore-backed where present, graceful empty states otherwise).
- [x] Academic calendar (BUILTIN_EVENTS + holidays + eventOverrides).
- [x] Hostel (info + announcements filter).

## Phase 5 — FCM + prefs + history + deep links [DONE]
- [x] Service (`MmmutFirebaseMessagingService`), 6 channels, token registration/refresh (`android_<hash>`).
- [x] Android 13+ permission flow, foreground + background handling.
- [x] History (DataStore) + per-category prefs + deep linking to NoticeDetail/Exams/Events/Hostel (never just Home).

## Phase 6 — Testing + security + performance + offline [DONE]
- [x] Unit tests: AuthEmail, ScheduleEngine, AttendanceUtils, NotificationDeepLink, tokenDocId.
- [x] Compose smoke test: Auth screen.
- [x] Security review: no secrets, rules unchanged, client identity never trusted (UID from Auth only), Storage image guard documented.
- [x] Offline: Firestore persistence enabled, repository try/catch → UiState.Error with retry, empty states.

## Phase 7 — Polish + release verification [IN PROGRESS]
- [x] Polish: light/dark, typography, spacing, accessibility labels, subtle animations.
- [ ] Remaining: developer runs `google-services.json` placement + `./gradlew :app:assembleDebug` / `bundleRelease` (no Android SDK in this container, so build must be verified on a dev machine — see `app/firebase/SETUP.md`).
- Known issues: `google-services.json` is NOT committed (template only); FCM background icon uses system drawable until launcher icon is added; examinations/results collections depend on admin-published data and show empty states until then.

## File map (selection)
- Docs: `app/docs/ARCHITECTURE.md`, `TASKS.md`, `AGENT_RULES.md`
- Firebase: `app/firebase/SETUP.md`, `app/firebase/FIRESTORE_STRUCTURE.md`, `app/firebase/FCM.md`, `google-services.json.example`
- Android: `app/android/settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, manifests, Kotlin sources, tests.
