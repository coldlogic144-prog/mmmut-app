package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.auth.AuthEmail
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository : AuthRepository {
    private val auth get() = FirebaseProvider.auth
    private val db get() = FirebaseProvider.firestore

    override suspend fun currentProfile(): StudentProfile? = withContext(Dispatchers.IO) {
        val u = auth.currentUser ?: return@withContext null
        profileOf(u.uid, null)
    }

    override suspend fun signIn(usernameOrRoll: String, password: String, useRoll: Boolean): RepoResult<StudentProfile> = withContext(Dispatchers.IO) {
        try {
            val input = usernameOrRoll.trim()
            if (input.isEmpty()) return@withContext RepoResult.Err("Please enter your username or roll number.")
            if (password.isEmpty()) return@withContext RepoResult.Err("Please enter your password.")

            val isRollPattern = com.mmmut.ero.data.local.AcademicData.ROLL_NUMBER_PATTERN.matches(input)
            val username = if (useRoll || isRollPattern) {
                when (val r = resolveRollToUsername(input)) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> {
                        if (!useRoll && !isRollPattern) input.lowercase()
                        else return@withContext RepoResult.Err(r.message)
                    }
                }
            } else input.lowercase()

            AuthEmail.validateUsername(username)?.let { return@withContext RepoResult.Err(it) }

            auth.signInWithEmailAndPassword(AuthEmail.toEmail(username), password).await()
            val u = auth.currentUser ?: return@withContext RepoResult.Err("Sign-in succeeded but no session was returned.")
            val profile = profileOf(u.uid, username) ?: StudentProfile(uid = u.uid, name = username, username = username)
            RepoResult.Ok(profile)
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: e.message
            RepoResult.Err(AuthEmail.friendlyAuthError(code, "login"))
        }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid
            val token = try { FirebaseProvider.messaging.token.await() } catch (_: Exception) { null }
            if (uid != null && token != null) {
                try { FirebaseNotificationRepository().unregisterToken(uid, token) } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
        auth.signOut()
    }

    override suspend fun resolveRollToUsername(roll: String): RepoResult<String> = withContext(Dispatchers.IO) {
        val r = roll.trim()
        if (!com.mmmut.ero.data.local.AcademicData.ROLL_NUMBER_PATTERN.matches(r))
            return@withContext RepoResult.Err("Enter a valid 10-digit roll number.")
        try {
            val snap = db.collection(FirestoreCollections.USER_ROLLS).document(r).get().await()
            if (snap.exists()) {
                val username = snap.getString("username") ?: ""
                if (username.isNotBlank()) return@withContext RepoResult.Ok(username)
            }
            RepoResult.Err("This roll number is not linked to any account yet. Log in with your username instead.")
        } catch (_: Exception) { RepoResult.Err("Could not resolve roll number (network error). Try username login.") }
    }

    override suspend fun signUp(
        username: String,
        password: String,
        name: String,
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        hostel: String,
        roomNumber: String,
        gender: String,
        rollNumber: String
    ): RepoResult<StudentProfile> {
        return FirebaseSignupHelper(apiBaseProvider = { "" }).signUp(
            username, password, name, branchId, semester, section,
            tutorialGroup, practicalGroup, hostel, roomNumber, gender, rollNumber
        )
    }

    internal suspend fun profileOf(uid: String, fallbackUsername: String?): StudentProfile? {
        return try {
            val snap = db.collection(FirestoreCollections.USERS).document(uid).get().await()
            if (!snap.exists()) {
                if (fallbackUsername != null) {
                    val rec = hashMapOf<String, Any>(
                        "name" to fallbackUsername,
                        "username" to fallbackUsername,
                        "branchId" to "cse",
                        "semester" to 1,
                        "section" to "A",
                        "tutorialGroup" to "T1",
                        "practicalGroup" to "P1",
                        "hostel" to "Day Scholar",
                        "roomNumber" to "",
                        "gender" to "Not specified",
                        "isAdmin" to false,
                        "adminRequested" to false,
                        "migrationStatus" to "verified",
                        "rollNumber" to "",
                        "rollNumberVerified" to false,
                        "pendingRollNumber" to "",
                        "migrationReviewReason" to "",
                        "createdAt" to System.currentTimeMillis(),
                        "lastReadPosts" to 0
                    )
                    try { db.collection(FirestoreCollections.USERS).document(uid).set(rec).await() } catch (_: Exception) { }
                    return StudentProfile(uid, fallbackUsername, fallbackUsername)
                }
                return null
            }
            StudentProfile(
                uid = uid,
                name = snap.getString("name") ?: fallbackUsername ?: "",
                username = snap.getString("username") ?: fallbackUsername ?: "",
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
        } catch (_: Exception) {
            if (fallbackUsername != null) StudentProfile(uid, fallbackUsername, fallbackUsername) else null
        }
    }
}
