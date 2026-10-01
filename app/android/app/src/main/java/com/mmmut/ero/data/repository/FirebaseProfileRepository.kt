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
            RepoResult.Ok(
                StudentProfile(
                    uid = uid,
                    name = snap.getString("name") ?: "",
                    username = snap.getString("username") ?: "",
                    rollNumber = snap.getString("rollNumber") ?: "",
                    branchId = snap.getString("branchId") ?: "cse",
                    semester = (snap.getLong("semester") ?: 1L).toInt(),
                    section = snap.getString("section") ?: "A",
                    tutorialGroup = snap.getString("tutorialGroup") ?: "T1",
                    practicalGroup = snap.getString("practicalGroup") ?: "P1",
                    hostel = snap.getString("hostel") ?: "Day Scholar",
                    roomNumber = snap.getString("roomNumber") ?: "",
                    gender = snap.getString("gender") ?: "Not specified",
                    isAdmin = snap.getBoolean("isAdmin") == true,
                    adminRequested = snap.getBoolean("adminRequested") == true,
                    migrationStatus = snap.getString("migrationStatus") ?: "verified",
                    rollNumberVerified = snap.getBoolean("rollNumberVerified") == true,
                    pendingRollNumber = snap.getString("pendingRollNumber") ?: "",
                    migrationReviewReason = snap.getString("migrationReviewReason") ?: "",
                    createdAt = snap.getLong("createdAt") ?: 0L,
                    lastReadPosts = snap.getDouble("lastReadPosts") ?: 0.0
                )
            )
        } catch (_: Exception) {
            RepoResult.Err("Could not load profile. Check your connection and retry.")
        }
    }

    override suspend fun updateProfile(uid: String, fields: Map<String, Any?>): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val allowed = setOf(
                "name", "branchId", "semester", "section", "tutorialGroup", "practicalGroup",
                "hostel", "roomNumber", "gender", "adminRequested", "pendingRollNumber", "lastReadPosts", "updatedAt"
            )
            val safe = mutableMapOf<String, Any>()
            fields.forEach { (k, v) -> if (k in allowed && v != null) safe[k] = v }
            db.collection(FirestoreCollections.USERS).document(uid).update(safe).await()
            RepoResult.Ok(Unit)
        } catch (_: Exception) {
            RepoResult.Err("Could not save profile. Check your connection and retry.")
        }
    }
}
