package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.AdminRequest
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* ==========================================================================
 * Admin dashboard
 * --------------------------------------------------------------------------
 * Authorization is unchanged and stays where it was: `load()` refuses to
 * publish any data unless the signed-in profile has isAdmin == true. The
 * screen renders a second, defensive check from `isAdmin` in this state so a
 * routing mistake can never expose the console.
 *
 * Only repositories that already existed are called — no new Firestore reads,
 * writes or rules are introduced.
 * ========================================================================== */

data class AdminScreenData(
    val profile: StudentProfile,
    val telegramApps: List<TelegramApplicationWithUser>,
    val pendingRolls: List<StudentProfile>,
    val adminRequests: List<AdminRequest>,
    val notices: List<Notice>
) {
    val isAdmin: Boolean get() = profile.isAdmin

    /**
     * Applications genuinely awaiting a decision. The repository returns every
     * telegramApplications document, so the console must filter for the pending
     * status rather than assuming the list is already the queue.
     */
    val pendingTelegramApps: List<TelegramApplicationWithUser>
        get() = telegramApps.filter { it.status == "PENDING_ADMIN_APPROVAL" }

    val decidedTelegramApps: List<TelegramApplicationWithUser>
        get() = telegramApps.filter { it.status != "PENDING_ADMIN_APPROVAL" }
}

/** Sections of the console. Kept as an enum so the screen cannot drift. */
enum class AdminSection { TELEGRAM, ROLL_VERIFY, PUBLISH_NOTICE, ADMIN_REQUESTS }

/** Filter chips over the Telegram queue. */
enum class TelegramQueueFilter { PENDING, ALL, DECIDED }

class AdminViewModel(
    private val authRepo: AuthRepository = FirebaseAuthRepository(),
    private val adminRepo: AdminRepository = FirebaseAdminRepository(),
    private val noticeRepo: NoticeRepository = FirestoreNoticeRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<AdminScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<AdminScreenData>> = _state

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val profile = try { authRepo.currentProfile() } catch (_: Exception) { null }
            if (profile == null) {
                _state.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }
            // Unchanged authorization gate.
            if (!profile.isAdmin) {
                _state.value = UiState.Error("Access Denied. You do not have administrator permissions.")
                return@launch
            }

            try {
                val tg = when (val r = adminRepo.getTelegramApplications()) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> emptyList()
                }
                val rolls = when (val r = adminRepo.getPendingRollVerifications()) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> emptyList()
                }
                val reqs = when (val r = adminRepo.getAdminRequests()) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> emptyList()
                }
                val nts = when (val r = noticeRepo.notices()) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> emptyList()
                }
                _state.value = UiState.Success(AdminScreenData(profile, tg, rolls, reqs, nts))
            } catch (_: Exception) {
                _state.value = UiState.Error(
                    "Could not load the admin dashboard. Check your connection and retry."
                )
            }
        }
    }

    fun approveTelegram(uid: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.approveTelegramApplication(uid)) {
                is RepoResult.Ok -> { _msg.value = "Telegram application approved."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not approve that application. Try again.")
            }
            _busy.value = false
        }
    }

    fun rejectTelegram(uid: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.rejectTelegramApplication(uid)) {
                is RepoResult.Ok -> { _msg.value = "Telegram application rejected."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not reject that application. Try again.")
            }
            _busy.value = false
        }
    }

    fun approveRoll(uid: String, roll: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.approveRollVerification(uid, roll)) {
                is RepoResult.Ok -> { _msg.value = "Roll verification approved."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not approve that roll number. Try again.")
            }
            _busy.value = false
        }
    }

    fun approveAdminRequest(requestId: String, targetUid: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.approveAdminRequest(requestId, targetUid)) {
                is RepoResult.Ok -> { _msg.value = "Admin request approved."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not approve that admin request. Try again.")
            }
            _busy.value = false
        }
    }

    fun createNotice(title: String, content: String, category: String, important: Boolean, pinned: Boolean) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.createNotice(title, content, category, important, pinned)) {
                is RepoResult.Ok -> { _msg.value = "Notice published successfully."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not publish the notice. Try again.")
            }
            _busy.value = false
        }
    }

    fun deleteNotice(id: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val r = adminRepo.deleteNotice(id)) {
                is RepoResult.Ok -> { _msg.value = "Notice deleted."; load() }
                is RepoResult.Err -> _msg.value = sanitize(r.message, "Could not delete the notice. Try again.")
            }
            _busy.value = false
        }
    }

    fun clearMsg() { _msg.value = null }

    /**
     * Repository failure strings are built from raw exceptions (they append the
     * thrown message). Those carry Firestore paths and HTTP detail, so any such
     * string is replaced with a plain sentence before reaching the screen.
     */
    private fun sanitize(message: String?, fallback: String): String {
        val m = message?.trim().orEmpty()
        if (m.isEmpty()) return fallback
        val t = m.lowercase()
        val looksTechnical = t.contains("could not ") && t.contains(":") ||
            t.contains("failed to approve") || t.contains("failed to reject") ||
            t.contains("permission_denied") || t.contains("firestore") ||
            t.contains("com.google.") || t.contains("http ") ||
            t.contains("network error") || t.contains("exception")
        return if (looksTechnical) fallback else m
    }
}
