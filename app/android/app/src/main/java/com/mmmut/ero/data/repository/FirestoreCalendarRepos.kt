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
        practicalGroup: String,
        semester: Int
    ): Map<String, List<TimetableCell>> {
        return ScheduleEngine.getWeek(
            branchId = branchId,
            semester = semester,
            section = section,
            tutorialGroup = tutorialGroup,
            practicalGroup = practicalGroup
        )
    }

    override suspend fun todayCells(
        branchId: String,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        semester: Int
    ): List<TimetableCell> {
        return ScheduleEngine.getToday(
            branchId = branchId,
            semester = semester,
            section = section,
            tutorialGroup = tutorialGroup,
            practicalGroup = practicalGroup
        )
    }
}

class FirestoreCalendarRepository : CalendarRepository {
    private val db get() = FirebaseProvider.firestore

    companion object {
        @Volatile
        private var cachedEvents: List<CalendarEvent>? = null
        @Volatile
        private var lastEventsFetch = 0L
        private const val EVENTS_TTL_MS = 30 * 60 * 1000L // 30 minutes
    }

    override suspend fun events(): RepoResult<List<CalendarEvent>> = withContext(Dispatchers.IO) {
        val cached = cachedEvents
        val now = System.currentTimeMillis()
        if (cached != null && (now - lastEventsFetch < EVENTS_TTL_MS)) {
            return@withContext RepoResult.Ok(cached)
        }
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
        val sorted = out.sortedBy { it.start }
        cachedEvents = sorted
        lastEventsFetch = now
        RepoResult.Ok(sorted)
    }

    override suspend fun exams(): RepoResult<List<ExamInfo>> = withContext(Dispatchers.IO) {
        val builtin = listOf(
            ExamInfo("mid-sem", "Mid Semester Examinations", "As per academic calendar", "2026-09-21", "examination"),
            ExamInfo("end-practical", "End Semester Examinations (Practical)", "As per academic calendar", "2026-11-23", "examination"),
            ExamInfo("end-theory", "End Semester Examinations (Theory)", "As per academic calendar", "2026-12-01", "examination")
        )
        when (val r = FirestoreNoticeRepository().notices()) {
            is RepoResult.Ok -> {
                val list = r.value.filter { it.category == "exam" || it.category == "examination" }
                    .map { ExamInfo(it.id, it.title, it.content, "", it.category) }
                RepoResult.Ok(builtin + list)
            }
            is RepoResult.Err -> RepoResult.Ok(builtin)
        }
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
