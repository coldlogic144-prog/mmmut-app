package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeScreenData(
    val profile: StudentProfile,
    val notices: List<Notice>,
    val unreadNoticeCount: Int,
    val todayCells: List<TimetableCell>
)

class HomeViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val noticeRepo: NoticeRepository = FirestoreNoticeRepository(),
    private val timetable: TimetableRepository = LocalTimetableRepository()
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
                val today = try {
                    timetable.todayCells(
                        branchId = profile.branchId,
                        section = profile.section,
                        tutorialGroup = profile.tutorialGroup,
                        practicalGroup = profile.practicalGroup,
                        semester = profile.semester
                    )
                } catch (_: Exception) { emptyList() }
                val notices = when (val r = noticeRepo.notices()) {
                    is com.mmmut.ero.core.RepoResult.Ok -> r.value
                    is com.mmmut.ero.core.RepoResult.Err -> emptyList()
                }
                val unread = notices.count { it.createdAtMillis.toDouble() > profile.lastReadPosts }
                _state.value = UiState.Success(HomeScreenData(profile, notices, unread, today))
            } catch (e: Exception) {
                _state.value = UiState.Error("Could not load home data: ${e.message}")
            }
        }
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
