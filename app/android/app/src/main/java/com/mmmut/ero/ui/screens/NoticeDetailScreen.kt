package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.NoticeDetailViewModel
import com.mmmut.ero.util.TimeUtils

@Composable
fun NoticeDetailScreen(id: String, vm: NoticeDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    val uri = LocalUriHandler.current
    LaunchedEffect(id) { vm.load(id) }
    when (val s = state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(s.message) { vm.load(id) }
        is UiState.Empty -> EmptyView("Notice not found.")
        is UiState.Success -> {
            val n = s.data
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                if (n.important) AssistChip(onClick = {}, label = { Text("Urgent / Important") })
                Text(n.title, style = MaterialTheme.typography.titleLarge)
                Text("${n.category.uppercase()} · ${TimeUtils.formatMillis(n.createdAtMillis)}", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                Text(n.content, style = MaterialTheme.typography.bodyMedium)
                if (n.linkUrl.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { try { uri.openUri(n.linkUrl) } catch (_: Exception) { } }) { Text("Open attachment / link") }
                }
            }
        }
    }
}
