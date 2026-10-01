package com.mmmut.ero.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    fun dateKey(d: Date = Date()): String = dayFmt.format(d)
    fun todayName(): String? = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"; Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"; Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"; else -> null
    }
    fun greeting(): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (h) { in 0..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    }
    fun formatMillis(millis: Long): String {
        if (millis <= 0) return "—"
        return SimpleDateFormat("dd MMM yyyy", Locale("en", "IN")).format(Date(millis))
    }
}
