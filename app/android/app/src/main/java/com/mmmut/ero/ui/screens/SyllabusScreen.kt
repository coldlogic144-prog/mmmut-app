package com.mmmut.ero.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.SyllabusViewModel

@Composable
fun SyllabusScreen(
    vm: SyllabusViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(16.dp)) {
                Text("Course Syllabus Tracker", style = MaterialTheme.typography.titleLarge)
                Text("Official MMMUT module & unit syllabus tracker.", style = MaterialTheme.typography.bodySmall)
            }
        }

        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No syllabus data available.")
            is UiState.Success -> {
                val data = s.data

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("${data.branchName} Overall Syllabus Progress", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${data.totalCompleted} of ${data.totalTopics} topics completed (${String.format("%.1f", data.overallPercent)}%)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (data.overallPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    items(data.subjectSyllabi, key = { it.subjectCode }) { sub ->
                        var expanded by remember { mutableStateOf(false) }
                        var expandedUnit by remember { mutableStateOf<String?>(null) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("${sub.subjectCode} — ${sub.subjectName}", style = MaterialTheme.typography.titleSmall)
                                        Text("${sub.category} · Credits: ${sub.credits} (${sub.ltp})", style = MaterialTheme.typography.labelSmall)
                                        Text("${sub.completedTopics} / ${sub.totalTopics} topics completed (${String.format("%.0f", sub.percent)}%)", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Expand"
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (sub.percent / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AnimatedVisibility(visible = expanded) {
                                    Column(
                                        Modifier
                                            .padding(top = 12.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HorizontalDivider()

                                        sub.units.forEach { unit ->
                                            val isUnitOpen = expandedUnit == unit.unitNumber
                                            Column(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp)
                                            ) {
                                                Surface(
                                                    onClick = {
                                                        expandedUnit = if (isUnitOpen) null else unit.unitNumber
                                                    },
                                                    color = if (isUnitOpen) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                                    shape = MaterialTheme.shapes.small,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "${unit.unitNumber}: ${unit.unitTitle} (${unit.completedCount}/${unit.totalCount})",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Icon(
                                                            if (isUnitOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                            contentDescription = if (isUnitOpen) "Collapse Module" else "Expand Module",
                                                            modifier = Modifier.size(20.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                if (isUnitOpen) {
                                                    Column(
                                                        Modifier
                                                            .padding(start = 8.dp, top = 4.dp, end = 8.dp)
                                                            .fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        unit.topics.forEach { topic ->
                                                            Row(
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable { vm.toggleTopic(sub.subjectCode, topic.key, !topic.completed) },
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Checkbox(
                                                                    checked = topic.completed,
                                                                    onCheckedChange = { chk ->
                                                                        vm.toggleTopic(sub.subjectCode, topic.key, chk)
                                                                    }
                                                                )
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(topic.name, style = MaterialTheme.typography.bodySmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
