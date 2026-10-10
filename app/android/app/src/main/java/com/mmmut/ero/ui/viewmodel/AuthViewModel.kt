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

/* ==========================================================================
 * AuthViewModel
 * --------------------------------------------------------------------------
 * The public surface is unchanged: `signIn`, `signUp`, `signOut`,
 * `checkSession`, `clearError`, `state`, `busy`, `error`. The auth mechanism,
 * the `username@mmmut.local` mapping and every validation rule live in
 * FirebaseAuthRepository / FirebaseSignupHelper / AuthEmail, which this class
 * does not touch — it only forwards the twelve signup fields verbatim.
 *
 * What is new:
 *  - `lastError` exposes the Throwable if a repository call ever throws
 *    instead of returning `RepoResult.Err`. Screens pass it through
 *    `friendlyError(...)` so a user never sees a raw exception message.
 *  - `busy` is reset in a `finally`, so a thrown call can no longer leave the
 *    submit button permanently disabled.
 * ========================================================================== */
class AuthViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<StudentProfile>>(UiState.Empty)
    val state: StateFlow<UiState<StudentProfile>> = _state

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _error = MutableStateFlow<String?>(null)

    /** Already-human message produced by the repositories. */
    val error: StateFlow<String?> = _error

    private val _lastError = MutableStateFlow<Throwable?>(null)

    /** Non-null only when a repository call threw. Feed to `friendlyError(...)`. */
    val lastError: StateFlow<Throwable?> = _lastError

    fun checkSession(onResult: (StudentProfile?) -> Unit) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val p = try {
                repo.currentProfile()
            } catch (e: Exception) {
                _lastError.value = e
                null
            }
            _state.value = if (p == null) UiState.Empty else UiState.Success(p)
            onResult(p)
        }
    }

    fun signIn(id: String, pw: String, useRoll: Boolean, onOk: (StudentProfile) -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            clearError()
            try {
                when (val r = repo.signIn(id, pw, useRoll)) {
                    is RepoResult.Ok -> {
                        _state.value = UiState.Success(r.value)
                        onOk(r.value)
                    }
                    is RepoResult.Err -> {
                        _error.value = r.message
                        _state.value = UiState.Error(r.message)
                    }
                }
            } catch (e: Exception) {
                _lastError.value = e
                _state.value = UiState.Error("Sign-in failed.")
            } finally {
                _busy.value = false
            }
        }
    }

    /**
     * Forwards the signup fields unchanged to the repository. The order and the
     * meaning of every parameter match `AuthRepository.signUp` exactly.
     */
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
            _busy.value = true
            clearError()
            try {
                when (val r = repo.signUp(u, pw, name, branch, semester, sec, tutGroup, pracGroup, hostel, roomNumber, gender, roll)) {
                    is RepoResult.Ok -> {
                        _state.value = UiState.Success(r.value)
                        onOk(r.value)
                    }
                    is RepoResult.Err -> {
                        _error.value = r.message
                        _state.value = UiState.Error(r.message)
                    }
                }
            } catch (e: Exception) {
                _lastError.value = e
                _state.value = UiState.Error("Account creation failed.")
            } finally {
                _busy.value = false
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.signOut()
            } catch (e: Exception) {
                _lastError.value = e
            }
            _state.value = UiState.Empty
            onDone()
        }
    }

    fun clearError() {
        _error.value = null
        _lastError.value = null
    }
}
