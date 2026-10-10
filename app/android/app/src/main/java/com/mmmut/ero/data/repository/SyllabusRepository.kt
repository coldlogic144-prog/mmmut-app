package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.local.SyllabusData
import com.mmmut.ero.data.model.SubjectSyllabus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface SyllabusRepository {
    suspend fun getSyllabus(uid: String, branchId: String): RepoResult<List<SubjectSyllabus>>
    suspend fun getSyllabusForView(uid: String, branchId: String, view: String): RepoResult<List<SubjectSyllabus>>
    suspend fun getCompletedKeys(uid: String, branchId: String): RepoResult<Set<String>>
    suspend fun toggleTopic(uid: String, branchId: String, topicKey: String, completed: Boolean): RepoResult<Unit>
    suspend fun toggleCourse(uid: String, branchId: String, topicKeys: List<String>, completed: Boolean): RepoResult<Unit>
    suspend fun resetBranchProgress(uid: String, branchId: String): RepoResult<Unit>
}

class FirebaseSyllabusRepository : SyllabusRepository {
    private val db get() = FirebaseProvider.firestore

    override suspend fun getCompletedKeys(uid: String, branchId: String): RepoResult<Set<String>> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext RepoResult.Ok(emptySet())
        try {
            val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(canonical)
            val snap = try { docRef.get().await() } catch (_: Exception) { null }
            val data = snap?.data ?: emptyMap()
            val completedSet = data.filterValues { it == 1L || it == 1 || it == true }.keys
            RepoResult.Ok(completedSet)
        } catch (_: Exception) {
            // Never break syllabus reading if Firestore network fails; return empty set
            RepoResult.Ok(emptySet())
        }
    }

    override suspend fun getSyllabus(uid: String, branchId: String): RepoResult<List<SubjectSyllabus>> =
        getSyllabusForView(uid, branchId, "1")

    override suspend fun getSyllabusForView(uid: String, branchId: String, view: String): RepoResult<List<SubjectSyllabus>> = withContext(Dispatchers.IO) {
        try {
            val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
            val completedSet = when (val res = getCompletedKeys(uid, canonical)) {
                is RepoResult.Ok -> res.value
                is RepoResult.Err -> emptySet()
            }

            val courses = SyllabusData.getCoursesForView(canonical, view)
            val list = if (courses.isNotEmpty()) {
                courses.map { c ->
                    SyllabusData.getSyllabusForCourse(canonical, c, completedSet)
                }
            } else {
                SyllabusData.getAllCoursesForBranch(canonical).map { c ->
                    SyllabusData.getSyllabusForCourse(canonical, c, completedSet)
                }
            }
            RepoResult.Ok(list)
        } catch (e: Exception) {
            RepoResult.Err("Could not load syllabus progress: ${e.message}")
        }
    }

    override suspend fun toggleTopic(uid: String, branchId: String, topicKey: String, completed: Boolean): RepoResult<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext RepoResult.Ok(Unit)
        try {
            val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(canonical)

            val updates = mapOf<String, Any>(
                topicKey to if (completed) 1 else 0,
                "updatedAt" to System.currentTimeMillis()
            )
            try {
                docRef.update(updates).await()
            } catch (_: Exception) {
                docRef.set(updates).await()
            }
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not save topic progress: ${e.message}")
        }
    }

    override suspend fun toggleCourse(uid: String, branchId: String, topicKeys: List<String>, completed: Boolean): RepoResult<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext RepoResult.Ok(Unit)
        try {
            val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(canonical)

            val updates = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )
            val v = if (completed) 1 else 0
            for (k in topicKeys) {
                updates[k] = v
            }
            try {
                docRef.update(updates).await()
            } catch (_: Exception) {
                docRef.set(updates).await()
            }
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not update course topics: ${e.message}")
        }
    }

    override suspend fun resetBranchProgress(uid: String, branchId: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext RepoResult.Ok(Unit)
        try {
            val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(canonical)
            docRef.delete().await()
            RepoResult.Ok(Unit)
        } catch (e: Exception) {
            RepoResult.Err("Could not reset branch syllabus progress: ${e.message}")
        }
    }
}
