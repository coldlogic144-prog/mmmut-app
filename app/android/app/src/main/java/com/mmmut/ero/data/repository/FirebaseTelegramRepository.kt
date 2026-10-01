package com.mmmut.ero.data.repository

import com.mmmut.ero.core.BackendConfig
import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.model.TelegramApplication
import com.mmmut.ero.data.remote.MmmutApiClient
import com.mmmut.ero.data.remote.TelegramCheckMembershipReq
import com.mmmut.ero.data.remote.TelegramCreateTokenReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseTelegramRepository(
    private val apiBaseProvider: () -> String = { BackendConfig.API_BASE_URL }
) : TelegramRepository {
    private val db get() = FirebaseProvider.firestore

    override suspend fun getApplication(uid: String): RepoResult<TelegramApplication?> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("telegramApplications").document(uid).get().await()
            if (!snap.exists()) return@withContext RepoResult.Ok(null)
            val data = snap.data ?: emptyMap()
            RepoResult.Ok(
                TelegramApplication(
                    status = data["status"] as? String ?: "NOT_APPLIED",
                    appliedAt = (data["appliedAt"] as? Number)?.toLong() ?: 0L,
                    telegramUserId = data["telegramUserId"] as? String,
                    telegramUsername = data["telegramUsername"] as? String,
                    isMember = data["isMember"] as? Boolean ?: false
                )
            )
        } catch (_: Exception) {
            RepoResult.Err("Could not load Telegram application status.")
        }
    }

    override suspend fun apply(uid: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = mapOf(
                "uid" to uid,
                "status" to "PENDING_ADMIN_APPROVAL",
                "appliedAt" to System.currentTimeMillis(),
                "telegramUserId" to null,
                "telegramUsername" to null
            )
            db.collection("telegramApplications").document(uid).set(doc).await()
            RepoResult.Ok(Unit)
        } catch (_: Exception) {
            RepoResult.Err("Could not submit Telegram application.")
        }
    }

    override suspend fun reapply(uid: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mapOf<String, Any?>(
                "status" to "PENDING_ADMIN_APPROVAL",
                "appliedAt" to System.currentTimeMillis()
            )
            db.collection("telegramApplications").document(uid).update(updates).await()
            RepoResult.Ok(Unit)
        } catch (_: Exception) {
            RepoResult.Err("Could not resubmit Telegram application.")
        }
    }

    override suspend fun createToken(uid: String): RepoResult<String> = withContext(Dispatchers.IO) {
        try {
            val res = MmmutApiClient.service.createTelegramToken(TelegramCreateTokenReq(uid))
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null && body.ok && !body.token.isNullOrBlank()) {
                    return@withContext RepoResult.Ok(body.token)
                }
                RepoResult.Err(body?.error ?: "Could not create Telegram token.")
            } else {
                RepoResult.Err("Failed to generate token (HTTP ${res.code()}).")
            }
        } catch (e: Exception) {
            RepoResult.Err("Network error creating Telegram link: ${e.message}")
        }
    }

    override suspend fun checkTokenStatus(token: String): RepoResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res = MmmutApiClient.service.getTelegramTokenStatus(token)
            if (res.isSuccessful) {
                val body = res.body()
                val linked = body != null && body.ok && body.status == "LINKED"
                RepoResult.Ok(linked)
            } else {
                RepoResult.Ok(false)
            }
        } catch (_: Exception) {
            RepoResult.Ok(false)
        }
    }

    override suspend fun saveHandle(uid: String, handle: String): RepoResult<Unit> = withContext(Dispatchers.IO) {
        val clean = handle.trim().removePrefix("@")
        if (clean.isBlank()) return@withContext RepoResult.Err("Enter a valid Telegram handle.")
        try {
            db.collection("telegramApplications").document(uid).update(
                mapOf(
                    "telegramUsername" to clean,
                    "telegramUserId" to clean
                )
            ).await()
            RepoResult.Ok(Unit)
        } catch (_: Exception) {
            RepoResult.Err("Could not save Telegram handle.")
        }
    }

    override suspend fun getInviteLink(): RepoResult<String> = withContext(Dispatchers.IO) {
        try {
            val res = MmmutApiClient.service.getTelegramChannelInvite()
            if (res.isSuccessful) {
                val body = res.body()
                val link = body?.inviteLink
                if (!link.isNullOrBlank()) RepoResult.Ok(link)
                else RepoResult.Err("Invite link not available.")
            } else {
                RepoResult.Err("Could not fetch channel invite link.")
            }
        } catch (e: Exception) {
            RepoResult.Err("Could not fetch invite link: ${e.message}")
        }
    }

    override suspend fun checkMembership(uid: String, telegramUserId: String?): RepoResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res = MmmutApiClient.service.checkTelegramMembership(TelegramCheckMembershipReq(uid, telegramUserId))
            if (res.isSuccessful) {
                val body = res.body()
                val isMem = body != null && body.ok && body.isMember
                if (isMem) {
                    try {
                        db.collection("telegramApplications").document(uid).update(
                            mapOf("status" to "CHANNEL_APPROVED", "isMember" to true)
                        ).await()
                    } catch (_: Exception) { }
                }
                RepoResult.Ok(isMem)
            } else {
                RepoResult.Ok(false)
            }
        } catch (_: Exception) {
            RepoResult.Ok(false)
        }
    }
}
