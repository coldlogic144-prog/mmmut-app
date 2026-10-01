package com.mmmut.ero.data.repository

import com.google.firebase.firestore.FieldValue
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.AdminRequest
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class TelegramApplicationWithUser(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val rollNumber: String = "",
    val status: String = "PENDING_ADMIN_APPROVAL",
    val appliedAt: Long = 0L,
    val telegramUsername: String? = null
)

interface AdminRepository {
    suspend fun getTelegramApplications(): RepoResult<List<TelegramApplicationWithUser>>
    suspend fun approveTelegramApplication(uid: String): RepoResult<Unit>
    suspend fun rejectTelegramApplication(uid: String): RepoResult<Unit>
    suspend fun getPendingRollVerifications(): RepoResult<List<StudentProfile>>
    suspend fun approveRollVerification(uid: String, rollNumber: String): RepoResult<Unit>
    suspend fun rejectRollVerification(uid: String, reason: String): RepoResult<Unit>
    suspend fun getAdminRequests(): RepoResult<List<AdminRequest>>
    suspend fun approveAdminRequest(requestId: String, targetUid: String): RepoResult<Unit>
    suspend fun createNotice(title: String, content: String, category: String, important: Boolean, pinned: Boolean): RepoResult<Unit>
    suspend fun deleteNotice(id: String): RepoResult<Unit>
}

class FirebaseAdminRepository : AdminRepository {
    private val db get() = FirebaseProvider.firestore

    override suspend fun getTelegramApplications(): RepoResult<List<TelegramApplicationWithUser>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("telegramApplications").get().await()
            val list = snap.documents.mapNotNull { d ->
                val uid = d.id
                val status = d.getString("status") ?: "PENDING_ADMIN_APPROVAL"
                val appliedAt = d.getLong("appliedAt") ?: 0L
                val tgUsername = d.getString("telegramUsername")
                val userSnap = try { db.collection(FirestoreCollections.USERS).document(uid).get().await() } catch (_: Exception) { null }
                val name = userSnap?.getString("name") ?: uid
                val username = userSnap?.getString("username") ?: uid
                val roll = userSnap?.getString("rollNumber") ?: ""
                TelegramApplicationWithUser(uid, name, username, roll, status, appliedAt, tgUsername)
            }.sortedByDescending { it.appliedAt }
            RepoResult.Ok(list)
        } catch (e: Exception) {
            RepoResult.Err("Could not load Telegram applications: ${e.message}")
        }
    }

    override suspend fun approveTelegramApplication(uid: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection("telegramApplications").document(uid).update(
                mapOf("status" to "ADMIN_APPROVED", "approvedAt" to System.currentTimeMillis())
            ).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not approve Telegram application: ${e.message}")
        }
    }

    override suspend fun rejectTelegramApplication(uid: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection("telegramApplications").document(uid).update(
                mapOf("status" to "ADMIN_REJECTED", "rejectedAt" to System.currentTimeMillis())
            ).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not reject Telegram application: ${e.message}")
        }
    }

    override suspend fun getPendingRollVerifications(): RepoResult<List<StudentProfile>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection(FirestoreCollections.USERS).get().await()
            val list = snap.documents.mapNotNull { d ->
                val p = StudentProfile(
                    uid = d.id,
                    name = d.getString("name") ?: "",
                    username = d.getString("username") ?: "",
                    branchId = d.getString("branchId") ?: "cse",
                    section = d.getString("section") ?: "A",
                    hostel = d.getString("hostel") ?: "Day Scholar",
                    gender = d.getString("gender") ?: "Not specified",
                    isAdmin = d.getBoolean("isAdmin") == true,
                    adminRequested = d.getBoolean("adminRequested") == true,
                    migrationStatus = d.getString("migrationStatus") ?: "verified",
                    rollNumber = d.getString("rollNumber") ?: "",
                    rollNumberVerified = d.getBoolean("rollNumberVerified") == true,
                    pendingRollNumber = d.getString("pendingRollNumber") ?: "",
                    migrationReviewReason = d.getString("migrationReviewReason") ?: "",
                    createdAt = d.getLong("createdAt") ?: 0L,
                    lastReadPosts = d.getDouble("lastReadPosts") ?: 0.0
                )
                if (!p.rollNumberVerified || p.migrationStatus == "pending" || p.pendingRollNumber.isNotBlank()) p else null
            }
            RepoResult.Ok(list)
        } catch (e: Exception) {
            RepoResult.Err("Could not load roll verification requests: ${e.message}")
        }
    }

    override suspend fun approveRollVerification(uid: String, rollNumber: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mapOf<String, Any>(
                "migrationStatus" to "verified",
                "rollNumberVerified" to true,
                "rollNumber" to rollNumber,
                "pendingRollNumber" to "",
                "migrationReviewReason" to ""
            )
            db.collection(FirestoreCollections.USERS).document(uid).update(updates).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not approve roll verification: ${e.message}")
        }
    }

    override suspend fun rejectRollVerification(uid: String, reason: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mapOf<String, Any>(
                "migrationStatus" to "rejected",
                "migrationReviewReason" to reason
            )
            db.collection(FirestoreCollections.USERS).document(uid).update(updates).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not reject roll verification: ${e.message}")
        }
    }

    override suspend fun getAdminRequests(): RepoResult<List<AdminRequest>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("adminRequests").get().await()
            val list = snap.documents.mapNotNull { d ->
                AdminRequest(
                    id = d.id,
                    uid = d.getString("uid") ?: "",
                    name = d.getString("name") ?: "",
                    username = d.getString("username") ?: "",
                    status = d.getString("status") ?: "pending",
                    requestedAt = d.getLong("requestedAt") ?: 0L
                )
            }.filter { it.status == "pending" }
            RepoResult.Ok(list)
        } catch (e: Exception) {
            RepoResult.Err("Could not load admin requests: ${e.message}")
        }
    }

    override suspend fun approveAdminRequest(requestId: String, targetUid: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection(FirestoreCollections.USERS).document(targetUid).update(
                mapOf("isAdmin" to true, "adminRequested" to false)
            ).await()
            db.collection("adminRequests").document(requestId).update(
                mapOf("status" to "approved", "approvedAt" to System.currentTimeMillis())
            ).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not approve admin request: ${e.message}")
        }
    }

    override suspend fun createNotice(title: String, content: String, category: String, important: Boolean, pinned: Boolean): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = mapOf(
                "title" to title.trim(),
                "content" to content.trim(),
                "category" to category.lowercase().trim(),
                "important" to important,
                "pinned" to pinned,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection(FirestoreCollections.POSTS).add(doc).await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not publish notice: ${e.message}")
        }
    }

    override suspend fun deleteNotice(id: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection(FirestoreCollections.POSTS).document(id).delete().await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not delete notice: ${e.message}")
        }
    }
}
