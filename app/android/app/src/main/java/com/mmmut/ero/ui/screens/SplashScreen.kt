package com.mmmut.ero.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.AuthViewModel

@Composable
fun SplashScreen(onSession: (Boolean) -> Unit, vm: AuthViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.checkSession { onSession(it != null) } }
    LoadingView("MMMUT Student…")
}
