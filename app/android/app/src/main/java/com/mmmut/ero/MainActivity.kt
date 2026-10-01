package com.mmmut.ero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mmmut.ero.navigation.AppNav
import com.mmmut.ero.ui.theme.MmmutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLink = intent?.data?.toString()
        setContent { MmmutTheme { AppNav(initialDeepLink = deepLink) } }
    }
}
