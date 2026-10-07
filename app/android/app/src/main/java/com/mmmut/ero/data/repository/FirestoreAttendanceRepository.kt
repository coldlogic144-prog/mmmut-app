package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreAttendanceRepository : AttendanceRepository {
    private val db get() = FirebaseProvider.firestore

    companion object {
        private val mapCache = java.util.concurrent.ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        @Volatile
        private var cachedHolidays: Set<String>? = null
        @Volatile
        private var lastHolidaysFetchTime = 0L
        private const val HOLIDAYS_TTL_MS = 60 * 60 * 1000L // 1 hour

        fun clearCache() {
            mapCache.clear()
            cachedHolidays = null
            lastHolidaysFetchTime = 0L
        }
    }

    override suspend fun loadMap(uid: String): RepoResult<Map<String, Map<String, String>>> = withContext(Dispatchers.IO) {
        val cached = mapCache[uid]
        try {
            val snap = db.collection(FirestoreCollections.ATTENDANCE).document(uid).get().await()
            if (!snap.exists()) {
                mapCache[uid] = emptyMap()
                return@withContext RepoResult.Ok(emptyMap())
            }
            @Suppress("UNCHECKED_CAST")
            val result = (snap.get("attendance") as? Map<String, Map<String, String>>) ?: emptyMap()
            mapCache[uid] = result
            RepoResult.Ok(result)
        } catch (_: Exception) {
            if (cached != null) RepoResult.Ok(cached)
            else RepoResult.Err("Could not load attendance. Check your connection and retry.")
        }
    }

    override suspend fun saveMap(uid: String, map: Map<String, Map<String, String>>): RepoResult<Unit> = withContext(Dispatchers.IO) {
        mapCache[uid] = map
        try {
            val ref = db.collection(FirestoreCollections.ATTENDANCE).document(uid)
            try { ref.update("attendance", map).await() } catch (_: Exception) { ref.set(mapOf("attendance" to map)).await() }
            RepoResult.Ok(Unit)
        } catch (_: Exception) { RepoResult.Err("Could not save attendance. Check your connection and retry.") }
    }

    override suspend fun holidays(): RepoResult<Set<String>> = withContext(Dispatchers.IO) {
        val cached = cachedHolidays
        val now = System.currentTimeMillis()
        if (cached != null && (now - lastHolidaysFetchTime < HOLIDAYS_TTL_MS)) {
            return@withContext RepoResult.Ok(cached)
        }
        try {
            val snap = db.collection(FirestoreCollections.HOLIDAYS).get().await()
            val set = snap.documents.mapNotNull { it.getString("date") }.toSet()
            cachedHolidays = set
            lastHolidaysFetchTime = now
            RepoResult.Ok(set)
        } catch (_: Exception) {
            RepoResult.Ok(cached ?: emptySet())
        }
    }
}
