package com.mmmut.ero.data.repository

import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.model.CalendarEvent
import com.mmmut.ero.data.model.ExamInfo
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.ResultEntry
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.remote.FirestoreCollections
import com.mmmut.ero.core.FirebaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocalTimetableRepository : TimetableRepository {
    override suspend fun weekCells(
        branchId: String,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): Map<String, List<TimetableCell>> {
        val branch = AcademicDataExtra.getBranch(branchId)
        val sec = section.ifBlank { branch.sections.firstOrNull() ?: "A" }

        val map = mutableMapOf<String, List<TimetableCell>>()
        AcademicData.DAYS.forEach { day ->
            val dayRawCells = ScheduleEngine.dayCells(branch, sec, day)
            val filtered = ScheduleEngine.filterCellsForStudent(dayRawCells, tutorialGroup, practicalGroup)
            map[day] = filtered
        }
        return map
    }

    override suspend fun todayCells(
        branchId: String,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): List<TimetableCell> {
        val day = com.mmmut.ero.util.TimeUtils.todayName() ?: "Monday"
        return weekCells(branchId, section, tutorialGroup, practicalGroup)[day] ?: emptyList()
    }
}

class FirestoreCalendarRepository : CalendarRepository {
    private val db get() = FirebaseProvider.firestore
    override suspend fun events(): RepoResult<List<CalendarEvent>> = withContext(Dispatchers.IO) {
        val out = AcademicDataExtra.BUILTIN_EVENTS.mapIndexed { i, e ->
            CalendarEvent("builtin-$i", e.title, e.start, e.end, "builtin")
        }.toMutableList()
        try {
            val snap = db.collection(FirestoreCollections.EVENT_OVERRIDES).get().await()
            snap.documents.forEach { d ->
                out.add(CalendarEvent(d.id, d.getString("title") ?: "", d.getString("start") ?: "",
                    d.getString("end") ?: d.getString("start") ?: "", "override"))
            }
        } catch (_: Exception) { }
        try {
            val snap = db.collection(FirestoreCollections.HOLIDAYS).get().await()
            snap.documents.forEach { d ->
                val date = d.getString("date") ?: return@forEach
                out.add(CalendarEvent(d.id, d.getString("title") ?: "Holiday", date, date, "holiday"))
            }
        } catch (_: Exception) { }
        RepoResult.Ok(out.sortedBy { it.start })
    }
    override suspend fun exams(): RepoResult<List<ExamInfo>> = withContext(Dispatchers.IO) {
        val builtin = listOf(
            ExamInfo("mid-sem", "Mid Semester Examinations", "As per academic calendar", "2026-09-21", "examination"),
            ExamInfo("end-practical", "End Semester Examinations (Practical)", "As per academic calendar", "2026-11-23", "examination"),
            ExamInfo("end-theory", "End Semester Examinations (Theory)", "As per academic calendar", "2026-12-01", "examination"))
        try {
            val snap = db.collection(FirestoreCollections.POSTS).get().await()
            val list = snap.documents.mapNotNull { d ->
                val cat = (d.getString("category") ?: "").lowercase()
                if (cat != "exam" && cat != "examination") return@mapNotNull null
                ExamInfo(d.id, d.getString("title") ?: "", d.getString("content") ?: "", "", cat)
            }
            RepoResult.Ok(builtin + list)
        } catch (_: Exception) { RepoResult.Ok(builtin) }
    }
    override suspend fun results(uid: String): RepoResult<List<ResultEntry>> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("results").document(uid).get().await()
            if (!snap.exists()) return@withContext RepoResult.Ok(emptyList())
            @Suppress("UNCHECKED_CAST")
            val items = snap.get("items") as? List<Map<String, Any?>> ?: return@withContext RepoResult.Ok(emptyList())
            RepoResult.Ok(items.map { m -> ResultEntry(m["subjectCode"] as? String ?: "",
                m["subjectName"] as? String ?: "", m["grade"] as? String ?: "",
                (m["credits"] as? Number)?.toDouble() ?: 0.0) })
        } catch (_: Exception) { RepoResult.Ok(emptyList()) }
    }
}

class FirestoreHostelRepository : HostelRepository {
    override suspend fun announcements(): RepoResult<List<Notice>> = withContext(Dispatchers.IO) {
        when (val r = FirestoreNoticeRepository().notices()) {
            is RepoResult.Ok -> RepoResult.Ok(r.value.filter { it.category == "hostel" })
            is RepoResult.Err -> r
        }
    }
}
