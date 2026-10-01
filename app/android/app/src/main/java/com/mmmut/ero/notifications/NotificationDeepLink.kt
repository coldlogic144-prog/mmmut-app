package com.mmmut.ero.notifications

import com.mmmut.ero.navigation.Routes

object NotificationDeepLink {
    fun routeFor(type: String, refId: String): String {
        val t = type.lowercase()
        return when {
            t == "examination" || t == "exam" -> Routes.ACADEMICS
            t == "hostel" && refId.isBlank() -> Routes.HOSTEL
            t == "events" || t == "event" -> if (refId.isBlank()) Routes.NOTICES else Routes.noticeDetail(refId)
            refId.isNotBlank() -> Routes.noticeDetail(refId)
            t == "hostel" -> Routes.HOSTEL
            else -> Routes.NOTICES
        }
    }

    fun parseUri(uri: String?): String? {
        if (uri == null) return null
        val schemeSplit = uri.split("://", limit = 2)
        if (schemeSplit.size != 2 || schemeSplit[0] != "mmmut") return null
        val rest = schemeSplit[1]
        val host = rest.substringBefore("/")
        val seg = rest.substringAfter("/", "").substringBefore("?").substringBefore("#")
        return when (host) {
            "notice" -> if (seg.isBlank()) Routes.NOTICES else Routes.noticeDetail(seg)
            "exam" -> Routes.ACADEMICS
            "event" -> if (seg.isBlank()) Routes.NOTICES else Routes.noticeDetail(seg)
            "hostel" -> Routes.HOSTEL
            else -> null
        }
    }
}
