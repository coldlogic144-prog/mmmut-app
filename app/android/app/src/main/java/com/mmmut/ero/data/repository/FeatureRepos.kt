package com.mmmut.ero.data.repository

import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.CalendarEvent
import com.mmmut.ero.data.model.ExamInfo
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.ResultEntry
import com.mmmut.ero.data.model.TimetableCell

interface NoticeRepository {
    suspend fun notices(): RepoResult<List<Notice>>
    suspend fun notice(id: String): RepoResult<Notice>
}

interface AttendanceRepository {
    suspend fun loadMap(uid: String): RepoResult<Map<String, Map<String, String>>>
    suspend fun saveMap(uid: String, map: Map<String, Map<String, String>>): RepoResult<Unit>
    suspend fun holidays(): RepoResult<Set<String>>
}

interface TimetableRepository {
    suspend fun weekCells(branchId: String, section: String, tutorialGroup: String = "T1", practicalGroup: String = "P1", semester: Int = 1): Map<String, List<TimetableCell>>
    suspend fun todayCells(branchId: String, section: String, tutorialGroup: String = "T1", practicalGroup: String = "P1", semester: Int = 1): List<TimetableCell>
}

interface CalendarRepository {
    suspend fun events(): RepoResult<List<CalendarEvent>>
    suspend fun exams(): RepoResult<List<ExamInfo>>
    suspend fun results(uid: String): RepoResult<List<ResultEntry>>
}

interface HostelRepository {
    suspend fun announcements(): RepoResult<List<Notice>>
}

interface NotificationRepository {
    suspend fun registerToken(uid: String, token: String)
    suspend fun unregisterToken(uid: String, token: String)
}

interface TelegramRepository {
    suspend fun getApplication(uid: String): RepoResult<com.mmmut.ero.data.model.TelegramApplication?>
    suspend fun apply(uid: String): RepoResult<Unit>
    suspend fun reapply(uid: String): RepoResult<Unit>
    suspend fun createToken(uid: String): RepoResult<String>
    suspend fun checkTokenStatus(token: String): RepoResult<Boolean>
    suspend fun saveHandle(uid: String, handle: String): RepoResult<Unit>
    suspend fun getInviteLink(): RepoResult<String>
    suspend fun checkMembership(uid: String, telegramUserId: String?): RepoResult<Boolean>
}
