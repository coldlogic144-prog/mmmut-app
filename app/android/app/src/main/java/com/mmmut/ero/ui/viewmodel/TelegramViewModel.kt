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

/* ==========================================================================
 * Telegram access — explicit flow state
 * --------------------------------------------------------------------------
 * The backend is authoritative. `telegramApplications/<uid>.status` is the
 * only thing that decides what the student sees, and a *missing document* is
 * the real "not applied yet" signal (FirebaseTelegramRepository returns
 * `Ok(null)` in that case rather than inventing a state).
 *
 * Status values the backend can actually write:
 *   PENDING_ADMIN_APPROVAL -> FirebaseTelegramRepository.apply / reapply
 *   ADMIN_APPROVED         -> FirebaseAdminRepository.approveTelegramApplication
 *   ADMIN_REJECTED         -> FirebaseAdminRepository.rejectTelegramApplication
 *   CHANNEL_APPROVED       -> FirebaseTelegramRepository.checkMembership (member confirmed)
 * Anything else (including a legacy "NOT_APPLIED" document) is shown as
 * "not approved yet" and is never treated as connected.
 *
 * Telegram-account linkage and channel membership are *separate* facts. A
 * student is only reported as connected when the backend says so
 * (status == CHANNEL_APPROVED or isMember == true) or when the bot-token
 * polling in `connectTelegram` actually observed a LINKED token.
 * ========================================================================== */

/** The six UI states required by the access flow. */
enum class TelegramAccessPhase {
    /** No application document exists yet. */
    NOT_APPLIED,

    /** WAITING FOR ADMIN APPROVAL — status == PENDING_ADMIN_APPROVAL. */
    WAITING_FOR_ADMIN_APPROVAL,

    /** APPLICATION APPROVED — status == ADMIN_APPROVED, Telegram not linked yet. */
    APPLICATION_APPROVED,

    /** CONNECT TELEGRAM — bot-token link is in flight / account not linked yet. */
    CONNECT_TELEGRAM,

    /** TELEGRAM CONNECTED — account linked and/or channel membership pending. */
    TELEGRAM_CONNECTED,

    /** CHANNEL VERIFICATION — linked, but not yet confirmed a channel member. */
    CHANNEL_VERIFICATION,

    /** ACCESS ACTIVE — linked and confirmed channel member. */
    ACCESS_ACTIVE,

    /** Application was rejected by an administrator. */
    REJECTED,

    /** Backend holds a status this build does not understand. */
    UNKNOWN
}

/** Where the student is in the access flow, for the vertical stepper. */
enum class TelegramStepState { DONE, CURRENT, TODO }

data class TelegramScreenData(
    val uid: String,
    val username: String,
    val application: TelegramApplication?
)

/**
 * Everything the Telegram screen needs to render. `steps` is precomputed so
 * the composable does no work beyond drawing.
 */
data class TelegramAccessState(
    val phase: TelegramAccessPhase,
    val application: TelegramApplication?,
    val telegramLinked: Boolean,
    val channelVerified: Boolean,
    val loadFailed: Boolean = false,
    val steps: List<TelegramStepState> = emptyList()
)

class TelegramViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val telegram: TelegramRepository = FirebaseTelegramRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<TelegramScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TelegramScreenData>> = _state

    private val _access = MutableStateFlow<TelegramAccessState?>(null)
    val access: StateFlow<TelegramAccessState?> = _access

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg

    private var pollJob: Job? = null

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _access.value = null
            val profile = try { auth.currentProfile() } catch (_: Exception) { null }
            if (profile == null) {
                _state.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }
            when (val r = telegram.getApplication(profile.uid)) {
                is RepoResult.Ok -> {
                    val app = r.value
                    _state.value = UiState.Success(TelegramScreenData(profile.uid, profile.username, app))
                    _access.value = buildAccessState(app, loadFailed = false)
                }
                is RepoResult.Err -> {
                    // We genuinely do not know the backend state. Show an explicit
                    // unknown/retry state — never a "not applied" or "connected" one.
                    _state.value = UiState.Success(TelegramScreenData(profile.uid, profile.username, null))
                    _access.value = buildAccessState(null, loadFailed = true)
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
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not submit your Telegram application. Try again.")
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
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not resubmit your Telegram application. Try again.")
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
                    // Optimistically surface the CONNECT_TELEGRAM state: a token
                    // exists but the account is provably NOT linked yet.
                    _access.value = _access.value?.copy(
                        phase = TelegramAccessPhase.CONNECT_TELEGRAM,
                        telegramLinked = false
                    ) ?: buildAccessState(cur.application, loadFailed = false)
                    onBotUrl(botUrl)
                    startPolling(token)
                }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not create a Telegram link. Try again.")
            }
            _busy.value = false
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun startPolling(token: String) {
        stopPolling()
        pollJob = viewModelScope.launch {
            repeat(12) { // 12 * 4s = 48s total
                delay(4000)
                when (val r = telegram.checkTokenStatus(token)) {
                    is RepoResult.Ok -> {
                        if (r.value) {
                            // The backend confirmed the token is LINKED. Only now
                            // may the UI say Telegram is connected.
                            _msg.value = "Telegram account connected. You can now verify your channel membership."
                            load()
                            return@launch
                        }
                    }
                    else -> {}
                }
            }
            stopPolling()
        }
    }

    fun saveHandle(handle: String) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _busy.value = true
            _msg.value = null
            when (val r = telegram.saveHandle(cur.uid, handle)) {
                is RepoResult.Ok -> {
                    _msg.value = "Telegram handle saved."
                    load()
                }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not save that Telegram handle. Try again.")
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
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not fetch the channel invite link. Try again.")
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
                    _msg.value = if (r.value) {
                        "Verified. You are a member of the MMMUT private channel."
                    } else {
                        "Not a channel member yet. Open the invite link in Telegram and request access first."
                    }
                    load()
                }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not check your channel membership. Try again.")
            }
            _busy.value = false
        }
    }

    fun clearMsg() { _msg.value = null }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }

    /* ------------------------------------------------------------ mapping --- */

    private fun buildAccessState(app: TelegramApplication?, loadFailed: Boolean): TelegramAccessState {
        val status = app?.status?.trim()?.uppercase().orEmpty()
        val isMember = app?.isMember == true
        val channelVerified = status == "CHANNEL_APPROVED" || isMember
        val handle = app?.telegramUsername?.takeIf { it.isNotBlank() }
            ?: app?.telegramUserId?.takeIf { it.isNotBlank() }
        val telegramLinked = channelVerified || handle != null

        val adminApproved = status == "ADMIN_APPROVED" || status == "TELEGRAM_NOT_CONNECTED"
        val adminPending = status == "PENDING_ADMIN_APPROVAL"
        val rejected = status == "ADMIN_REJECTED" || status == "REJECTED"
        val hasDoc = app != null

        val phase = when {
            !hasDoc -> TelegramAccessPhase.NOT_APPLIED
            rejected -> TelegramAccessPhase.REJECTED
            adminPending -> TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL
            channelVerified -> TelegramAccessPhase.ACCESS_ACTIVE
            adminApproved && !telegramLinked -> TelegramAccessPhase.APPLICATION_APPROVED
            adminApproved -> TelegramAccessPhase.CHANNEL_VERIFICATION
            // A document exists with a status we do not recognise. Do not guess:
            // report it as "connected but unverified" only if the backend said so,
            // otherwise treat it as not-yet-approved.
            telegramLinked -> TelegramAccessPhase.TELEGRAM_CONNECTED
            else -> TelegramAccessPhase.UNKNOWN
        }

        val step1 = when {
            adminPending || adminApproved || channelVerified -> TelegramStepState.DONE
            rejected || !hasDoc -> TelegramStepState.TODO
            else -> TelegramStepState.TODO
        }
        val step2 = when {
            adminPending -> TelegramStepState.CURRENT
            adminApproved || channelVerified -> TelegramStepState.DONE
            else -> TelegramStepState.TODO
        }
        val step3 = when {
            channelVerified -> TelegramStepState.DONE
            adminApproved && telegramLinked -> TelegramStepState.DONE
            adminApproved -> TelegramStepState.CURRENT
            else -> TelegramStepState.TODO
        }
        val step4 = when {
            channelVerified -> TelegramStepState.DONE
            telegramLinked && adminApproved -> TelegramStepState.CURRENT
            else -> TelegramStepState.TODO
        }

        return TelegramAccessState(
            phase = phase,
            application = app,
            telegramLinked = telegramLinked,
            channelVerified = channelVerified,
            loadFailed = loadFailed,
            steps = listOf(step1, step2, step3, step4)
        )
    }

    /**
     * Repository messages sometimes carry raw exception text (Firebase paths,
     * HTTP codes). Those must never reach the screen; only messages this app
     * authored are passed through.
     */
    private fun sanitize(message: String?, fallback: String): String {
        val m = message?.trim().orEmpty()
        if (m.isEmpty()) return fallback
        val t = m.lowercase()
        val looksTechnical = t.contains("network error") || t.contains("http ") ||
            t.contains("failed to generate") || t.contains("permission_denied") ||
            t.contains("firestore") || t.contains("com.google.") ||
            t.contains("socket") || t.contains("timeout") || t.contains("exception")
        return if (looksTechnical) fallback else m
    }
}
