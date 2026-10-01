# FCM — Android implementation notes

- Service: `com.mmmut.ero.notifications.MmmutFirebaseMessagingService`
  (`onNewToken` → `NotificationRepository.registerToken`; `onMessageReceived` → channel + history + deep-link PendingIntent).
- Channels (`NotificationChannels.kt`): general, academic, examination, hostel, events, emergency (emergency = high importance).
- Token doc ID: `android_<base36 hash>` (mirrors web `web_<hash>` so re-logins don't duplicate).
- Payload: prefer **data-only** (`type, refId, title, body`) so foreground + background both deep-link. Notification-payload messages still handled (title/body fallback).
- History: `NotificationHistoryStore` (DataStore `mmmut_notifications`) capped at 100, shown in Notifications screen.
- Prefs: `NotificationPrefs` (DataStore `mmmut_notif_prefs`) per-category booleans, default all-on.
- Permission: `POST_NOTIFICATIONS` (API 33+) requested via Accompanist-free ActivityResult launcher in Notifications/Home screens.
- Test: send data message from FCM console with `type=academic&refId=<posts doc id>`; tap must land on NoticeDetail, not Home.
