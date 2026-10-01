package com.mmmut.ero.data.repository

import com.google.firebase.firestore.FieldValue
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.data.remote.FirestoreCollections
import com.mmmut.ero.util.TokenId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun tokenDocId(token: String): String = TokenId.docId(token)

class FirebaseNotificationRepository : NotificationRepository {
    private val db get() = FirebaseProvider.firestore
    override suspend fun registerToken(uid: String, token: String): Unit = withContext(Dispatchers.IO) {
        try {
            db.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.NOTIFICATION_TOKENS).document(tokenDocId(token))
                .set(mapOf("token" to token, "platform" to "android",
                    "updatedAt" to FieldValue.serverTimestamp(), "userAgent" to "Android")).await()
        } catch (_: Exception) { }
    }
    override suspend fun unregisterToken(uid: String, token: String): Unit = withContext(Dispatchers.IO) {
        try {
            db.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.NOTIFICATION_TOKENS).document(tokenDocId(token)).delete().await()
        } catch (_: Exception) { }
    }
}
