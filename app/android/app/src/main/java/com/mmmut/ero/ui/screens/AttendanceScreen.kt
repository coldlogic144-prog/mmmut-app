package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.repository.AttendanceRepository
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.data.repository.FirestoreAttendanceRepository
import com.mmmut.ero.data.repository.LocalTimetableRepository
import com.mmmut.ero.data.repository.TimetableRepository
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.SectionHeader
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.components.friendlyError
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo
import com.mmmut.ero.util.SubjectAttendanceStats
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* ==========================================================================
 * ATTENDANCE — top-level tab
 * --------------------------------------------------------------------------
 * Data source is unchanged from the old Academics ▸ Attendance tab:
 *   • AttendanceRepository.loadMap(uid)      → Map<date, Map<"period::code", status>>
 *   • AttendanceUtils.summarize / subjectBreakdown / historyForSubject / computeLeaveInfo
 * Nothing about the calculation or the Firestore shape was touched; this file
 * only presents what those already return.
 *
 * Thresholds: the app has always used a single 75% bar (see
 * AttendanceUtils.SubjectAttendanceStats.status and AcademicsViewModel callers).
 * This screen uses the same 75% bar, so no number shown here disagrees with the
 * rest of the app.
 *
 * `onBack` is the screen-level back action. The internal subject-detail state
 * deliberately does NOT intercept it — wire it to navController.popBackStack()
 * so the system/app back arrow leaves Attendance, and the on-screen back arrow
 * returns from a subject detail to the list.
 * ========================================================================== */

const val ATTENDANCE_TARGET_PERCENT = 75.0

data class AttendanceScreenData(
    val profile: StudentProfile,
    val map: Map<String, Map<String, String>>,
    val week: Map<String, List<TimetableCell>>
)

/**
 * Same sources, same order, same calculations as `AcademicsViewModel` — this
 * screen simply does not need the calendar half (events/exams/results).
 * `AcademicsViewModel` is read-only for this task and lives in a file we do not
 * own, so the loading sequence is reused verbatim here rather than modified.
 */
class AttendanceViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val att: AttendanceRepository = FirestoreAttendanceRepository(),
    private val tt: TimetableRepository = LocalTimetableRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<AttendanceScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<AttendanceScreenData>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val profile = auth.currentProfile() ?: run {
                    _state.value = UiState.Error("Session expired. Please log in again.")
                    return@launch
                }
                val map = when (val r = att.loadMap(profile.uid)) {
                    is RepoResult.Ok -> r.value
                    is RepoResult.Err -> {
                        _state.value = UiState.Error(r.message)
                        return@launch
                    }
                }
                val week = try {
                    tt.weekCells(
                        branchId = profile.branchId,
                        section = profile.section,
                        tutorialGroup = profile.tutorialGroup,
                        practicalGroup = profile.practicalGroup,
                        semester = profile.semester
                    )
                } catch (_: Exception) { emptyMap() }

                _state.value = UiState.Success(AttendanceScreenData(profile, map, week))
            } catch (t: Throwable) {
                _state.value = UiState.Error(
                    friendlyError(t, "Couldn't load your attendance. Check your connection and retry.")
                )
            }
        }
    }

    /** Unchanged from the old Attendance tab: same key shape "period::code". */
    fun setAttendance(periodKey: String, subjectCode: String, status: String) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val today = TimeUtils.dateKey()
            val mutable = cur.map.mapValues { it.value.toMutableMap() }.toMutableMap()
            val day = mutable.getOrPut(today) { mutableMapOf() }
            day["$periodKey::$subjectCode"] = status
            val merged = cur.map.toMutableMap()
            merged[today] = day.toMap()
            _state.value = UiState.Success(cur.copy(map = merged))
            att.saveMap(cur.profile.uid, merged)
        }
    }
}

@Composable
fun AttendanceScreen(
    onBack: () -> Unit = {},
    vm: AttendanceViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var selectedSubject by remember { mutableStateOf<SubjectAttendanceStats?>(null) }

    val loaded = state as? UiState.Success
    val selected = selectedSubject

    if (loaded != null && selected != null) {
        SubjectDetail(
            stat = selected,
            map = loaded.data.map,
            onBackToList = { selectedSubject = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> ListSkeleton(rows = 5)
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("No attendance records yet.")
        is UiState.Success -> AttendanceList(
            data = s.data,
            onSelectSubject = { selectedSubject = it },
            onMark = { period, code, status -> vm.setAttendance(period, code, status) },
            onBack = onBack
        )
    }
}

/* ------------------------------------------------------------ list view ---- */

@Composable
private fun AttendanceList(
    data: AttendanceScreenData,
    onSelectSubject: (SubjectAttendanceStats) -> Unit,
    onMark: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val branch = remember(data.profile.branchId) { AcademicDataExtra.getBranch(data.profile.branchId) }

    val summary = remember(data.map) { AttendanceUtils.summarize(data.map) }
    val present = summary.first
    val absent = summary.second
    val pct = summary.third
    val total = present + absent
    val hasRecords = total > 0

    val leave = remember(present, absent) {
        AttendanceUtils.computeLeaveInfo(present, absent, ATTENDANCE_TARGET_PERCENT)
    }

    // Real subject definitions from the branch data, with the real logged counts.
    val breakdown = remember(data.map, branch.subjects) {
        AttendanceUtils.subjectBreakdown(data.map, branch.subjects)
    }
    // Only subjects that exist for this branch and have at least one logged class.
    // Nothing is invented for subjects that were never marked.
    val subjects = remember(breakdown) { breakdown.filter { it.total > 0 } }

    val statusColor = attendanceStatusColor(pct, hasRecords)

    val todayName = TimeUtils.todayName()
    val todayCells = remember(data.week, todayName) {
        (data.week[todayName] ?: emptyList()).filter { it.subjectCode != "—" }
    }
    val todayMap = remember(data.map) { data.map[TimeUtils.dateKey()] ?: emptyMap() }

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
        item(key = "header") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Attendance", style = MaterialTheme.typography.titleLarge, maxLines = 1)
                    Text(
                        "${branch.name} · Sem ${data.profile.semester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item(key = "overall") {
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
                        if (hasRecords) "${formatPct(pct)}%" else "—",
                        style = MaterialTheme.typography.displaySmall,
                        color = statusColor
                    )
                    Text(
                        "Attended $present / Total $total",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = { if (hasRecords) (pct / 100.0).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth().height(MmmutDimens.barHeight),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Text(
                        if (hasRecords) {
                            "Target ${formatPct(ATTENDANCE_TARGET_PERCENT)}% · ${projectionText(leave)}"
                        } else {
                            "No classes marked yet. Mark today's classes below to start tracking."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasRecords && pct < ATTENDANCE_TARGET_PERCENT) {
                        StatusChip("Below ${formatPct(ATTENDANCE_TARGET_PERCENT)}% target", ChipTone.Danger)
                    }
                }
            }
        }

        item(key = "subjects-head") {
            SectionHeader("Subjects", "${subjects.size}")
        }

        if (subjects.isEmpty()) {
            item(key = "subjects-empty") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No class has been marked present or absent yet, so there is nothing to break down by subject.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.card)
                    )
                }
            }
        } else {
            items(subjects, key = { "sub-${it.subjectCode}" }) { stat ->
                SubjectRow(stat = stat, onOpen = { onSelectSubject(stat) })
            }
        }

        item(key = "today-head") {
            SectionHeader("Today's log")
        }

        if (todayCells.isEmpty()) {
            item(key = "today-empty") {
                Text(
                    "No classes scheduled for today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
            }
        } else {
            items(
                todayCells,
                key = { "today-${it.id.ifBlank { it.periodKey + "::" + it.subjectCode }}" }
            ) { cell ->
                TodayClassRow(
                    cell = cell,
                    status = todayMap["${cell.periodKey}::${cell.subjectCode}"],
                    fallbackRoom = branch.room,
                    onMark = onMark
                )
            }
        }
    }
}

@Composable
private fun SubjectRow(
    stat: SubjectAttendanceStats,
    onOpen: () -> Unit
) {
    val hasRecords = stat.total > 0
    val tone = attendanceTone(stat.pct, hasRecords)

    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = MmmutDimens.minRowHeight)
    ) {
        Column(
            Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stat.subjectName.ifBlank { stat.subjectCode },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stat.subjectCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (hasRecords) "${formatPct(stat.pct)}%" else "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = attendanceStatusColor(stat.pct, hasRecords)
                    )
                    Text(
                        "${stat.present} / ${stat.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { if (hasRecords) (stat.pct / 100.0).toFloat().coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth().height(MmmutDimens.barHeight),
                color = attendanceStatusColor(stat.pct, hasRecords),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(
                    text = if (hasRecords) stat.status else "No records",
                    tone = tone
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Attended ${stat.present} / ${stat.total}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TodayClassRow(
    cell: TimetableCell,
    status: String?,
    fallbackRoom: String,
    onMark: (String, String, String) -> Unit
) {
    val room = cell.room.ifBlank { fallbackRoom }
    val groupTag = cell.practicalGroup?.let { " · Group $it" }
        ?: cell.tutorialGroup?.let { " · Group $it" }
        ?: ""

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
                "${cell.type}$groupTag · Room $room",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = status == "present",
                    onClick = { onMark(cell.periodKey, cell.subjectCode, "present") },
                    label = { Text("Present") },
                    leadingIcon = {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconSm))
                    }
                )
                FilterChip(
                    selected = status == "absent",
                    onClick = { onMark(cell.periodKey, cell.subjectCode, "absent") },
                    label = { Text("Absent") },
                    leadingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconSm))
                    }
                )
            }
        }
    }
}

/* ---------------------------------------------------------- detail view ---- */

@Composable
private fun SubjectDetail(
    stat: SubjectAttendanceStats,
    map: Map<String, Map<String, String>>,
    onBackToList: () -> Unit
) {
    val history = remember(map, stat.subjectCode) {
        AttendanceUtils.historyForSubject(map, stat.subjectCode)
    }
    val leave = remember(stat.present, stat.absent) {
        AttendanceUtils.computeLeaveInfo(stat.present, stat.absent, ATTENDANCE_TARGET_PERCENT)
    }
    val hasRecords = stat.total > 0
    val statusColor = attendanceStatusColor(stat.pct, hasRecords)

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
        item(key = "detail-header") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToList) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to all subjects",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        stat.subjectCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        stat.subjectName.ifBlank { stat.subjectCode },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item(key = "detail-summary") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        if (hasRecords) "${formatPct(stat.pct)}%" else "—",
                        style = MaterialTheme.typography.displaySmall,
                        color = statusColor
                    )
                    LinearProgressIndicator(
                        progress = { if (hasRecords) (stat.pct / 100.0).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth().height(MmmutDimens.barHeight),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                        DetailStat("Attended", stat.present.toString())
                        DetailStat("Absent", stat.absent.toString())
                        DetailStat("Total", stat.total.toString())
                    }
                    StatusChip(
                        text = if (hasRecords) stat.status else "No records",
                        tone = attendanceTone(stat.pct, hasRecords)
                    )
                    Text(
                        if (hasRecords) {
                            "Target ${formatPct(ATTENDANCE_TARGET_PERCENT)}% · ${projectionText(leave)}"
                        } else {
                            "No classes logged for this subject yet."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "detail-history-head") {
            SectionHeader("Class history")
        }

        if (history.isEmpty()) {
            item(key = "detail-history-empty") {
                Text(
                    "No logs recorded for this subject.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
            }
        } else {
            items(
                count = history.size,
                key = { i -> "h-$i-${history[i].first}-${history[i].second}" }
            ) { i ->
                val (date, period, status) = history[i]
                val isPresent = status == "present"
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = MmmutDimens.touchTarget)
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            date,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (period.isNotBlank()) "Period $period" else "Period —",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    StatusChip(
                        text = if (isPresent) "Present" else "Absent",
                        tone = if (isPresent) ChipTone.Success else ChipTone.Danger
                    )
                }
                MmmutDivider()
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* --------------------------------------------------------------- helpers --- */

/**
 * Percentage colour.
 *
 * The app's one hard threshold is 75% (`SubjectAttendanceStats.status`,
 * `computeLeaveInfo`). The 65% step here is purely a "getting close to the line"
 * visual cue and does not change any calculation or the 75% chip text.
 */
@Composable
private fun attendanceStatusColor(pct: Double, hasRecords: Boolean): Color {
    val s = MaterialTheme.semantic
    return when {
        !hasRecords -> MaterialTheme.colorScheme.onSurfaceVariant
        pct >= ATTENDANCE_TARGET_PERCENT -> s.success
        pct >= 65.0 -> s.warning
        else -> s.danger
    }
}

/** Chip tone derived from the same tiers as [attendanceStatusColor]. */
private fun attendanceTone(pct: Double, hasRecords: Boolean): ChipTone = when {
    !hasRecords -> ChipTone.Neutral
    pct >= ATTENDANCE_TARGET_PERCENT -> ChipTone.Success
    pct >= 65.0 -> ChipTone.Warning
    else -> ChipTone.Danger
}

private fun formatPct(value: Double): String = String.format(java.util.Locale.US, "%.1f", value)

/** Projection text — derived from real logged counts only, never invented. */
private fun projectionText(leave: LeaveInfo): String = when (leave) {
    is LeaveInfo.CanSkip -> "you can skip ${leave.count} more classes and stay above target"
    is LeaveInfo.MustAttend -> "attend the next ${leave.count} classes to reach target"
    is LeaveInfo.None -> "not enough records yet for a projection"
}
