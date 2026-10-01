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
import com.mmmut.ero.ui.viewmodel.NoticesViewModel

@Composable
fun NoticesScreen(onOpen: (String) -> Unit, vm: NoticesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("all") }
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(query, { query = it }, label = { Text("Search notices") }, modifier = Modifier.fillMaxWidth().padding(12.dp), singleLine = true)
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all", "general", "academic", "examination", "hostel", "events", "emergency").forEach { c ->
                FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
            }
        }
        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No announcements yet.")
            is UiState.Success -> {
                val filtered = s.data.filter {
                    (cat == "all" || it.category == cat || (cat == "examination" && it.category == "exam")) &&
                    (query.isBlank() || it.title.contains(query, true) || it.content.contains(query, true))
                }
                if (filtered.isEmpty()) EmptyView("No notices match your filters.")
                else LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(filtered, key = { it.id }) { n -> NoticeCard(n) { onOpen(n.id) } }
                }
            }
        }
    }
}
