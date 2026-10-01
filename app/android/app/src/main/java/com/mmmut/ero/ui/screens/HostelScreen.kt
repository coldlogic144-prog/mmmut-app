package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.HostelViewModel

@Composable
fun HostelScreen(onOpen: (String) -> Unit, vm: HostelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Hostel", style = MaterialTheme.typography.titleLarge)
                Text("Allotment, mess timings and warden notices are published here by the university. Your profile hostel preference is shown on the Profile tab.", style = MaterialTheme.typography.bodySmall)
            }
        }
        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No hostel announcements yet.")
            is UiState.Success -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                if (s.data.isEmpty()) item { Text("No hostel announcements yet.", modifier = Modifier.padding(16.dp)) }
                items(s.data, key = { it.id }) { n -> NoticeCard(n) { onOpen(n.id) } }
            }
        }
    }
}
