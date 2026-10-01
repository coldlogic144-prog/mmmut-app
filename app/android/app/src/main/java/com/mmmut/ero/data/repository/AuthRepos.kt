package com.mmmut.ero.data.repository

import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.RosterRecord
import com.mmmut.ero.data.model.StudentProfile

interface AuthRepository {
    suspend fun currentProfile(): StudentProfile?
    suspend fun signIn(usernameOrRoll: String, password: String, useRoll: Boolean): RepoResult<StudentProfile>
    suspend fun signUp(username: String, password: String, name: String, branchId: String, section: String, hostel: String, gender: String, rollNumber: String): RepoResult<StudentProfile>
    suspend fun signOut()
    suspend fun resolveRollToUsername(roll: String): RepoResult<String>
}

interface RosterRepository {
    suspend fun getRoster(roll: String): RepoResult<RosterRecord?>
    suspend fun claimRoll(roll: String): RepoResult<RosterRecord>
}

interface ProfileRepository {
    suspend fun getProfile(uid: String): RepoResult<StudentProfile>
    suspend fun updateProfile(uid: String, fields: Map<String, Any?>): RepoResult<Unit>
}
