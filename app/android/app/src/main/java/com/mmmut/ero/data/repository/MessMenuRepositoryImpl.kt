package com.mmmut.ero.data.repository

import com.mmmut.ero.core.FirebaseProvider
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.data.local.MessMenuData
import com.mmmut.ero.data.model.MessDay
import com.mmmut.ero.data.model.MessWeeklyMenu
import com.mmmut.ero.data.remote.FirestoreCollections
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class MessMenuRepositoryImpl : MessMenuRepository {

    companion object {
        @Volatile
        private var cachedWeeklyMenu: MessWeeklyMenu? = null
        @Volatile
        private var lastFetchTime = 0L
        private const val CACHE_TTL_MS = 60 * 60 * 1000L // 1 hour memory cache to avoid unnecessary Firestore reads
    }

    override suspend fun getWeeklyMenu(): RepoResult<MessWeeklyMenu> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val inMemory = cachedWeeklyMenu
        if (inMemory != null && (now - lastFetchTime) < CACHE_TTL_MS) {
            return@withContext RepoResult.Ok(inMemory)
        }

        // Attempt to fetch from Firestore if configured, otherwise fallback to local source
        try {
            val docSnap = FirebaseProvider.firestore
                .collection(FirestoreCollections.MESS_MENU)
                .document("weeklyMenu")
                .get()
                .await()

            if (docSnap.exists()) {
                val remoteMenu = docSnap.toObject(MessWeeklyMenu::class.java)
                if (remoteMenu != null && remoteMenu.days.isNotEmpty()) {
                    cachedWeeklyMenu = remoteMenu
                    lastFetchTime = now
                    return@withContext RepoResult.Ok(remoteMenu)
                }
            }
        } catch (_: Exception) {
            // Silently fall back to official local menu on network error, offline, or missing collection
        }

        // Default centralized official menu
        val local = MessMenuData.DEFAULT_WEEKLY_MENU
        cachedWeeklyMenu = local
        lastFetchTime = now
        RepoResult.Ok(local)
    }

    override suspend fun getDayMenu(dayName: String): RepoResult<MessDay> = withContext(Dispatchers.IO) {
        when (val res = getWeeklyMenu()) {
            is RepoResult.Ok -> {
                val day = res.value.days.firstOrNull { it.day.equals(dayName, ignoreCase = true) }
                    ?: MessMenuData.getDay(dayName)
                RepoResult.Ok(day)
            }
            is RepoResult.Err -> RepoResult.Ok(MessMenuData.getDay(dayName))
        }
    }
}
