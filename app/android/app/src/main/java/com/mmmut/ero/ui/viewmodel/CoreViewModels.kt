package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.repository.*
import com.mmmut.ero.ui.components.friendlyError
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo
import com.mmmut.ero.util.SubjectAttendanceStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/** Minimum attendance the university requires before a student is debarred. */
private const val ATTENDANCE_TARGET_PCT = 75.0

/**
 * Attendance block for the dashboard.
 *
 * [needsAttention] is capped at three subjects so the card stays scannable;
 * it is derived from [AttendanceUtils.subjectBreakdown] over the branch's
 * official subject list, so no subject outside the curriculum can appear.
 */
data class HomeAttendanceSummary(
    val present: Int = 0,
    val absent: Int = 0,
    val percent: Double = 0.0,
    val needsAttention: List<SubjectAttendanceStats> = emptyList(),
    val projection: String? = null
) {
    val total: Int get() = present + absent
    val hasRecords: Boolean get() = total > 0
}

data class HomeScreenData(
    val profile: StudentProfile,
    val notices: List<Notice>,
    val unreadNoticeCount: Int,
    val todayCells: List<TimetableCell>,
    /** Weekday name for [todayCells]; null means "no teaching day" (Sunday). */
    val todayName: String? = null,
    val attendance: HomeAttendanceSummary = HomeAttendanceSummary()
)

/** Calendar weekday -> the day key used by AcademicData.DAYS / the timetable data. */
private fun currentTeachingDayName(): String? =
    when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> null // Sunday is not a teaching day in the official timetable.
    }

class HomeViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val noticeRepo: NoticeRepository = FirestoreNoticeRepository(),
    private val timetable: TimetableRepository = LocalTimetableRepository(),
    private val attendance: AttendanceRepository = FirestoreAttendanceRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<HomeScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeScreenData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = auth.currentProfile() ?: run {
                    _state.value = UiState.Error("Session expired. Please log in again."); return@launch
                }

                // Resolve the weekday ourselves instead of relying on the
                // "todayName ?: Monday" fallback inside ScheduleEngine, which
                // showed Monday's classes as "today" on Saturdays and Sundays.
                val dayName = currentTeachingDayName()
                val today: List<TimetableCell> = try {
                    if (dayName == null) emptyList() else ScheduleEngine.getToday(profile, dayName)
                } catch (_: Exception) { emptyList() }

                val notices = try {
                    when (val r = noticeRepo.notices()) {
                        is RepoResult.Ok -> r.value
                        is RepoResult.Err -> emptyList()
                    }
                } catch (_: Exception) { emptyList() }

                val unread = notices.count { it.createdAtMillis.toDouble() > profile.lastReadPosts }

                _state.value = UiState.Success(
                    HomeScreenData(
                        profile = profile,
                        notices = notices,
                        unreadNoticeCount = unread,
                        todayCells = today,
                        todayName = dayName,
                        attendance = loadAttendance(profile)
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(
                    friendlyError(e, "Could not load your dashboard. Check your connection and try again.")
                )
            }
        }
    }

    /**
     * Reads the same attendance document AcademicsViewModel uses
     * (FirestoreAttendanceRepository -> ATTENDANCE/{uid}) and summarises it.
     * A failure here must never take the dashboard down, so everything
     * degrades to an empty summary.
     */
    private suspend fun loadAttendance(profile: StudentProfile): HomeAttendanceSummary {
        val map = try {
            when (val r = attendance.loadMap(profile.uid)) {
                is RepoResult.Ok -> r.value
                is RepoResult.Err -> emptyMap()
            }
        } catch (_: Exception) { emptyMap() }
        if (map.isEmpty()) return HomeAttendanceSummary()

        val (present, absent, percent) = AttendanceUtils.summarize(map)
        if (present + absent == 0) return HomeAttendanceSummary()

        val subjects = try { AcademicDataExtra.getBranch(profile.branchId).subjects } catch (_: Exception) { emptyList() }
        val needsAttention = AttendanceUtils.subjectBreakdown(map, subjects)
            .filter { it.total > 0 && it.pct < ATTENDANCE_TARGET_PCT }
            .sortedBy { it.pct }
            .take(3)

        val projection = when (val info = AttendanceUtils.computeLeaveInfo(present, absent, ATTENDANCE_TARGET_PCT)) {
            is LeaveInfo.CanSkip -> if (info.count == 0) {
                "Right on the 75% line — don't miss the next class."
            } else {
                "You can miss up to ${info.count} more ${if (info.count == 1) "class" else "classes"} and stay above 75%."
            }
            is LeaveInfo.MustAttend ->
                "Attend the next ${info.count} ${if (info.count == 1) "class" else "classes"} to get back above 75%."
            LeaveInfo.None -> null
        }

        return HomeAttendanceSummary(
            present = present,
            absent = absent,
            percent = percent,
            needsAttention = needsAttention,
            projection = projection
        )
    }
}

class ProfileViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val repo: ProfileRepository = FirebaseProfileRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<StudentProfile>>(UiState.Loading)
    val state: StateFlow<UiState<StudentProfile>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val p = auth.currentProfile()
            if (p != null) _state.value = UiState.Success(p)
            else _state.value = UiState.Error("Profile not found.")
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch { auth.signOut(); onDone() }
    }

    fun updateGroups(tGroup: String, pGroup: String, onDone: () -> Unit) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repo.updateProfile(cur.uid, mapOf("tutorialGroup" to tGroup, "practicalGroup" to pGroup))
            load()
            onDone()
        }
    }

    fun updateAcademicProfile(
        semester: Int,
        section: String,
        tutGroup: String,
        pracGroup: String,
        hostel: String,
        roomNumber: String,
        onDone: () -> Unit
    ) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repo.updateProfile(
                cur.uid,
                mapOf(
                    "semester" to semester,
                    "section" to section,
                    "tutorialGroup" to tutGroup,
                    "practicalGroup" to pracGroup,
                    "hostel" to hostel,
                    "roomNumber" to roomNumber
                )
            )
            load()
            onDone()
        }
    }
}
