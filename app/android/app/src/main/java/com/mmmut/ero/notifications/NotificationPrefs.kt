package com.mmmut.ero.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefsStore by preferencesDataStore("mmmut_notif_prefs")

class NotificationPrefs(private val ctx: Context) {
    private fun key(type: String) = booleanPreferencesKey("notif_$type")
    val types = listOf("general", "academic", "examination", "hostel", "events", "emergency")
    fun enabledFlow(type: String): Flow<Boolean> =
        ctx.prefsStore.data.map { it[key(type)] ?: true }
    suspend fun setEnabled(type: String, enabled: Boolean) {
        ctx.prefsStore.edit { it[key(type)] = enabled }
    }
    suspend fun isEnabled(type: String): Boolean =
        try { ctx.prefsStore.data.map { it[key(type)] ?: true }.first() } catch (_: Exception) { true }
}
