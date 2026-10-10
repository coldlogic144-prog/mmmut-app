package com.mmmut.ero

import android.app.Application
import com.mmmut.ero.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmmutApp : Application() {
    companion object {
        lateinit var instance: MmmutApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.mmmut.ero.data.local.SyllabusData.init(this)
        CoroutineScope(Dispatchers.Default).launch {
            NotificationChannels.createAll(this@MmmutApp)
        }
    }
}
