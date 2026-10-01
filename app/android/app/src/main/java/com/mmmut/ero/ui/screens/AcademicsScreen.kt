package com.mmmut.ero.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.AcademicsViewModel
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo
import com.mmmut.ero.util.SubjectAttendanceStats

@Composable
fun AcademicsScreen(vm: AcademicsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            listOf("Attendance", "Timetable", "Subjects", "Exams", "Results", "Calendar").forEachIndexed { i, t ->
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
                    0 -> { // Attendance Redesign
                        val (p, a, pct) = AttendanceUtils.summarize(d.map)
                        val total = p + a
                        val leave = AttendanceUtils.computeLeaveInfo(p, a, 75.0)
                        val subjectStats = AttendanceUtils.subjectBreakdown(d.map, branch.subjects)
                        var detailSubject by remember { mutableStateOf<SubjectAttendanceStats?>(null) }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    colors = if (pct < 75.0 && total > 0) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Overall Attendance Summary", style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${String.format("%.1f", pct)}%", style = MaterialTheme.typography.headlineLarge)
                                            Text("P: $p · A: $a · Total: $total", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        if (pct < 75.0 && total > 0) {
                                            Text(
                                                "⚠️ Low Attendance Warning: Your attendance is below 75%.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Text(
                                            when (leave) {
                                                is LeaveInfo.CanSkip -> "You can skip next ${leave.count} classes while staying >= 75%."
                                                is LeaveInfo.MustAttend -> "You must attend next ${leave.count} classes to reach 75% target."
                                                is LeaveInfo.None -> "Record class attendance to view projections."
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            item {
                                Text("Today's Daily Logger", style = MaterialTheme.typography.titleSmall)
                            }

                            val todayName = com.mmmut.ero.util.TimeUtils.todayName()
                            val todayDateKey = com.mmmut.ero.util.TimeUtils.dateKey()
                            val todayCells = d.week[todayName] ?: emptyList()
                            val todayMap = d.map[todayDateKey] ?: emptyMap()

                            if (todayCells.none { it.subjectCode != "—" }) {
                                item { Text("No classes scheduled for today.", style = MaterialTheme.typography.bodySmall) }
                            } else {
                                items(todayCells.filter { it.subjectCode != "—" }, key = { it.periodKey }) { cell ->
                                    val key = "${cell.periodKey}::${cell.subjectCode}"
                                    val currentStatus = todayMap[key]

                                    Card(Modifier.fillMaxWidth()) {
                                        Row(
                                            Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Period ${cell.periodKey} (${cell.start} - ${cell.end})", style = MaterialTheme.typography.labelSmall)
                                                Text("${cell.subjectCode} — ${cell.subjectName}", style = MaterialTheme.typography.titleSmall)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                FilterChip(
                                                    selected = currentStatus == "present",
                                                    onClick = { vm.setAttendance(cell.periodKey, cell.subjectCode, "present") },
                                                    label = { Text("Present") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )
                                                FilterChip(
                                                    selected = currentStatus == "absent",
                                                    onClick = { vm.setAttendance(cell.periodKey, cell.subjectCode, "absent") },
                                                    label = { Text("Absent") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.error,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onError
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Subject Attendance Breakdown", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                            }

                            items(subjectStats, key = { it.subjectCode }) { subStat ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { detailSubject = subStat }
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text("${subStat.subjectCode} — ${subStat.subjectName}", style = MaterialTheme.typography.titleSmall)
                                                Text("Attended ${subStat.present} of ${subStat.total} classes (${String.format("%.1f", subStat.pct)}%)", style = MaterialTheme.typography.bodySmall)
                                            }
                                            AssistChip(
                                                onClick = { detailSubject = subStat },
                                                label = { Text(subStat.status) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    labelColor = if (subStat.pct >= 75.0 || subStat.total == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { (subStat.pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        detailSubject?.let { subStat ->
                            val history = AttendanceUtils.historyForSubject(d.map, subStat.subjectCode)
                            val subLeave = AttendanceUtils.computeLeaveInfo(subStat.present, subStat.absent, 75.0)

                            AlertDialog(
                                onDismissRequest = { detailSubject = null },
                                title = { Text("${subStat.subjectCode} — ${subStat.subjectName}") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Overall: ${String.format("%.1f", subStat.pct)}% (${subStat.present}/${subStat.total} attended)")
                                        Text(
                                            when (subLeave) {
                                                is LeaveInfo.CanSkip -> "You can skip ${subLeave.count} classes for this subject."
                                                is LeaveInfo.MustAttend -> "Must attend next ${subLeave.count} classes for this subject."
                                                is LeaveInfo.None -> "No attendance logged yet."
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        HorizontalDivider()
                                        Text("History Log:", style = MaterialTheme.typography.labelMedium)
                                        if (history.isEmpty()) {
                                            Text("No logs recorded.", style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                                items(history) { (date, period, status) ->
                                                    Row(
                                                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("$date ${if (period.isNotBlank()) "(P-$period)" else ""}", style = MaterialTheme.typography.bodySmall)
                                                        Text(
                                                            status.uppercase(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (status == "present") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { detailSubject = null }) { Text("Close") }
                                }
                            )
                        }
                    }

                    1 -> LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        d.week.forEach { (day, cells) ->
                            item { Text(day, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(8.dp)) }
                            items(cells) { c -> Text("${c.periodKey} ${c.start}-${c.end} · ${c.subjectCode} ${c.subjectName} [${c.type}]", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }
                        }
                    }

                    2 -> LazyColumn { items(branch.subjects) { sub ->
                        ListItem(headlineContent = { Text("${sub.code} — ${sub.name}") }, supportingContent = { Text("L${sub.l} T${sub.t} P${sub.p} · ${branch.room}") })
                        HorizontalDivider()
                    } }

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
