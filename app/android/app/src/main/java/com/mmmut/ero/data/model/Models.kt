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
    val id: String = "",
    val day: String = "",
    val periodKey: String = "",
    val startPeriod: String = "",
    val endPeriod: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val type: String = "",
    val start: String = "",
    val end: String = "",
    val tutorialGroup: String? = null,
    val practicalGroup: String? = null,
    val room: String = "TL-206",
    val teacher: String = "",
    val instructor: String = teacher,
    val notes: String = ""
)

data class TimetableEntry(
    val id: String = "",
    val academicSession: String = "2025-26",
    val branch: String = "civil",
    val section: String = "A",
    val semester: Int = 1,
    val day: String = "Monday",
    val startPeriod: String = "I",
    val endPeriod: String = "I",
    val startTime: String = "09:10",
    val endTime: String = "10:00",
    val subjectCode: String = "",
    val subjectName: String = "",
    val type: String = "LECTURE", // LECTURE, TUTORIAL, PRACTICAL
    val tutorialGroup: String? = null, // "T1", "T2", or null (for all)
    val practicalGroup: String? = null, // "P1", "P2", "P3", "P4", or null (for all)
    val room: String = "TL-206",
    val teacher: String = "",
    val notes: String = ""
) {
    val periodDisplay: String get() = if (startPeriod == endPeriod) startPeriod else "$startPeriod-$endPeriod"
    val classType: String get() = type
    val instructor: String get() = teacher

    fun toCell(): TimetableCell = TimetableCell(
        id = id,
        day = day,
        periodKey = periodDisplay,
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        subjectCode = subjectCode,
        subjectName = subjectName,
        type = type.lowercase().replaceFirstChar { it.uppercase() },
        start = startTime,
        end = endTime,
        tutorialGroup = tutorialGroup,
        practicalGroup = practicalGroup,
        room = room,
        teacher = teacher,
        instructor = teacher,
        notes = notes
    )
}

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
    val hours: Int = 0,
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
    val units: List<SyllabusUnit> = emptyList(),
    val prerequisite: String = "",
    val assessment: String = "",
    val objectives: String = "",
    val courseOutcomes: List<String> = emptyList(),
    val referenceBooks: List<String> = emptyList()
) {
    val totalTopics: Int get() = units.sumOf { it.totalCount }
    val completedTopics: Int get() = units.sumOf { it.completedCount }
    val percent: Double get() = if (totalTopics == 0) 0.0 else (completedTopics * 100.0 / totalTopics)
}

data class MessDay(
    val day: String = "",
    val breakfast: List<String> = emptyList(),
    val lunch: List<String> = emptyList(),
    val snacks: List<String> = emptyList(),
    val dinner: List<String> = emptyList()
)

data class MessWeeklyMenu(
    val days: List<MessDay> = emptyList(),
    val lastUpdated: Long = 0L,
    val note: String = ""
)
