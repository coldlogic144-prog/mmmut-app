package com.mmmut.ero.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.TimetableViewModel

@Composable
fun TimetableScreen(
    vm: TimetableViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf("Monday") }
    var detailCell by remember { mutableStateOf<TimetableCell?>(null) }
    var showGroupConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(16.dp)) {
                Text("Academic Timetable (Session 2025-26)", style = MaterialTheme.typography.titleLarge)
                Text("Official timetable schedule for lectures, tutorials, and practical labs.", style = MaterialTheme.typography.bodySmall)
            }
        }

        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No timetable available.")
            is UiState.Success -> {
                val data = s.data
                LaunchedEffect(data.todayName) { selectedDay = data.todayName }

                // Group Configuration Header Banner
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Tut: ${data.profile.tutorialGroup} · Prac: ${data.profile.practicalGroup} · Sec ${data.profile.section}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        FilledTonalButton(onClick = { showGroupConfigDialog = true }) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Change Groups")
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Today") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Full Week") })
                }

                // Next Class Banner / Card
                val next = data.currentCell ?: data.nextCell
                if (next != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (data.currentCell != null) "ONGOING NOW IN CLASS" else "NEXT UPCOMING CLASS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("${next.subjectCode} — ${next.subjectName}", style = MaterialTheme.typography.titleSmall)
                                Text("Period ${next.periodKey} (${next.start} - ${next.end}) · Room ${data.room}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (selectedTab == 0) { // Today View
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "${data.branchName} · Sem ${data.profile.semester} · Sec ${data.profile.section} (${data.todayName})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        items(data.todayCells, key = { it.periodKey + (it.tutorialGroup ?: "") + (it.practicalGroup ?: "") }) { cell ->
                            val isCurrent = data.currentCell?.periodKey == cell.periodKey

                            if (cell.periodKey == "V" && data.todayCells.any { it.periodKey == "IV" }) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                        Text("🍱 LUNCH BREAK · 12:30 PM - 02:00 PM", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            TimetableClassCard(
                                cell = cell,
                                room = data.room,
                                isCurrent = isCurrent,
                                onClick = { detailCell = cell }
                            )
                        }
                    }
                } else { // Full Week View
                    Column(Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = AcademicData.DAYS.indexOf(selectedDay).coerceAtLeast(0),
                            edgePadding = 16.dp
                        ) {
                            AcademicData.DAYS.forEach { day ->
                                Tab(
                                    selected = selectedDay == day,
                                    onClick = { selectedDay = day },
                                    text = { Text(day) }
                                )
                            }
                        }

                        val dayCells = data.weekGrid[selectedDay] ?: emptyList()

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(dayCells, key = { it.periodKey + (it.tutorialGroup ?: "") + (it.practicalGroup ?: "") }) { cell ->
                                if (cell.periodKey == "V" && dayCells.any { it.periodKey == "IV" }) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                            Text("🍱 LUNCH BREAK · 12:30 PM - 02:00 PM", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }

                                TimetableClassCard(
                                    cell = cell,
                                    room = data.room,
                                    isCurrent = false,
                                    onClick = { detailCell = cell }
                                )
                            }
                        }
                    }
                }

                // Group Config Dialog
                if (showGroupConfigDialog) {
                    var tempTut by remember { mutableStateOf(data.profile.tutorialGroup) }
                    var tempPrac by remember { mutableStateOf(data.profile.practicalGroup) }

                    AlertDialog(
                        onDismissRequest = { showGroupConfigDialog = false },
                        title = { Text("Configure Timetable Groups") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Your timetable may contain different parallel classes for different groups.", style = MaterialTheme.typography.bodySmall)

                                Text("Tutorial Group", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("T1", "T2", "N/A").forEach { tg ->
                                        FilterChip(
                                            selected = tempTut == tg,
                                            onClick = {
                                                tempTut = tg
                                                if (tg == "T1") tempPrac = "P1"
                                                if (tg == "T2") tempPrac = "P2"
                                            },
                                            label = { Text("Group $tg") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                Text("Practical Group", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("P1", "P2", "N/A").forEach { pg ->
                                        FilterChip(
                                            selected = tempPrac == pg,
                                            onClick = {
                                                tempPrac = pg
                                                if (pg == "P1") tempTut = "T1"
                                                if (pg == "P2") tempTut = "T2"
                                            },
                                            label = { Text("Group $pg") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                vm.updateGroups(tempTut, tempPrac)
                                showGroupConfigDialog = false
                            }) {
                                Text("Save & Apply")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showGroupConfigDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                // Detail Dialog
                detailCell?.let { cell ->
                    AlertDialog(
                        onDismissRequest = { detailCell = null },
                        title = { Text("${cell.subjectCode} — ${cell.subjectName}") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Period: ${cell.periodKey} (${cell.start} - ${cell.end})")
                                Text("Class Type: ${cell.type}")
                                Text("Room: ${data.room}")
                                Text("Branch: ${data.branchName}")
                                Text("Semester: Semester ${data.profile.semester}")
                                Text("Section: Section ${data.profile.section}")
                                if (cell.tutorialGroup != null) Text("Tutorial Group: ${cell.tutorialGroup}")
                                if (cell.practicalGroup != null) Text("Practical Group: ${cell.practicalGroup}")
                                Text("Academic Session: 2025-26")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { detailCell = null }) { Text("Close") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableClassCard(
    cell: TimetableCell,
    room: String,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = if (isCurrent) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Period ${cell.periodKey}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("•", style = MaterialTheme.typography.labelSmall)
                    Text("${cell.start} - ${cell.end}", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(2.dp))
                Text(cell.subjectName, style = MaterialTheme.typography.titleSmall)

                if (cell.subjectCode != "—") {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text(cell.subjectCode) }
                        )
                        AssistChip(
                            onClick = onClick,
                            label = { Text(cell.type) }
                        )
                        if (cell.tutorialGroup != null) {
                            AssistChip(
                                onClick = onClick,
                                label = { Text("Group ${cell.tutorialGroup}") }
                            )
                        }
                        if (cell.practicalGroup != null) {
                            AssistChip(
                                onClick = onClick,
                                label = { Text("Group ${cell.practicalGroup}") }
                            )
                        }
                        Text("Room $room", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Info, contentDescription = "Details")
            }
        }
    }
}
