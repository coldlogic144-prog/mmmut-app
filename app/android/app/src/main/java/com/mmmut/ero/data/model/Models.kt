package com.mmmut.ero.data.model

data class StudentProfile(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val branchId: String = "cse",
    val section: String = "A",
    val hostel: String = "Day Scholar",
    val gender: String = "Not specified",
    val isAdmin: Boolean = false,
    val adminRequested: Boolean = false,
    val migrationStatus: String = "pending",
    val rollNumber: String = "",
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
    val end: String = ""
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

