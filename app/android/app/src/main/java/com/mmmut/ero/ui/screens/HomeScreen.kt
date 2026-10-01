package com.mmmut.ero.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.HomeViewModel
import com.mmmut.ero.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNotices: () -> Unit,
    onNotice: (String) -> Unit,
    onAcademics: () -> Unit,
    onProfile: () -> Unit,
    onHostel: () -> Unit,
    onNotifications: () -> Unit,
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
            val p = d.profile
            val branch = AcademicDataExtra.getBranch(p.branchId)

            val nowTimeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            val currentCell = com.mmmut.ero.data.local.ScheduleEngine.getCurrentClass(d.todayCells, nowTimeStr)
            val nextCell = com.mmmut.ero.data.local.ScheduleEngine.getNextClass(d.todayCells, nowTimeStr)
            val activeOrNext = currentCell ?: nextCell

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Top Header Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "MADAN MOHAN MALAVIYA UNIVERSITY OF TECHNOLOGY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${TimeUtils.greeting()}, ${p.name.ifBlank { p.username }}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${branch.name} · Sem ${p.semester} · Sec ${p.section}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Group ${p.tutorialGroup} (Tut) · Group ${p.practicalGroup} (Prac) · ${p.hostel}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (p.rollNumber.isNotBlank()) {
                                Text("Roll No: ${p.rollNumber}", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onNotifications) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Alerts (${d.unreadNoticeCount})")
                                }
                                OutlinedButton(onClick = onProfile) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Profile")
                                }
                            }
                        }
                    }
                }

                // Current / Next Class Indicator
                if (activeOrNext != null) {
                    item {
                        Card(
                            colors = if (currentCell != null) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (currentCell != null) "CLASS IN PROGRESS NOW" else "NEXT UPCOMING CLASS TODAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentCell != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Text("${activeOrNext.subjectCode} — ${activeOrNext.subjectName}", style = MaterialTheme.typography.titleSmall)
                                    val groupTag = activeOrNext.practicalGroup?.let { " · Group $it" } ?: activeOrNext.tutorialGroup?.let { " · Group $it" } ?: ""
                                    val classRoom = activeOrNext.room.ifBlank { branch.room }
                                    Text("Period ${activeOrNext.periodKey} (${activeOrNext.start} - ${activeOrNext.end}) · ${activeOrNext.type}${groupTag} · Room $classRoom", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Today's Timetable Section
                item {
                    SectionHeader("Today's Schedule", "Full Timetable") { onAcademics() }
                }

                if (d.todayCells.isEmpty()) {
                    item {
                        Text(
                            "No academic classes scheduled for today.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(d.todayCells, key = { it.id.ifBlank { it.periodKey + (it.tutorialGroup ?: "") + (it.practicalGroup ?: "") } }) { cell ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onAcademics() }
                        ) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Period ${cell.periodKey}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        Text("•", style = MaterialTheme.typography.labelSmall)
                                        Text("${cell.start} - ${cell.end}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Text(cell.subjectName, style = MaterialTheme.typography.titleSmall)
                                    if (cell.subjectCode != "—") {
                                        val groupTag = cell.practicalGroup?.let { " · Group $it" } ?: cell.tutorialGroup?.let { " · Group $it" } ?: ""
                                        val classRoom = cell.room.ifBlank { branch.room }
                                        Text("${cell.subjectCode} · ${cell.type}${groupTag} · Room $classRoom", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Important Notices
                item {
                    SectionHeader("Important Notices", "View All") { onNotices() }
                }

                if (d.notices.isEmpty()) {
                    item {
                        Text(
                            "No notices posted.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    items(d.notices.take(3)) { n ->
                        NoticeCard(n) { onNotice(n.id) }
                    }
                }

                // Quick Actions
                item {
                    SectionHeader("ERP Services")
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = onAcademics, modifier = Modifier.weight(1f)) {
                                Text("Timetable")
                            }
                            FilledTonalButton(onClick = onHostel, modifier = Modifier.weight(1f)) {
                                Text("Hostel")
                            }
                            FilledTonalButton(onClick = onNotices, modifier = Modifier.weight(1f)) {
                                Text("Notices")
                            }
                        }
                        Button(onClick = onTelegram, modifier = Modifier.fillMaxWidth()) {
                            Text("Telegram Private Channel Access")
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
