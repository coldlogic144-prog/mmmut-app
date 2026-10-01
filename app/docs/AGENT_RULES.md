# Agent rules — MMMUT Android (`app/`)

1. Ownership (no blind overwrites):
   - AGENT 1: `app/docs/*`, `app/android/**/navigation/*`, `app/android/**/core/*`, repository interfaces.
   - AGENT 2: `app/android/**/ui/**` (theme/components/screens/viewmodel).
   - AGENT 3: `app/android/**/data/**`, `app/android/**/notifications/*`, `MmmutApp.kt`, `AndroidManifest.xml` FCM blocks, `app/firebase/*`.
   - AGENT 4: `src/test/**`, `src/androidTest/**`, build/security verification.
2. Before editing a shared file (Routes, Models, AcademicData, Manifest, gradle), read its current state first.
3. Never run `git reset --hard`, `git clean -fd`, or any destructive repo-wide op.
4. Never delete another agent's work to replace it; extend via interfaces.
5. UI layer must never import Firestore/Firebase directly — go through ViewModel → Repository.
6. No secrets in code: no service-account JSON, no private keys. Only public Firebase client config via `google-services.json` (developer-supplied, gitignored) + `google-services.json.example`.
7. Never weaken `firestore.rules` / `storage.rules`. Android must work within existing rules.
8. Never disable App Check to "make dev easier".
9. No fake permanent data on production screens — loading/empty/error/offline states required.
10. Every phase ends with: build check, tests, TASKS.md update, summary (done/files/tests/remaining/known issues).
11. Web app (`frontend/`, root `index.html`, `backend/`) is untouched except documented reuse. All Android work lives under `app/`.
