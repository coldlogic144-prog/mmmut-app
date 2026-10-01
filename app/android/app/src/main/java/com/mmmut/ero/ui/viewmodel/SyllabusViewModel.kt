package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
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
    val subjectSyllabi: List<SubjectSyllabus>
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

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val profile = try { auth.currentProfile() } catch (_: Exception) { null }
            if (profile == null) {
                _state.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }
            currentUid = profile.uid
            currentBranchId = profile.branchId

            when (val r = syllabusRepo.getSyllabus(currentUid, currentBranchId)) {
                is RepoResult.Ok -> {
                    _state.value = UiState.Success(SyllabusData(profile.branchId.uppercase(), r.value))
                }
                is RepoResult.Err -> _state.value = UiState.Error(r.message)
            }
        }
    }

    fun toggleTopic(subjectCode: String, topicKey: String, completed: Boolean) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
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
            _state.value = UiState.Success(cur.copy(subjectSyllabi = updatedSyllabi))
            syllabusRepo.toggleTopic(currentUid, currentBranchId, topicKey, completed)
        }
    }
}
