package com.mmmut.ero.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mmmut.ero.MainActivity
import com.mmmut.ero.data.model.StoredNotification
import com.mmmut.ero.data.repository.FirebaseNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MmmutFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val uid = com.mmmut.ero.core.FirebaseProvider.auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try { FirebaseNotificationRepository().registerToken(uid, token) } catch (_: Exception) { }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val data = msg.data
        val type = (data["type"] ?: data["category"] ?: "general").lowercase()
        val title = data["title"] ?: msg.notification?.title ?: "MMMUT"
        val body = data["body"] ?: msg.notification?.body ?: ""
        val refId = data["refId"] ?: data["ref_id"] ?: data["id"] ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationHistoryStore(applicationContext).append(
                    StoredNotification(System.currentTimeMillis().toString(), type, title, body, refId, System.currentTimeMillis()))
                val token = try { com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await() } catch (_: Exception) { null }
                val uid = com.mmmut.ero.core.FirebaseProvider.auth.currentUser?.uid
                if (uid != null && token != null) {
                    try { FirebaseNotificationRepository().registerToken(uid, token) } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
        }
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = NotificationPrefs(applicationContext)
            val enabled = try { prefs.isEnabled(type) } catch (_: Exception) { true }
            if (!enabled) return@launch
            showSystemNotification(type, title, body, refId)
        }
    }

    private fun showSystemNotification(type: String, title: String, body: String, refId: String) {
        val channel = NotificationChannels.channelFor(type)
        val targetRoute = NotificationDeepLink.routeFor(type, refId)
        val deepUri = when {
            targetRoute.startsWith("notice/") -> "mmmut://notice/${targetRoute.removePrefix("notice/")}"
            targetRoute == "home" -> "mmmut://home"
            else -> "mmmut://$targetRoute"
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse(deepUri)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pi = PendingIntent.getActivity(this, (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true).setContentIntent(pi).build()
        try { NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif) }
        catch (_: SecurityException) { }
    }
}
