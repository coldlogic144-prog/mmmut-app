package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.HomeViewModel
import com.mmmut.ero.util.TimeUtils

@Composable
fun HomeScreen(
    onNotices: () -> Unit, onNotice: (String) -> Unit, onAcademics: () -> Unit,
    onProfile: () -> Unit, onHostel: () -> Unit, onNotifications: () -> Unit,
    onTelegram: () -> Unit,
    vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    when (val s = state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("Nothing here yet.")
        is UiState.Success -> {
            val d = s.data
            val branch = AcademicDataExtra.getBranch(d.profile.branchId)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                item {
                    Card(Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("MADAN MOHAN MALAVIYA UNIVERSITY OF TECHNOLOGY", style = MaterialTheme.typography.labelSmall)
                            Text("${TimeUtils.greeting()}, ${d.profile.name.ifBlank { d.profile.username }}", style = MaterialTheme.typography.titleLarge)
                            Text("${branch.name} · Sec ${d.profile.section} · Sem 1", style = MaterialTheme.typography.bodySmall)
                            if (d.profile.rollNumber.isNotBlank()) Text("Roll: ${d.profile.rollNumber}", style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.padding(top = 8.dp)) {
                                Button(onClick = onNotifications) { Text("Notifications") }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = onProfile) { Text("Profile") }
                            }
                        }
                    }
                }
                item { SectionHeader("Today's timetable", "Full week") { onAcademics() } }
                if (d.today.isEmpty()) item { Text("No classes today.", modifier = Modifier.padding(horizontal = 16.dp)) }
                else items(d.today.take(4)) { c ->
                    ListItem(headlineContent = { Text("${c.periodKey} · ${c.subjectCode}") }, supportingContent = { Text("${c.subjectName} (${c.start}-${c.end})") })
                    HorizontalDivider()
                }
                item {
                    SectionHeader("Attendance", "Details") { onAcademics() }
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Present ${d.present} · Absent ${d.absent} · ${String.format("%.1f", d.pct)}%", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(progress = { (d.pct / 100).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        }
                    }
                }
                item { SectionHeader("Important notices", "View all") { onNotices() } }
                items(d.notices) { n -> NoticeCard(n) { onNotice(n.id) } }
                item {
                    SectionHeader("Quick actions")
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onAcademics, modifier = Modifier.weight(1f)) { Text("Academics") }
                            OutlinedButton(onClick = onHostel, modifier = Modifier.weight(1f)) { Text("Hostel") }
                            OutlinedButton(onClick = onNotices, modifier = Modifier.weight(1f)) { Text("Notices") }
                        }
                        OutlinedButton(onClick = onTelegram, modifier = Modifier.fillMaxWidth()) { Text("Telegram Access") }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
