package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.*
import com.mmmut.ero.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AcademicsData(
    val profile: StudentProfile,
    val week: Map<String, List<TimetableCell>>,
    val map: Map<String, Map<String, String>>,
    val holidays: Set<String>,
    val events: List<CalendarEvent>,
    val exams: List<ExamInfo>,
    val results: List<ResultEntry>
)

class AcademicsViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val tt: TimetableRepository = LocalTimetableRepository(),
    private val att: AttendanceRepository = FirestoreAttendanceRepository(),
    private val cal: CalendarRepository = FirestoreCalendarRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<AcademicsData>>(UiState.Loading)
    val state: StateFlow<UiState<AcademicsData>> = _state
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = auth.currentProfile() ?: run {
                    _state.value = UiState.Error("Session expired. Please log in again."); return@launch
                }
                val week = try { tt.weekCells(profile.branchId, profile.section) } catch (_: Exception) { emptyMap() }
                val map = when (val r = att.loadMap(profile.uid)) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptyMap()
                }
                val hol = when (val r = att.holidays()) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptySet()
                }
                val events = when (val r = cal.events()) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptyList()
                }
                val exams = when (val r = cal.exams()) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptyList()
                }
                val results = when (val r = cal.results(profile.uid)) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptyList()
                }
                _state.value = UiState.Success(AcademicsData(profile, week, map, hol, events, exams, results))
            } catch (_: Exception) { _state.value = UiState.Error("Could not load academics. Check your connection and retry.") }
        }
    }
    fun toggleAttendance(periodKey: String, subjectCode: String, onSaved: (Map<String, Map<String, String>>) -> Unit) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val today = com.mmmut.ero.util.TimeUtils.dateKey()
            if (cur.holidays.contains(today)) return@launch
            val mutable = cur.map.mapValues { it.value.toMutableMap() }.toMutableMap()
            val day = mutable.getOrPut(today) { mutableMapOf() }
            val key = "$periodKey::$subjectCode"
            if (day[key] == "present") day.remove(key) else day[key] = "present"
            val merged = cur.map.toMutableMap(); merged[today] = day.toMap()
            att.saveMap(cur.profile.uid, merged.toMap())
            onSaved(emptyMap())
            load()
        }
    }
}

class HostelViewModel(private val repo: HostelRepository = FirestoreHostelRepository()) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Notice>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Notice>>> = _state
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val r = repo.announcements()) {
                is RepoResult.Ok -> _state.value = if (r.value.isEmpty()) UiState.Empty else UiState.Success(r.value)
                is RepoResult.Err -> _state.value = UiState.Error(r.message)
            }
        }
    }
}

class NotificationsViewModel : ViewModel() {
    private val _history = MutableStateFlow<List<StoredNotification>>(emptyList())
    val history: StateFlow<List<StoredNotification>> = _history
    fun load(ctx: android.content.Context) {
        viewModelScope.launch {
            try { _history.value = com.mmmut.ero.notifications.NotificationHistoryStore(ctx).all() }
            catch (_: Exception) { _history.value = emptyList() }
        }
    }
}

class VerifyRollViewModel(private val repo: RosterRepository = RosterRepositoryImpl()) : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg
    fun claim(roll: String, onOk: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true; _msg.value = null
            when (val r = repo.claimRoll(roll)) {
                is RepoResult.Ok -> onOk()
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }
    fun clear() { _msg.value = null }
}
