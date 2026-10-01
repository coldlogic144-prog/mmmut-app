package com.mmmut.ero.data.repository

import com.google.firebase.firestore.FieldValue
import com.mmmut.ero.core.BackendConfig
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.model.RosterRecord
import com.mmmut.ero.data.remote.FirestoreCollections
import com.mmmut.ero.data.remote.MmmutApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RosterRepositoryImpl(private val apiBaseProvider: () -> String = { BackendConfig.API_BASE_URL }) : RosterRepository {
    private val db get() = FirebaseProvider.firestore

    override suspend fun getRoster(roll: String): RepoResult<RosterRecord?> = withContext(Dispatchers.IO) {
        val r = roll.trim()
        if (!AcademicData.ROLL_NUMBER_PATTERN.matches(r)) return@withContext RepoResult.Err("Enter a valid 10-digit roll number.")
        try {
            val snap = db.collection(FirestoreCollections.STUDENT_ROSTER).document(r).get().await()
            if (snap.exists()) return@withContext RepoResult.Ok(recordOf(snap.data ?: emptyMap()))
        } catch (_: Exception) { }
        val backend = restLookup(r)
        if (backend != null) return@withContext RepoResult.Ok(backend)
        RepoResult.Ok(null)
    }

    override suspend fun claimRoll(roll: String): RepoResult<RosterRecord> = withContext(Dispatchers.IO) {
        val r = roll.trim()
        val rec = when (val g = getRoster(r)) {
            is RepoResult.Ok -> g.value ?: return@withContext RepoResult.Err("Roll number not found in the admission roster.")
            is RepoResult.Err -> return@withContext g
        }
        val uid = FirebaseProvider.auth.currentUser?.uid ?: return@withContext RepoResult.Err("You are not logged in.")
        try {
            val existing = db.collection(FirestoreCollections.USER_ROLLS).document(r).get().await()
            if (existing.exists() && existing.getString("uid") != uid)
                return@withContext RepoResult.Err("This roll number is already linked to a different account.")
            if (existing.exists()) return@withContext RepoResult.Ok(rec)
        } catch (_: Exception) { return@withContext RepoResult.Err("Network error while checking the roll. Please try again.") }
        try {
            val username = try { db.collection(FirestoreCollections.USERS).document(uid).get().await().getString("username") ?: "" } catch (_: Exception) { "" }
            db.collection(FirestoreCollections.USER_ROLLS).document(r)
                .set(mapOf("uid" to uid, "username" to username, "rollNumber" to r, "verifiedAt" to FieldValue.serverTimestamp())).await()
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("PERMISSION", true))
                return@withContext RepoResult.Err("Firestore rules blocked the claim. Ask an admin to append the userRolls rules. Your account is unchanged.")
            return@withContext RepoResult.Err("Could not save the claim right now. Your account is unchanged — please try again.")
        }
        try {
            val branch = AcademicData.rosterBranchToId(rec.branchName)
            val updates = hashMapOf<String, Any>("name" to rec.formalName.ifBlank { rec.applicantName },
                "branchId" to branch, "migrationStatus" to "verified", "rollNumber" to r,
                "rollNumberVerified" to true, "pendingRollNumber" to "", "migrationReviewReason" to "")
            if (rec.section.isNotBlank()) updates["section"] = rec.section
            updates["rollClaimedAt"] = FieldValue.serverTimestamp()
            db.collection(FirestoreCollections.USERS).document(uid).update(updates).await()
        } catch (_: Exception) { }
        RepoResult.Ok(rec)
    }

    private fun recordOf(m: Map<String, Any?>) = RosterRecord(
        m["rollNumber"] as? String ?: "", m["enrollmentNo"] as? String ?: "",
        m["applicantName"] as? String ?: "", m["formalName"] as? String ?: "",
        m["branchName"] as? String ?: "", m["section"] as? String ?: "",
        m["batch"] as? String ?: "", (m["block"] as? Number)?.toInt() ?: 0,
        m["sourceFormNumber"] as? String ?: "")

    private suspend fun restLookup(roll: String): RosterRecord? {
        return try {
            val res = MmmutApiClient.service.getRoster(roll)
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null && body.ok && body.found && body.record != null) {
                    val dto = body.record
                    RosterRecord(
                        dto.rollNumber, dto.enrollmentNo, dto.applicantName,
                        dto.formalName, dto.branchName, dto.section,
                        dto.batch, dto.block, dto.sourceFormNumber
                    )
                } else null
            } else null
        } catch (_: Exception) { null }
    }
}
