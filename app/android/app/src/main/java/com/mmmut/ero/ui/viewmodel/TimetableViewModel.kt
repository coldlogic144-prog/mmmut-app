package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.data.repository.FirebaseProfileRepository
import com.mmmut.ero.data.repository.LocalTimetableRepository
import com.mmmut.ero.data.repository.TimetableRepository
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TimetableScreenData(
    val profile: StudentProfile,
    val branchName: String,
    val room: String,
    val weekGrid: Map<String, List<TimetableCell>>,
    val todayName: String,
    val todayCells: List<TimetableCell>,
    val currentCell: TimetableCell?,
    val nextCell: TimetableCell?
)

class TimetableViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val ttRepo: TimetableRepository = LocalTimetableRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<TimetableScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TimetableScreenData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val profile = try { auth.currentProfile() } catch (_: Exception) { null }
            if (profile == null) {
                _state.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }
            val branch = AcademicDataExtra.getBranch(profile.branchId)
            val weekGrid = try {
                ttRepo.weekCells(
                    branchId = profile.branchId,
                    section = profile.section,
                    tutorialGroup = profile.tutorialGroup,
                    practicalGroup = profile.practicalGroup,
                    semester = profile.semester
                )
            } catch (_: Exception) { emptyMap() }
            val todayName = TimeUtils.todayName() ?: "Monday"
            val todayCells = weekGrid[todayName] ?: emptyList()

            val nowTimeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            val currentCell = com.mmmut.ero.data.local.ScheduleEngine.getCurrentClass(todayCells, nowTimeStr)
            val nextCell = com.mmmut.ero.data.local.ScheduleEngine.getNextClass(todayCells, nowTimeStr)

            _state.value = UiState.Success(
                TimetableScreenData(
                    profile = profile,
                    branchName = branch.name,
                    room = branch.room,
                    weekGrid = weekGrid,
                    todayName = todayName,
                    todayCells = todayCells,
                    currentCell = currentCell,
                    nextCell = nextCell
                )
            )
        }
    }

    fun updateGroups(tutorialGroup: String, practicalGroup: String) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            FirebaseProfileRepository().updateProfile(
                cur.profile.uid,
                mapOf("tutorialGroup" to tutorialGroup, "practicalGroup" to practicalGroup)
            )
            load()
        }
    }
}
