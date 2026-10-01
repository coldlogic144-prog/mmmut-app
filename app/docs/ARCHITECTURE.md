# MMMUT Student App — Architecture (native Android)

Owner: AGENT 1. Status: implemented (see TASKS.md).

## 1. Repository audit (original web/ERP)

Audited: README, docs/TECHNICAL_MAP, frontend/js/modules/*,
backend/*, firestore.rules, storage.rules, firebase-messaging-sw.js,
.env.example, roster_store.py shape.

| Concern | Original | Android reuse |
|---|---|---|
| Firebase project | student-erp-77605 (public web keys inline; senderId 734576815247) | Same project; Android appId separate (console). Only public config via google-services.json (gitignored). |
| Auth | Firebase Auth email/pw, mapped email username@mmmut.local; username ^[a-z0-9._-]+$, min 3; pw min 6; friendlyAuthError mapping | Ported 1:1 in data/auth/AuthEmail.kt. Session = FirebaseAuth currentUser. |
| Roll verification | studentRoster/{roll} + userRolls/{roll} create-only (docID=roll); 10-digit pattern; ROSTER_BRANCH_TO_ID CED->civil CSD->cse EED->ee ECD->ece IOT->eceiot MED->me CHD->chemical ITC->it. Gate currently DISABLED on web. Backend fallback GET /api/roster/<roll> | Optional VerifyRoll screen, never a hard block. Same pattern/mapping/claim. |
| Firestore | users, attendance, posts, holidays, eventOverrides, timetableOverrides, studentRoster, userRolls, users/{uid}/notificationTokens, + web-only (community/feedback/chess/ledger) | Android reads student-relevant subset only. Rules unchanged. |
| Timetable | PERIODS I-IV+LUNCH+V-VIII; DAYS Mon-Fri; 10 BRANCHES; PDF_TIMETABLES; buildSchedule() seeded RNG | Ported to data/local/AcademicData.kt + ScheduleEngine.kt. |
| Attendance | attendance/{uid} {date:{period::code:status}} + holidays guard + computeLeaveInfo | Ported to AttendanceRepository + AttendanceUtils. |
| Push (web) | FCM web-push, VAPID inline, token doc users/{uid}/notificationTokens/web_<hash> | Native port: android_<hash>, 6 channels, history+prefs+deep links. |
| Storage | communityPosts/{uid}/{file}, feedback/{uid}/{file}, image<5MB | v1 read-only; no uploads. Rules preserved. |
| Backend | Flask /api/health, /api/roster/<roll> | Android uses roster lookup only as fallback (base URL in Settings; empty=disabled). |

## 2. Module structure (app/android/)

Single-module :app, minSdk 26, compile/target 34.
Kotlin 1.9.24 + Compose BOM 2024.06.00 + Firebase BOM 33.1.2 + nav 2.7.7.

- core/: FirebaseProvider (persistence on), UiState, RepoResult
- navigation/: Routes (typed + notice/{id}), AppNav (NavHost + bottom bar + deep links)
- data/model/Models.kt (mirrors Firestore shapes)
- data/local/AcademicData.kt + AcademicDataExtra.kt (10 branches, periods, events)
- data/local/ScheduleEngine.kt (seeded buildSchedule port + tokenDocId)
- data/auth/AuthEmail.kt (mapping + validation + friendly errors)
- data/remote/FirestoreCollections.kt (path constants)
- data/repository/ (interfaces + Firestore impls; Compose never queries Firestore)
- ui/theme (M3 light/dark, MMMUT indigo/maroon/gold)
- ui/components/CommonUi.kt (Loading/Empty/Error/SectionHeader/NoticeCard)
- ui/screens (10) + ui/viewmodel (Auth/Home/Notices/Profile/Academics/Hostel/Notifications/VerifyRoll)
- notifications/ (6 channels, FCM service, DataStore prefs + history, deep links)
- util/ (TimeUtils greeting/today/dateKey, AttendanceUtils computeLeaveInfo port)

## 3. Firebase architecture

Auth -> AuthRepository. Firestore -> repositories only.
Notices = posts ordered pinned/createdAt + client filter/search.
Attendance = attendance/{uid} merge-writes + holidays guard.
Roster = studentRoster read -> REST fallback; claim = userRolls/{roll} create-only.
Tokens = users/{uid}/notificationTokens/android_<hash>.
Storage not written by v1. No secrets; rules unchanged; App Check not disabled.

## 4. Auth flow

Splash (currentUser?) -> Auth (Login/Signup + Username/Roll toggle; roll resolves
userRolls/{roll}->username then normal sign-in) -> load users/{uid} -> Home.
VerifyRoll optional (never blocks). Logout clears token doc + signOut.

## 5. Notification flow

FCM data {type, refId, title, body} -> service -> per-type channel +
deep-link PendingIntent (mmmut://notice/{refId} etc.) -> tap routes to
NoticeDetail/Academics/Hostel/Notices (never just Home) -> also stored in
DataStore history. Prefs gate system notifications (history still recorded).
POST_NOTIFICATIONS requested on API 33+.

## 6. Navigation

Splash Auth VerifyRoll Home Academics Notices NoticeDetail(id) Profile Hostel
Notifications. Bottom bar: Home Academics Notices Hostel Profile.
Deep links: mmmut://notice/{id}, mmmut://exam, mmmut://event/{id}, mmmut://hostel.

## 7. Agent ownership

AGENT 1: docs, navigation, core, repo interfaces.
AGENT 2: ui/**. AGENT 3: data/**, notifications/*, MmmutApp, manifest FCM, firebase docs.
AGENT 4: tests + verification. Shared files read-first, contract-append-only.

