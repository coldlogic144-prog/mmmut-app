package com.mmmut.ero.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val GENERAL = "mmmut_general"
    const val ACADEMIC = "mmmut_academic"
    const val EXAMINATION = "mmmut_examination"
    const val HOSTEL = "mmmut_hostel"
    const val EVENTS = "mmmut_events"
    const val EMERGENCY = "mmmut_emergency"

    val ALL = listOf(GENERAL, ACADEMIC, EXAMINATION, HOSTEL, EVENTS, EMERGENCY)

    fun channelFor(type: String): String = when (type.lowercase()) {
        "academic" -> ACADEMIC
        "examination", "exam" -> EXAMINATION
        "hostel" -> HOSTEL
        "events", "event" -> EVENTS
        "emergency" -> EMERGENCY
        else -> GENERAL
    }

    fun createAll(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fun ch(id: String, name: String, imp: Int) =
            NotificationChannel(id, name, imp).apply { description = "MMMUT $name" }
        listOf(
            ch(GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT),
            ch(ACADEMIC, "Academic", NotificationManager.IMPORTANCE_DEFAULT),
            ch(EXAMINATION, "Examination", NotificationManager.IMPORTANCE_HIGH),
            ch(HOSTEL, "Hostel", NotificationManager.IMPORTANCE_DEFAULT),
            ch(EVENTS, "Events", NotificationManager.IMPORTANCE_DEFAULT),
            ch(EMERGENCY, "Emergency", NotificationManager.IMPORTANCE_HIGH)
        ).forEach { mgr.createNotificationChannel(it) }
    }
}
