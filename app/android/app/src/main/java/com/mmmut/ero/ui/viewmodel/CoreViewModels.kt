package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.repository.*
import com.mmmut.ero.util.AttendanceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeData(
    val profile: StudentProfile,
    val today: List<TimetableCell>,
    val present: Int, val absent: Int, val pct: Double,
    val notices: List<Notice>
)

class HomeViewModel(
    private val notices: NoticeRepository = FirestoreNoticeRepository(),
    private val attendance: AttendanceRepository = FirestoreAttendanceRepository(),
    private val timetable: TimetableRepository = LocalTimetableRepository(),
    private val auth: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = auth.currentProfile() ?: run {
                    _state.value = UiState.Error("Session expired. Please log in again."); return@launch
                }
                val today = try { timetable.todayCells(profile.branchId, profile.section) } catch (_: Exception) { emptyList() }
                val map = when (val r = attendance.loadMap(profile.uid)) {
                    is RepoResult.Ok -> r.value; is RepoResult.Err -> emptyMap()
                }
                val (p, a, pct) = AttendanceUtils.summarize(map)
                val nts = when (val r = notices.notices()) {
                    is RepoResult.Ok -> r.value.take(5); is RepoResult.Err -> emptyList()
                }
                _state.value = UiState.Success(HomeData(profile, today, p, a, pct, nts))
            } catch (e: Exception) { _state.value = UiState.Error("Could not load home. Check your connection and retry.") }
        }
    }
}

class NoticesViewModel(private val repo: NoticeRepository = FirestoreNoticeRepository()) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Notice>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Notice>>> = _state
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val r = repo.notices()) {
                is RepoResult.Ok -> _state.value = if (r.value.isEmpty()) UiState.Empty else UiState.Success(r.value)
                is RepoResult.Err -> _state.value = UiState.Error(r.message)
            }
        }
    }
}

class NoticeDetailViewModel(private val repo: NoticeRepository = FirestoreNoticeRepository()) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Notice>>(UiState.Loading)
    val state: StateFlow<UiState<Notice>> = _state
    fun load(id: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val r = repo.notice(id)) {
                is RepoResult.Ok -> _state.value = UiState.Success(r.value)
                is RepoResult.Err -> _state.value = UiState.Error(r.message)
            }
        }
    }
}

class ProfileViewModel(
    private val repo: ProfileRepository = FirebaseProfileRepository(),
    private val auth: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<StudentProfile>>(UiState.Loading)
    val state: StateFlow<UiState<StudentProfile>> = _state
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val p = try { auth.currentProfile() } catch (_: Exception) { null }
            _state.value = if (p == null) UiState.Error("Session expired. Please log in again.") else UiState.Success(p)
        }
    }
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch { auth.signOut(); onDone() }
    }
}
