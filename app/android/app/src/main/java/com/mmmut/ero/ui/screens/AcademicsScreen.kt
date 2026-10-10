package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.SectionHeader
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.AcademicsData
import com.mmmut.ero.ui.viewmodel.AcademicsViewModel
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo
import com.mmmut.ero.util.TimeUtils
import java.util.Locale
import com.mmmut.ero.ui.viewmodel.SyllabusData
import com.mmmut.ero.ui.viewmodel.SyllabusViewModel

/* ==========================================================================
 * ACADEMICS — The central educational hub
 * --------------------------------------------------------------------------
 * Previously "Academics" was a single giant screen carrying Timetable, Attendance,
 * Syllabus, Exams, Results, and Calendar all at once. Because the student
 * bottom bar was capped at 5 primary destinations (Home, Timetable, Attendance,
 * Academics, Profile), everything that does not need a dedicated daily bottom tab
 * is preserved as a hub destination rather than deleted:
 *
 *   Attendance  → daily class logger + overall summary. Marking a class is real
 *                 functionality, so it stays reachable.
 *   Syllabus    → real branch module & topic checklist and progress tracker.
 *   Subjects    → real branch subject definitions with L/T/P and credits.
 *   Timetable   → the real weekly timetable map.
 *   Exams       → Firestore calendar exams, with an honest empty state.
 *   Results     → Firestore published results, with an honest empty state.
 *   Calendar    → Firestore academic calendar events.
 *   Notices     → count + pointer to the Notices tab.
 *
 * Hub rows expand their real content inline (one section at a time, with a
 * labelled Back affordance) or navigate to dedicated views when appropriate.
 * ========================================================================== */

private enum class HubSection(val label: String, val description: String) {
    ATTENDANCE("Attendance & daily log", "Mark today's classes and review your overall percentage"),
    SYLLABUS("Syllabus", "Module-wise topics, unit breakdown, and completion tracker"),
    SUBJECTS("Subjects", "Registered subjects with credits and contact hours"),
    TIMETABLE("Timetable", "Weekly class schedule with rooms and groups"),
    EXAMS("Exams", "Scheduled examinations from the academic calendar"),
    RESULTS("Results", "Marks and grades published by the university"),
    CALENDAR("Calendar", "Academic events and important dates"),
    NOTICES("Notices", "Official announcements and circulars")
}

@Composable
fun AcademicsScreen(
    onSyllabus: (() -> Unit)? = null,
    vm: AcademicsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    sylVm: SyllabusViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    val sylState by sylVm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var section by remember { mutableStateOf<HubSection?>(null) }

    LaunchedEffect(section) {
        if (section == HubSection.SYLLABUS) {
            sylVm.load()
        }
    }

    var expandedSubjects by remember { mutableStateOf(emptySet<String>()) }
    var expandedModules by remember { mutableStateOf(emptySet<String>()) }

    when (val s = state) {
        is UiState.Loading -> ListSkeleton(rows = 5)
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("No academic data.")
        is UiState.Success -> {
            val d = s.data
            val branch = remember(d.profile.branchId) {
                AcademicDataExtra.getBranch(d.profile.branchId)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.screen,
                    end = Spacing.screen,
                    top = Spacing.sm,
                    bottom = Spacing.xxxl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
            ) {
                item(key = "acad-header") {
                    Column {
                        Text("Academics", style = MaterialTheme.typography.titleLarge, maxLines = 1)
                        Text(
                            "${branch.name} · Sem ${d.profile.semester} · Sec ${d.profile.section}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val active = section
                if (active == null) {
                    item(key = "hub-hint") {
                        Text(
                            "Everything academic in one place. Attendance also has its own tab in the bottom bar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(HubSection.entries.toList(), key = { "hub-${it.name}" }) { entry ->
                        HubRow(
                            entry = entry,
                            onClick = {
                                if (entry == HubSection.SYLLABUS && onSyllabus != null) {
                                    onSyllabus()
                                } else {
                                    section = entry
                                }
                            }
                        )
                    }
                } else {
                    item(key = "section-head") {
                        SectionBackHeader(
                            title = active.label,
                            subtitle = active.description,
                            onBack = { section = null }
                        )
                    }

                    when (active) {
                        HubSection.ATTENDANCE -> attendanceSection(d, branch, vm)
                        HubSection.SYLLABUS -> syllabusSection(
                            sylState = sylState,
                            sylVm = sylVm,
                            expandedSubjects = expandedSubjects,
                            expandedModules = expandedModules,
                            onToggleSubject = { code ->
                                expandedSubjects = if (expandedSubjects.contains(code)) {
                                    expandedSubjects - code
                                } else {
                                    expandedSubjects + code
                                }
                            },
                            onToggleModule = { modId ->
                                expandedModules = if (expandedModules.contains(modId)) {
                                    expandedModules - modId
                                } else {
                                    expandedModules + modId
                                }
                            },
                            onOpenFullScreen = onSyllabus
                        )
                        HubSection.SUBJECTS -> subjectsSection(d, branch)
                        HubSection.TIMETABLE -> timetableSection(d, branch)
                        HubSection.EXAMS -> examsSection(d)
                        HubSection.RESULTS -> resultsSection(d)
                        HubSection.CALENDAR -> calendarSection(d)
                        HubSection.NOTICES -> noticesSection(d)
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- rows ---- */

@Composable
private fun HubRow(entry: HubSection, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = MmmutDimens.minRowHeight)
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.card, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = iconFor(entry),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(Spacing.sm).size(MmmutDimens.iconLg)
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.label,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MmmutDimens.iconLg)
            )
        }
    }
}

private fun iconFor(entry: HubSection): ImageVector = when (entry) {
    HubSection.ATTENDANCE -> Icons.Default.DateRange
    HubSection.SYLLABUS -> Icons.AutoMirrored.Filled.MenuBook
    HubSection.SUBJECTS -> Icons.Default.Groups
    HubSection.TIMETABLE -> Icons.Default.Schedule
    HubSection.EXAMS -> Icons.Default.Assignment
    HubSection.RESULTS -> Icons.Default.Grade
    HubSection.CALENDAR -> Icons.Default.CalendarMonth
    HubSection.NOTICES -> Icons.Default.Notifications
}

@Composable
private fun SectionBackHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to academics",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ---------------------------------------------------------- attendance ---- */

private fun LazyListScope.attendanceSection(
    d: AcademicsData,
    branch: com.mmmut.ero.data.local.Branch,
    vm: AcademicsViewModel
) {
    item(key = "att-summary") {
        val summary = remember(d.map) { AttendanceUtils.summarize(d.map) }
        val present = summary.first
        val absent = summary.second
        val pct = summary.third
        val total = present + absent
        val hasRecords = total > 0
        val leave = remember(present, absent) {
            AttendanceUtils.computeLeaveInfo(present, absent, 75.0)
        }
        val statusColor = when {
            !hasRecords -> MaterialTheme.colorScheme.onSurfaceVariant
            pct >= 75.0 -> MaterialTheme.semantic.success
            pct >= 65.0 -> MaterialTheme.semantic.warning
            else -> MaterialTheme.semantic.danger
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(Spacing.card),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    "Overall attendance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (hasRecords) "${formatOneDecimal(pct)}%" else "—",
                    style = MaterialTheme.typography.headlineSmall,
                    color = statusColor
                )
                Text(
                    "Attended $present / Total $total",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (hasRecords) {
                        when (leave) {
                            is LeaveInfo.CanSkip -> "You can skip ${leave.count} classes and stay at or above 75%."
                            is LeaveInfo.MustAttend -> "You must attend the next ${leave.count} classes to reach 75%."
                            is LeaveInfo.None -> "Record class attendance to view projections."
                        }
                    } else {
                        "No classes marked yet. Mark today's classes below."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    item(key = "att-pointer") {
        Text(
            "Per-subject breakdown, history and projections are on the Attendance tab.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    item(key = "att-today-head") {
        SectionHeader("Today's class log")
    }

    val todayName = TimeUtils.todayName()
    val todayCells = (d.week[todayName] ?: emptyList()).filter { it.subjectCode != "—" }
    val todayMap = d.map[TimeUtils.dateKey()] ?: emptyMap()

    if (todayCells.isEmpty()) {
        item(key = "att-today-empty") {
            Text(
                "No classes scheduled for today.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        items(
            todayCells,
            key = { c -> "att-${c.id.ifBlank { c.periodKey + "::" + c.subjectCode }}" }
        ) { cell ->
            val status = todayMap["${cell.periodKey}::${cell.subjectCode}"]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        "Period ${cell.periodKey} · ${cell.start}–${cell.end}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${cell.subjectCode} — ${cell.subjectName}",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${cell.type} · Room ${cell.room.ifBlank { branch.room }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilterChip(
                            selected = status == "present",
                            onClick = { vm.setAttendance(cell.periodKey, cell.subjectCode, "present") },
                            label = { Text("Present") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(MmmutDimens.iconSm)
                                )
                            }
                        )
                        FilterChip(
                            selected = status == "absent",
                            onClick = { vm.setAttendance(cell.periodKey, cell.subjectCode, "absent") },
                            label = { Text("Absent") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(MmmutDimens.iconSm)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------ syllabus ---- */

private fun LazyListScope.syllabusSection(
    sylState: UiState<SyllabusData>,
    sylVm: SyllabusViewModel,
    expandedSubjects: Set<String>,
    expandedModules: Set<String>,
    onToggleSubject: (String) -> Unit,
    onToggleModule: (String) -> Unit,
    onOpenFullScreen: (() -> Unit)? = null
) {
    if (onOpenFullScreen != null) {
        item(key = "syl-fullscreen-btn") {
            FilledTonalButton(
                onClick = onOpenFullScreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(MmmutDimens.iconSm)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text("Open Dedicated Syllabus View")
            }
        }
    }

    when (sylState) {
        is UiState.Loading -> item(key = "syl-loading") { ListSkeleton(rows = 4) }
        is UiState.Error -> item(key = "syl-err") { ErrorView(sylState.message) { sylVm.load() } }
        is UiState.Empty -> item(key = "syl-empty") { EmptyView("No syllabus data available.") }
        is UiState.Success -> {
            val data = sylState.data
            if (data.subjectSyllabi.isEmpty() || data.totalTopics == 0) {
                item(key = "syl-no-topics") {
                    Text(
                        "No module breakdown is published for your branch yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                syllabusBodyItems(
                    data = data,
                    expandedSubjects = expandedSubjects,
                    expandedModules = expandedModules,
                    onToggleSubject = onToggleSubject,
                    onToggleModule = onToggleModule,
                    onToggleTopic = { code, key, comp ->
                        sylVm.toggleTopic(code, key, comp)
                    }
                )
            }
        }
    }
}

/* ------------------------------------------------------------ subjects ---- */

private fun LazyListScope.subjectsSection(
    d: AcademicsData,
    branch: com.mmmut.ero.data.local.Branch
) {
    item(key = "subj-intro") {
        Text(
            "${branch.subjects.size} registered subjects for ${branch.name}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    items(branch.subjects, key = { "subj-${it.code}" }) { sub ->
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MmmutDimens.minRowHeight)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    sub.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${sub.code} · L${sub.l} T${sub.t} P${sub.p}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "${sub.l + sub.t + (sub.p / 2)} cr",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MmmutDivider()
    }
}

/* ----------------------------------------------------------- timetable ---- */

private fun LazyListScope.timetableSection(
    d: AcademicsData,
    branch: com.mmmut.ero.data.local.Branch
) {
    val days = d.week.keys.toList()
    if (days.isEmpty()) {
        item(key = "tt-empty") {
            Text(
                "No timetable is published for your branch, section and groups yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    days.forEach { day ->
        item(key = "tt-day-$day") {
            SectionHeader(day)
        }
        val cells = (d.week[day] ?: emptyList()).filter { it.subjectCode != "—" }
        if (cells.isEmpty()) {
            item(key = "tt-day-empty-$day") {
                Text(
                    "No classes scheduled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                cells,
                key = { c -> "tt-$day-${c.id.ifBlank { c.periodKey + "::" + c.subjectCode }}" }
            ) { cell ->
                val group = cell.practicalGroup?.let { " · Group $it" }
                    ?: cell.tutorialGroup?.let { " · Group $it" }
                    ?: ""
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = MmmutDimens.touchTarget)
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Period ${cell.periodKey} · ${cell.start}–${cell.end}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${cell.subjectCode} — ${cell.subjectName}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${cell.type}$group · Room ${cell.room.ifBlank { branch.room }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                MmmutDivider()
            }
        }
    }
}

/* --------------------------------------------------------------- exams ---- */

private fun LazyListScope.examsSection(d: AcademicsData) {
    if (d.exams.isEmpty()) {
        item(key = "exam-empty") {
            Text(
                "No exams have been published on the academic calendar yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    items(d.exams, key = { e -> "exam-${e.id.ifBlank { e.title + e.date }}" }) { exam ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(Spacing.card),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    exam.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    exam.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (exam.detail.isNotBlank()) {
                    Text(
                        exam.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------- results ---- */

private fun LazyListScope.resultsSection(d: AcademicsData) {
    if (d.results.isEmpty()) {
        item(key = "result-empty") {
            Text(
                "Results will appear here once published by the university.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    items(d.results, key = { r -> "res-${r.subjectCode}" }) { r ->
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MmmutDimens.minRowHeight)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    r.subjectName.ifBlank { r.subjectCode },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    r.subjectCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            StatusChip(r.grade.ifBlank { "—" }, com.mmmut.ero.ui.components.ChipTone.Info)
        }
        MmmutDivider()
    }
}

/* ------------------------------------------------------------ calendar ---- */

private fun LazyListScope.calendarSection(d: AcademicsData) {
    if (d.events.isEmpty()) {
        item(key = "cal-empty") {
            Text(
                "No academic calendar events have been published yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    items(d.events, key = { e -> "cal-${e.id.ifBlank { e.title + e.start }}" }) { e ->
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MmmutDimens.minRowHeight)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    e.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (e.end.isBlank() || e.end == e.start) {
                        e.start
                    } else {
                        "${e.start} → ${e.end}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (e.source.isNotBlank()) {
                Spacer(Modifier.width(Spacing.sm))
                StatusChip(e.source, com.mmmut.ero.ui.components.ChipTone.Neutral)
            }
        }
        MmmutDivider()
    }
}

/* ------------------------------------------------------------- notices ---- */

private fun LazyListScope.noticesSection(d: AcademicsData) {
    item(key = "notice-pointer") {
        Text(
            "Open the Notices tab for the full list with complete text. The Home screen shows the latest posts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    item(key = "notice-note") {
        Text(
            "Notice content is not loaded on this screen, so nothing is shown that could be out of date.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ------------------------------------------------------------- helpers ---- */

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)
