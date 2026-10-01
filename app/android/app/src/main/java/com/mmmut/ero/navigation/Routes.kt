package com.mmmut.ero.navigation

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val VERIFY_ROLL = "verify_roll"
    const val HOME = "home"
    const val ACADEMICS = "academics"
    const val NOTICES = "notices"
    const val NOTICE_DETAIL = "notice/{noticeId}"
    const val PROFILE = "profile"
    const val HOSTEL = "hostel"
    const val NOTIFICATIONS = "notifications"
    const val TELEGRAM = "telegram"

    fun noticeDetail(id: String) = "notice/$id"
}
