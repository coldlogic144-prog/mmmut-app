# Firestore structure reused by Android (read-only reference)

Authoritative rules: root `firestore.rules` + `firestore_rules_append.txt` (already applied in console).
Android works strictly inside these rules — no changes made.

```
users/{uid} {
  name, username, branchId, section, hostel, gender,
  isAdmin: bool, adminRequested: bool,
  migrationStatus: pending|verified|rejected|manual_review,
  rollNumber, rollNumberVerified: bool, pendingRollNumber,
  migrationReviewReason, rollClaimedAt, createdAt, lastReadPosts
}
users/{uid}/notificationTokens/{id} {
  token, platform: web|android, updatedAt, userAgent, appVersion?
}
attendance/{uid} { attendance: { "2026-09-30": { "I::BSM-110": "present|absent|holiday" } } }
posts/{id} { title, content, category, pinned: bool, createdAt: ts, linkUrl?, important: bool, uid? }
  → surfaced as Notices (category filter incl. hostel/exam/event).
holidays/{id} { date: "YYYY-MM-DD", title? }
eventOverrides/{id} { start, end, title }
timetableOverrides/{id} { branchId, section, day, periodKey, code, name, type }
studentRoster/{roll} { rollNumber, enrollmentNo, applicantName, formalName, branchName, section, batch, block, sourceFormNumber }
userRolls/{roll} { uid, username, rollNumber, verifiedAt }  (create-only; doc ID = roll)
```

Web-only collections (not read by Android v1): communityPosts, feedback, ratings,
adminRequests, chessClubMembers, chessChallenges, chessEvents, chessActivity,
chessGames, ledgerProgress. They remain untouched.
