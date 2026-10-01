package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreAttendanceRepository : AttendanceRepository {
    private val db get() = FirebaseProvider.firestore
    override suspend fun loadMap(uid: String): RepoResult<Map<String, Map<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection(FirestoreCollections.ATTENDANCE).document(uid).get().await()
            if (!snap.exists()) return@withContext RepoResult.Ok(emptyMap())
            @Suppress("UNCHECKED_CAST")
            RepoResult.Ok((snap.get("attendance") as? Map<String, Map<String, String>>) ?: emptyMap())
        } catch (_: Exception) { RepoResult.Err("Could not load attendance. Check your connection and retry.") }
    }
    override suspend fun saveMap(uid: String, map: Map<String, Map<String, String>>): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val ref = db.collection(FirestoreCollections.ATTENDANCE).document(uid)
            try { ref.update("attendance", map).await() } catch (_: Exception) { ref.set(mapOf("attendance" to map)).await() }
            RepoResult.Ok(Unit)
        } catch (_: Exception) { RepoResult.Err("Could not save attendance. Check your connection and retry.") }
    }
    override suspend fun holidays(): RepoResult<Set<String>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection(FirestoreCollections.HOLIDAYS).get().await()
            RepoResult.Ok(snap.documents.mapNotNull { it.getString("date") }.toSet())
        } catch (_: Exception) { RepoResult.Ok(emptySet()) }
    }
}
