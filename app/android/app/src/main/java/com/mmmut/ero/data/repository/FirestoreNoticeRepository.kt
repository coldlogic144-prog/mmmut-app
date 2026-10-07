package com.mmmut.ero.data.repository

import com.google.firebase.Timestamp
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun noticeOf(id: String, m: Map<String, Any?>): Notice = Notice(id,
    m["title"] as? String ?: "", m["content"] as? String ?: m["body"] as? String ?: "",
    (m["category"] as? String ?: "general").lowercase(), m["pinned"] as? Boolean ?: false,
    m["important"] as? Boolean ?: false, m["linkUrl"] as? String ?: m["link"] as? String ?: "",
    ((m["createdAt"] as? Timestamp)?.toDate()?.time ?: (m["createdAt"] as? Number)?.toLong() ?: 0L))

class FirestoreNoticeRepository : NoticeRepository {
    private val db get() = FirebaseProvider.firestore

    companion object {
        @Volatile
        private var cachedNotices: List<Notice>? = null
        @Volatile
        private var lastFetchTime = 0L
        private const val TTL_MS = 3 * 60 * 1000L // 3 minutes

        fun clearCache() {
            cachedNotices = null
            lastFetchTime = 0L
        }

        fun updateCache(list: List<Notice>) {
            cachedNotices = list
            lastFetchTime = System.currentTimeMillis()
        }
    }

    override suspend fun notices(): RepoResult<List<Notice>> = withContext(Dispatchers.IO) {
        val cached = cachedNotices
        val now = System.currentTimeMillis()
        if (cached != null && (now - lastFetchTime < TTL_MS)) {
            return@withContext RepoResult.Ok(cached)
        }
        try {
            val snap = db.collection(FirestoreCollections.POSTS).get().await()
            val list = snap.documents.mapNotNull { d -> d.data?.let { noticeOf(d.id, it) } }
                .sortedWith(compareByDescending<Notice> { it.pinned }.thenByDescending { it.createdAtMillis })
            updateCache(list)
            RepoResult.Ok(list)
        } catch (_: Exception) {
            if (cached != null) RepoResult.Ok(cached)
            else RepoResult.Err("Could not load notices. Check your connection and retry.")
        }
    }

    override suspend fun notice(id: String): RepoResult<Notice> = withContext(Dispatchers.IO) {
        cachedNotices?.find { it.id == id }?.let { return@withContext RepoResult.Ok(it) }
        try {
            val d = db.collection(FirestoreCollections.POSTS).document(id).get().await()
            if (!d.exists()) return@withContext RepoResult.Err("Notice not found.")
            RepoResult.Ok(noticeOf(d.id, d.data ?: emptyMap()))
        } catch (_: Exception) { RepoResult.Err("Could not load notice. Check your connection and retry.") }
    }
}
