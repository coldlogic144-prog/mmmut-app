package com.mmmut.ero.data.model

data class StudentProfile(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val rollNumber: String = "",
    val branchId: String = "cse",
    val semester: Int = 1,
    val section: String = "A",
    val tutorialGroup: String = "T1", // "T1", "T2", "N/A"
    val practicalGroup: String = "P1", // "P1", "P2", "N/A"
    val hostel: String = "Day Scholar",
    val roomNumber: String = "",
    val gender: String = "Not specified",
    val isAdmin: Boolean = false,
    val adminRequested: Boolean = false,
    val migrationStatus: String = "pending",
    val rollNumberVerified: Boolean = false,
    val pendingRollNumber: String = "",
    val migrationReviewReason: String = "",
    val createdAt: Long = 0L,
    val lastReadPosts: Double = 0.0
)

data class RosterRecord(
    val rollNumber: String = "",
    val enrollmentNo: String = "",
    val applicantName: String = "",
    val formalName: String = "",
    val branchName: String = "",
    val section: String = "",
    val batch: String = "",
    val block: Int = 0,
    val sourceFormNumber: String = ""
)

data class Notice(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "general",
    val pinned: Boolean = false,
    val important: Boolean = false,
    val linkUrl: String = "",
    val createdAtMillis: Long = 0L
)

data class TimetableCell(
    val periodKey: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val type: String = "",
    val start: String = "",
    val end: String = "",
    val tutorialGroup: String? = null,
    val practicalGroup: String? = null,
    val room: String = "TL-206"
)

data class TimetableEntry(
    val academicSession: String = "2025-26",
    val branch: String = "civil",
    val section: String = "A",
    val semester: Int = 1,
    val day: String = "Monday",
    val period: String = "I",
    val startTime: String = "09:10",
    val endTime: String = "10:00",
    val subjectCode: String = "",
    val subjectName: String = "",
    val classType: String = "LECTURE", // LECTURE, TUTORIAL, PRACTICAL, LUNCH, FREE
    val tutorialGroup: String? = null, // "T1", "T2", or null
    val practicalGroup: String? = null, // "P1", "P2", or null
    val room: String = "TL-206",
    val teacher: String = "",
    val notes: String = ""
)

data class SubjectInfo(
    val code: String = "",
    val name: String = "",
    val l: Int = 0,
    val t: Int = 0,
    val p: Int = 0
)

data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val start: String = "",
    val end: String = "",
    val source: String = "builtin"
)

data class ExamInfo(
    val id: String = "",
    val title: String = "",
    val detail: String = "",
    val date: String = "",
    val category: String = "examination"
)

data class ResultEntry(
    val subjectCode: String = "",
    val subjectName: String = "",
    val grade: String = "",
    val credits: Double = 0.0
)

data class StoredNotification(
    val id: String = "",
    val type: String = "general",
    val title: String = "",
    val body: String = "",
    val refId: String = "",
    val receivedAt: Long = 0L
)

data class TelegramApplication(
    val status: String = "NOT_APPLIED",
    val appliedAt: Long = 0L,
    val telegramUserId: String? = null,
    val telegramUsername: String? = null,
    val isMember: Boolean = false
)

data class AdminRequest(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val status: String = "pending",
    val requestedAt: Long = 0L
)

data class TopicItem(
    val key: String = "",
    val name: String = "",
    val completed: Boolean = false
)

data class SyllabusUnit(
    val unitNumber: String = "",
    val unitTitle: String = "",
    val topics: List<TopicItem> = emptyList()
) {
    val completedCount: Int get() = topics.count { it.completed }
    val totalCount: Int get() = topics.size
}

data class SubjectSyllabus(
    val subjectCode: String = "",
    val subjectName: String = "",
    val detailKey: String = "",
    val category: String = "",
    val credits: Int = 4,
    val ltp: String = "",
    val units: List<SyllabusUnit> = emptyList()
) {
    val totalTopics: Int get() = units.sumOf { it.totalCount }
    val completedTopics: Int get() = units.sumOf { it.completedCount }
    val percent: Double get() = if (totalTopics == 0) 0.0 else (completedTopics * 100.0 / totalTopics)
}
