package com.mmmut.ero.data.repository

import com.google.firebase.firestore.FieldValue
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.auth.AuthEmail
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import com.mmmut.ero.core.BackendConfig

class FirebaseSignupHelper(private val apiBaseProvider: () -> String = { BackendConfig.API_BASE_URL }) {
    suspend fun signUp(username: String, password: String, name: String, branchId: String, section: String, hostel: String, gender: String, rollNumber: String): RepoResult<StudentProfile> = withContext(Dispatchers.IO) {
        try {
            val u = username.trim().lowercase()
            AuthEmail.validateUsername(u)?.let { return@withContext RepoResult.Err(it) }
            AuthEmail.validatePassword(password)?.let { return@withContext RepoResult.Err(it) }
            var finalName = name.trim(); var finalBranch = branchId; var finalSection = section
            var status = "verified"; var roll = ""
            if (rollNumber.isNotBlank()) {
                if (!AcademicData.ROLL_NUMBER_PATTERN.matches(rollNumber.trim()))
                    return@withContext RepoResult.Err("Roll number must be 10 digits.")
                when (val r = RosterRepositoryImpl(apiBaseProvider).getRoster(rollNumber.trim())) {
                    is RepoResult.Ok -> {
                        val rec = r.value ?: return@withContext RepoResult.Err("Roll number not found in the admission roster.")
                        finalName = rec.formalName.ifBlank { rec.applicantName }
                        finalBranch = AcademicData.rosterBranchToId(rec.branchName)
                        finalSection = rec.section.ifBlank { section }
                        roll = rec.rollNumber
                    }
                    is RepoResult.Err -> return@withContext RepoResult.Err(r.message)
                }
            } else {
                if (finalName.isBlank()) return@withContext RepoResult.Err("Fill in your name, username and password.")
                status = "pending"
            }
            val cred = FirebaseProvider.auth.createUserWithEmailAndPassword(AuthEmail.toEmail(u), password).await()
            val uid = cred.user?.uid ?: return@withContext RepoResult.Err("Authentication succeeded but no session was returned. Please try logging in.")
            val doc = hashMapOf<String, Any>("name" to finalName, "username" to u, "branchId" to finalBranch,
                "section" to finalSection, "hostel" to hostel, "gender" to gender, "isAdmin" to false,
                "adminRequested" to false, "migrationStatus" to status, "rollNumber" to roll,
                "rollNumberVerified" to (roll.isNotBlank()), "pendingRollNumber" to "",
                "migrationReviewReason" to "", "createdAt" to System.currentTimeMillis(), "lastReadPosts" to 0)
            try { FirebaseProvider.firestore.collection(FirestoreCollections.USERS).document(uid).set(doc).await() }
            catch (_: Exception) { return@withContext RepoResult.Err(AuthEmail.friendlyAuthError("permission-denied", "signup")) }
            if (roll.isNotBlank()) {
                try {
                    FirebaseProvider.firestore.collection(FirestoreCollections.USER_ROLLS).document(roll)
                        .set(mapOf("uid" to uid, "username" to u, "rollNumber" to roll, "verifiedAt" to FieldValue.serverTimestamp())).await()
                } catch (_: Exception) { }
            }
            RepoResult.Ok(FirebaseAuthRepository().profileOf(uid, u)
                ?: StudentProfile(uid, finalName, u, finalBranch, finalSection, hostel, gender))
        } catch (e: Exception) {
            RepoResult.Err(AuthEmail.friendlyAuthError((e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode?.let { "auth/$it" } ?: e.message, "signup"))
        }
    }
}
