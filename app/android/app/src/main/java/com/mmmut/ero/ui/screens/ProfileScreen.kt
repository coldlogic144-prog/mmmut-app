package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onVerifyRoll: () -> Unit,
    onTelegram: () -> Unit,
    vm: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    when (val s = state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("No profile.")
        is UiState.Success -> {
            val p = s.data
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text(p.name.ifBlank { p.username }, style = MaterialTheme.typography.titleLarge)
                Text("@${p.username}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Roll: ${p.rollNumber.ifBlank { "Not verified" }}")
                Text("Branch: ${p.branchId} · Sec ${p.section}")
                Text("Hostel: ${p.hostel} · ${p.gender}")
                Text("Status: ${p.migrationStatus}")
                Spacer(Modifier.height(12.dp))
                if (!p.rollNumberVerified) {
                    OutlinedButton(onClick = onVerifyRoll, modifier = Modifier.fillMaxWidth()) {
                        Text("Link my roll number")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text("Services & Settings", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onTelegram, modifier = Modifier.fillMaxWidth()) {
                    Text("Telegram Private Access")
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { vm.logout(onLoggedOut) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Logout")
                }
            }
        }
    }
}
