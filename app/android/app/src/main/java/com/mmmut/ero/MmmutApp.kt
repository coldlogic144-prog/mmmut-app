package com.mmmut.ero

import android.app.Application
import com.mmmut.ero.notifications.NotificationChannels

class MmmutApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
    }
}
