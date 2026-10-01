package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseProfileRepository : ProfileRepository {
    private val db get() = FirebaseProvider.firestore
    override suspend fun getProfile(uid: String): RepoResult<StudentProfile> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection(FirestoreCollections.USERS).document(uid).get().await()
            if (!snap.exists()) return@withContext RepoResult.Err("Profile not found.")
            RepoResult.Ok(StudentProfile(uid, snap.getString("name") ?: "", snap.getString("username") ?: "",
                snap.getString("branchId") ?: "cse", snap.getString("section") ?: "A",
                snap.getString("hostel") ?: "Day Scholar", snap.getString("gender") ?: "Not specified",
                snap.getBoolean("isAdmin") == true, snap.getBoolean("adminRequested") == true,
                snap.getString("migrationStatus") ?: "verified", snap.getString("rollNumber") ?: "",
                snap.getBoolean("rollNumberVerified") == true, snap.getString("pendingRollNumber") ?: "",
                snap.getString("migrationReviewReason") ?: "", (snap.getLong("createdAt") ?: 0L),
                (snap.getDouble("lastReadPosts") ?: 0.0)))
        } catch (_: Exception) { RepoResult.Err("Could not load profile. Check your connection and retry.") }
    }
    override suspend fun updateProfile(uid: String, fields: Map<String, Any?>): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val allowed = setOf("name", "branchId", "section", "hostel", "gender", "adminRequested", "pendingRollNumber", "lastReadPosts", "updatedAt")
            val safe = mutableMapOf<String, Any>()
            fields.forEach { (k, v) -> if (k in allowed && v != null) safe[k] = v }
            db.collection(FirestoreCollections.USERS).document(uid).update(safe).await()
            RepoResult.Ok(Unit)
        } catch (_: Exception) { RepoResult.Err("Could not save profile. Check your connection and retry.") }
    }
}
