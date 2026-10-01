package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.SyllabusData
import com.mmmut.ero.data.model.SubjectSyllabus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface SyllabusRepository {
    suspend fun getSyllabus(uid: String, branchId: String): RepoResult<List<SubjectSyllabus>>
    suspend fun toggleTopic(uid: String, branchId: String, topicKey: String, completed: Boolean): RepoResult<Unit>
}

class FirebaseSyllabusRepository : SyllabusRepository {
    private val db get() = FirebaseProvider.firestore

    override suspend fun getSyllabus(uid: String, branchId: String): RepoResult<List<SubjectSyllabus>> = withContext(Dispatchers.IO) {
        try {
            val branch = AcademicDataExtra.getBranch(branchId)
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(branch.id)
            val snap = try { docRef.get().await() } catch (_: Exception) { null }
            val data = snap?.data ?: emptyMap()
            val completedSet = data.filterValues { it == 1L || it == 1 || it == true }.keys

            val list = branch.subjects.map { sub ->
                SyllabusData.getSyllabusForSubject(branch.id, sub.code, sub.name, completedSet)
            }
            RepoResult.Ok(list)
        } catch (e: Exception) {
            RepoResult.Err("Could not load syllabus progress: ${e.message}")
        }
    }

    override suspend fun toggleTopic(uid: String, branchId: String, topicKey: String, completed: Boolean): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val branch = AcademicDataExtra.getBranch(branchId)
            val docRef = db.collection("users").document(uid).collection("ledgerProgress").document(branch.id)

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
}
