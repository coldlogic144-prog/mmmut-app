package com.mmmut.ero

import android.app.Application
import com.mmmut.ero.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmmutApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            NotificationChannels.createAll(this@MmmutApp)
        }
    }
}
