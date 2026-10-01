package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.TelegramApplication
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.data.repository.FirebaseTelegramRepository
import com.mmmut.ero.data.repository.TelegramRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TelegramScreenData(
    val uid: String,
    val username: String,
    val application: TelegramApplication?
)

class TelegramViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val telegram: TelegramRepository = FirebaseTelegramRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<TelegramScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TelegramScreenData>> = _state

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg

    private var pollJob: Job? = null

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val profile = try { auth.currentProfile() } catch (_: Exception) { null }
            if (profile == null) {
                _state.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }
            when (val r = telegram.getApplication(profile.uid)) {
                is RepoResult.Ok -> {
                    _state.value = UiState.Success(TelegramScreenData(profile.uid, profile.username, r.value))
                }
                is RepoResult.Err -> {
                    _state.value = UiState.Success(TelegramScreenData(profile.uid, profile.username, null))
                }
            }
        }
    }

    fun apply() {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.apply(cur.uid)) {
                is RepoResult.Ok -> load()
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    fun reapply() {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.reapply(cur.uid)) {
                is RepoResult.Ok -> load()
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    fun connectTelegram(onBotUrl: (String) -> Unit) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.createToken(cur.uid)) {
                is RepoResult.Ok -> {
                    val token = r.value
                    val botUrl = "https://t.me/mmmut_erp_bot?start=$token"
                    onBotUrl(botUrl)
                    startPolling(token)
                }
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    private fun startPolling(token: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(30) {
                delay(3000)
                when (val r = telegram.checkTokenStatus(token)) {
                    is RepoResult.Ok -> {
                        if (r.value) {
                            _msg.value = "Telegram account connected successfully!"
                            load()
                            return@launch
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun saveHandle(handle: String) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.saveHandle(cur.uid, handle)) {
                is RepoResult.Ok -> {
                    _msg.value = "Telegram handle saved successfully."
                    load()
                }
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    fun joinChannel(onInviteUrl: (String) -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.getInviteLink()) {
                is RepoResult.Ok -> onInviteUrl(r.value)
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    fun checkMembership() {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.checkMembership(cur.uid, cur.application?.telegramUserId)) {
                is RepoResult.Ok -> {
                    if (r.value) {
                        _msg.value = "Verified! You are a member of MMMUT Private Channel."
                    } else {
                        _msg.value = "Not verified as a channel member yet. Request access in Telegram first."
                    }
                    load()
                }
                is RepoResult.Err -> _msg.value = r.message
            }
            _busy.value = false
        }
    }

    fun clearMsg() { _msg.value = null }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
