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
import com.mmmut.ero.ui.viewmodel.AcademicsViewModel
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo

@Composable
fun AcademicsScreen(vm: AcademicsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            listOf("Subjects", "Timetable", "Attendance", "Exams", "Results", "Calendar").forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, maxLines = 1) })
            }
        }
        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No academic data.")
            is UiState.Success -> {
                val d = s.data
                val branch = com.mmmut.ero.data.local.AcademicDataExtra.getBranch(d.profile.branchId)
                when (tab) {
                    0 -> LazyColumn { items(branch.subjects) { sub ->
                        ListItem(headlineContent = { Text("${sub.code} — ${sub.name}") }, supportingContent = { Text("L${sub.l} T${sub.t} P${sub.p} · ${branch.room}") })
                        HorizontalDivider()
                    } }
                    1 -> LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        d.week.forEach { (day, cells) ->
                            item { Text(day, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(8.dp)) }
                            items(cells) { c -> Text("${c.periodKey} ${c.start}-${c.end} · ${c.subjectCode} ${c.subjectName} [${c.type}]", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }
                        }
                    }
                    2 -> {
                        val (p, a, pct) = AttendanceUtils.summarize(d.map)
                        val leave = AttendanceUtils.computeLeaveInfo(p, a, 75.0)
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            item {
                                Text("Overall ${String.format("%.1f", pct)}% (P$p/A$a)", style = MaterialTheme.typography.titleMedium)
                                Text(when (leave) { is LeaveInfo.CanSkip -> "You can skip ${leave.count} classes (75% target)."; is LeaveInfo.MustAttend -> "Attend next ${leave.count} classes (75% target)."; is LeaveInfo.None -> "Mark attendance to see projections." }, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Text("Tap a period below to toggle present (today only).")
                            }
                            val today = com.mmmut.ero.util.TimeUtils.todayName()
                            val cells = d.week[today] ?: emptyList()
                            items(cells) { c ->
                                OutlinedButton(onClick = { vm.toggleAttendance(c.periodKey, c.subjectCode) {} }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text("${c.periodKey} · ${c.subjectCode}")
                                }
                            }
                        }
                    }
                    3 -> LazyColumn { items(d.exams) { e ->
                        ListItem(headlineContent = { Text(e.title) }, supportingContent = { Text("${e.date} · ${e.detail}") })
                        HorizontalDivider()
                    } }
                    4 -> if (d.results.isEmpty()) EmptyView("Results will appear here once published by the university.") else LazyColumn { items(d.results) { r ->
                        ListItem(headlineContent = { Text("${r.subjectCode} — ${r.grade}") }, supportingContent = { Text(r.subjectName) })
                        HorizontalDivider()
                    } }
                    5 -> LazyColumn { items(d.events) { e ->
                        ListItem(headlineContent = { Text(e.title) }, supportingContent = { Text("${e.start} → ${e.end} (${e.source})") })
                        HorizontalDivider()
                    } }
                }
            }
        }
    }
}
