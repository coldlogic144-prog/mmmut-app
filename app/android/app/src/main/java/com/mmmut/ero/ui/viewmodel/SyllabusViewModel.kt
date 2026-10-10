package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.local.SyllabusData as LocalSyllabusData
import com.mmmut.ero.data.model.SubjectSyllabus
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.data.repository.FirebaseSyllabusRepository
import com.mmmut.ero.data.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SyllabusData(
    val branchName: String,
    val subjectSyllabi: List<SubjectSyllabus>,
    val branchId: String = "civil",
    val selectedView: String = "1",
    val availableViews: List<Pair<String, String>> = emptyList(),
    val availableBranches: List<Pair<String, String>> = emptyList(),
    val searchQuery: String = "",
    val programCredits: Int = 0,
    val programTopicsDone: Int = 0,
    val programTotalTopics: Int = 0
) {
    val totalCompleted: Int get() = subjectSyllabi.sumOf { it.completedTopics }
    val totalTopics: Int get() = subjectSyllabi.sumOf { it.totalTopics }
    val overallPercent: Double get() = if (totalTopics == 0) 0.0 else (totalCompleted * 100.0 / totalTopics)
}

class SyllabusViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val syllabusRepo: SyllabusRepository = FirebaseSyllabusRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<SyllabusData>>(UiState.Loading)
    val state: StateFlow<UiState<SyllabusData>> = _state

    private var currentUid: String = ""
    private var currentBranchId: String = ""
    private var currentView: String = "1"
    private var currentSearch: String = ""
    private var completedKeysCache: Set<String> = emptySet()

    fun load(branchId: String? = null, view: String? = null) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val profile = try { auth.currentProfile() } catch (_: Exception) { null }
            currentUid = profile?.uid ?: ""
            val defaultBranch = profile?.branchId?.takeIf { it.isNotBlank() } ?: "civil"
            currentBranchId = branchId?.takeIf { it.isNotBlank() }
                ?: if (currentBranchId.isNotBlank()) currentBranchId else ScheduleEngine.canonicalBranchId(defaultBranch)
            if (currentBranchId.isBlank()) currentBranchId = "civil"
            currentView = view ?: if (currentView.isNotBlank()) currentView else "1"

            refreshData()
        }
    }

    fun selectBranch(newBranchId: String) {
        currentBranchId = ScheduleEngine.canonicalBranchId(newBranchId)
        currentView = "1"
        currentSearch = ""
        viewModelScope.launch {
            refreshData()
        }
    }

    fun selectView(newView: String) {
        currentView = newView
        viewModelScope.launch {
            refreshData()
        }
    }

    fun setSearchQuery(query: String) {
        currentSearch = query
        viewModelScope.launch {
            refreshData()
        }
    }

    private suspend fun refreshData() {
        val branchObj = LocalSyllabusData.getBranch(currentBranchId)
        val branchDisplayName = branchObj?.name ?: currentBranchId.uppercase()
        val availableViews = LocalSyllabusData.getAvailableViewsForBranch(currentBranchId)
        val availableBranches = LocalSyllabusData.getAllBranches()

        if (availableViews.isNotEmpty() && availableViews.none { it.first == currentView }) {
            currentView = availableViews.first().first
        }

        when (val compRes = syllabusRepo.getCompletedKeys(currentUid, currentBranchId)) {
            is RepoResult.Ok -> completedKeysCache = compRes.value
            is RepoResult.Err -> { }
        }

        when (val r = syllabusRepo.getSyllabusForView(currentUid, currentBranchId, currentView)) {
            is RepoResult.Ok -> {
                val allBranchCourses = LocalSyllabusData.getAllCoursesForBranch(currentBranchId)
                val allBranchSyllabi = allBranchCourses.map { LocalSyllabusData.getSyllabusForCourse(currentBranchId, it, completedKeysCache) }
                val progTotalTopics = allBranchSyllabi.sumOf { it.totalTopics }
                val progDoneTopics = allBranchSyllabi.sumOf { it.completedTopics }
                val progCredits = allBranchSyllabi.sumOf { it.credits }

                val displayed = if (currentSearch.isNotBlank()) {
                    filterSyllabi(r.value, currentSearch)
                } else {
                    r.value
                }

                _state.value = UiState.Success(
                    SyllabusData(
                        branchName = branchDisplayName,
                        subjectSyllabi = displayed,
                        branchId = currentBranchId,
                        selectedView = currentView,
                        availableViews = availableViews,
                        availableBranches = availableBranches,
                        searchQuery = currentSearch,
                        programCredits = progCredits,
                        programTopicsDone = progDoneTopics,
                        programTotalTopics = progTotalTopics
                    )
                )
            }
            is RepoResult.Err -> _state.value = UiState.Error(r.message)
        }
    }

    private fun filterSyllabi(list: List<SubjectSyllabus>, q: String): List<SubjectSyllabus> {
        val trimmed = q.trim()
        if (trimmed.isBlank()) return list
        return list.filter { sub ->
            sub.subjectCode.contains(trimmed, ignoreCase = true) ||
            sub.subjectName.contains(trimmed, ignoreCase = true) ||
            sub.units.any { u ->
                u.unitTitle.contains(trimmed, ignoreCase = true) ||
                u.topics.any { t -> t.name.contains(trimmed, ignoreCase = true) }
            }
        }
    }

    fun toggleTopic(subjectCode: String, topicKey: String, completed: Boolean) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        completedKeysCache = if (completed) completedKeysCache + topicKey else completedKeysCache - topicKey

        val updatedSyllabi = cur.subjectSyllabi.map { sub ->
            if (sub.subjectCode.equals(subjectCode, ignoreCase = true)) {
                val updatedUnits = sub.units.map { unit ->
                    val updatedTopics = unit.topics.map { t ->
                        if (t.key == topicKey) t.copy(completed = completed) else t
                    }
                    unit.copy(topics = updatedTopics)
                }
                sub.copy(units = updatedUnits)
            } else sub
        }
        val progDone = if (completed) cur.programTopicsDone + 1 else (cur.programTopicsDone - 1).coerceAtLeast(0)
        _state.value = UiState.Success(cur.copy(subjectSyllabi = updatedSyllabi, programTopicsDone = progDone))

        viewModelScope.launch {
            syllabusRepo.toggleTopic(currentUid, currentBranchId, topicKey, completed)
        }
    }

    fun toggleCourse(subjectCode: String, completed: Boolean) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        val targetSub = cur.subjectSyllabi.firstOrNull { it.subjectCode.equals(subjectCode, ignoreCase = true) } ?: return
        val allKeys = targetSub.units.flatMap { it.topics.map { t -> t.key } }

        completedKeysCache = if (completed) completedKeysCache + allKeys.toSet() else completedKeysCache - allKeys.toSet()

        val updatedSyllabi = cur.subjectSyllabi.map { sub ->
            if (sub.subjectCode.equals(subjectCode, ignoreCase = true)) {
                val updatedUnits = sub.units.map { unit ->
                    val updatedTopics = unit.topics.map { t -> t.copy(completed = completed) }
                    unit.copy(topics = updatedTopics)
                }
                sub.copy(units = updatedUnits)
            } else sub
        }
        _state.value = UiState.Success(cur.copy(subjectSyllabi = updatedSyllabi))

        viewModelScope.launch {
            syllabusRepo.toggleCourse(currentUid, currentBranchId, allKeys, completed)
        }
    }

    fun resetBranchProgress() {
        viewModelScope.launch {
            completedKeysCache = emptySet()
            syllabusRepo.resetBranchProgress(currentUid, currentBranchId)
            refreshData()
        }
    }
}
