package com.mmmut.ero.notifications

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mmmut.ero.data.model.StoredNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.histStore by preferencesDataStore("mmmut_notifications")

class NotificationHistoryStore(private val ctx: Context) {
    private val KEY = stringPreferencesKey("history_json")

    suspend fun append(n: StoredNotification) {
        val cur = all()
        val next = (listOf(n) + cur).take(100)
        ctx.histStore.edit { prefs ->
            val arr = JSONArray()
            next.forEach {
                arr.put(JSONObject().put("id", it.id).put("type", it.type)
                    .put("title", it.title).put("body", it.body)
                    .put("refId", it.refId).put("receivedAt", it.receivedAt))
            }
            prefs[KEY] = arr.toString()
        }
    }

    suspend fun all(): List<StoredNotification> {
        val raw = ctx.histStore.data.map { it[KEY] ?: "[]" }.first()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StoredNotification(o.optString("id"), o.optString("type"),
                    o.optString("title"), o.optString("body"),
                    o.optString("refId"), o.optLong("receivedAt"))
            }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun clear() { ctx.histStore.edit { it.remove(KEY) } }
}
