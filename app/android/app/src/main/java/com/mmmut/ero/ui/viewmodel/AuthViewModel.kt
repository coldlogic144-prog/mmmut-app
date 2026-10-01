package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<StudentProfile>>(UiState.Empty)
    val state: StateFlow<UiState<StudentProfile>> = _state
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun checkSession(onResult: (StudentProfile?) -> Unit) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val p = try { repo.currentProfile() } catch (_: Exception) { null }
            _state.value = if (p == null) UiState.Empty else UiState.Success(p)
            onResult(p)
        }
    }

    fun signIn(id: String, pw: String, useRoll: Boolean, onOk: (StudentProfile) -> Unit) {
        viewModelScope.launch {
            _busy.value = true; _error.value = null
            when (val r = repo.signIn(id, pw, useRoll)) {
                is RepoResult.Ok -> { _state.value = UiState.Success(r.value); onOk(r.value) }
                is RepoResult.Err -> { _error.value = r.message; _state.value = UiState.Error(r.message) }
            }
            _busy.value = false
        }
    }

    fun signUp(
        u: String,
        pw: String,
        name: String,
        branch: String,
        semester: Int,
        sec: String,
        tutGroup: String,
        pracGroup: String,
        hostel: String,
        roomNumber: String,
        gender: String,
        roll: String,
        onOk: (StudentProfile) -> Unit
    ) {
        viewModelScope.launch {
            _busy.value = true; _error.value = null
            when (val r = repo.signUp(u, pw, name, branch, semester, sec, tutGroup, pracGroup, hostel, roomNumber, gender, roll)) {
                is RepoResult.Ok -> { _state.value = UiState.Success(r.value); onOk(r.value) }
                is RepoResult.Err -> { _error.value = r.message; _state.value = UiState.Error(r.message) }
            }
            _busy.value = false
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch { repo.signOut(); _state.value = UiState.Empty; onDone() }
    }

    fun clearError() { _error.value = null }
}
