# Firebase setup — MMMUT Android app

Firebase project (shared with web): **`student-erp-77605`**.

## 1. Create the Android app record
1. Firebase Console → Project `student-erp-77605` → Add app → Android.
2. Package name: `com.mmmut.ero` (must match `app/android/app/build.gradle.kts` `applicationId`).
3. Download `google-services.json` → place at `app/android/app/google-services.json` (gitignored; never commit the real file).
4. Template: `app/android/google-services.json.example`.

## 2. Enable products
- Authentication → Sign-in method → Email/Password → Enable (usernames map to `username@mmmut.local`; no code change needed).
- Firestore → existing database reused. Rules in root `firestore.rules` are authoritative — do NOT weaken them for Android.
- Cloud Messaging → enabled by default. No VAPID needed natively (web VAPID stays web-only).
- Storage → existing bucket reused; v1 Android does not upload.

## 3. Android 13+ notifications
Runtime `POST_NOTIFICATIONS` is requested in-app (Notifications screen + Home bell). Channels are created on app start (`MmmutApp`).

## 4. Sending push (data payload contract)
Send via Console → Cloud Messaging or Admin SDK with **data** (so taps deep-link correctly):

```json
{
  "to": "<fcm token>",
  "data": {
    "type": "academic",
    "refId": "<notice-or-exam-doc-id>",
    "title": "Mid-sem datesheet published",
    "body": "CSE Sem 3 · check Examinations"
  }
}
```

`type` ∈ `general|academic|examination|hostel|events|emergency`. Tap routes:
- general/academic/events + refId → NoticeDetail(refId)
- examination → Examinations (Exams tab)
- hostel → Hostel
- emergency → NoticeDetail if refId else Notices.

## 5. Backend fallback (optional)
Settings → API base URL (e.g. `https://mmmut-ero-backend.onrender.com`). Empty = disabled (default, matches web `mmmut_api_base` unset behavior). Used only for `GET /api/roster/<roll>` when Firestore roster read fails.

## 6. SHA-1 (only if using Google-sign-in / App Check in future)
Not required for v1 (email/password only). Add debug/release SHA-1 later if enabling Play Integrity / App Check enforcement.
