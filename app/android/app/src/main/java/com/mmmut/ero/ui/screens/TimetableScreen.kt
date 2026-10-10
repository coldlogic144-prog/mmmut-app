package com.mmmut.ero.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.components.friendlyError
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.TimetableFilter
import com.mmmut.ero.ui.viewmodel.TimetableOption
import com.mmmut.ero.ui.viewmodel.TimetableScreenData
import com.mmmut.ero.ui.viewmodel.TimetableViewModel
import kotlinx.coroutines.delay

/* ==========================================================================
 * Academic Timetable — presentation layer
 * --------------------------------------------------------------------------
 * Presentation only. Every class shown is whatever ScheduleEngine already
 * returned for the current selection; nothing about branch, group, section or
 * period filtering is decided here.
 *
 * Layout rules this screen exists to satisfy:
 *   • exactly ONE LazyColumn per view — never a vertical list inside a list,
 *   • a fixed-width time rail so times cannot wrap into the subject,
 *   • classes sharing a time window are grouped into one labelled card,
 *   • a multi-period practical is one block with its real start/end times.
 * ========================================================================== */

/* --------------------------------------------------------------- local model */

/** How a slot relates to the clock right now. */
private enum class SlotState { Live, Next, Later }

/**
 * All classes the student has in one time window.
 *
 * A slot exists because two different classes can start at the same minute —
 * a T1 tutorial while T2 has a different tutorial, or a P1 practical while P2
 * is in another lab. `entries` therefore holds every concurrent class and the
 * card stacks them instead of letting one hide the other.
 */
private data class Slot(
    val start: String,
    val end: String,
    val entries: List<TimetableCell>,
    val state: SlotState
) {
    val key: String get() = "$start|$end|" + entries.joinToString(",") { cellKey(it) }
}

private enum class TimetableView { Today, Week }

private const val LIST_LABEL_MAX = 20

/* ------------------------------------------------------------------- screen */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    vm: TimetableViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()

    var view by remember { mutableStateOf(TimetableView.Today) }
    /** Cell whose detail dialog is open, plus how many classes share its window. */
    var detail by remember { mutableStateOf<Pair<TimetableCell, Int>?>(null) }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Timetable",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Session 2025-26",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.load() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload timetable",
                            modifier = Modifier.size(MmmutDimens.iconLg)
                        )
                    }
                }
            )
        }
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            when (val s = state) {
                is UiState.Loading -> ListSkeleton(5)

                is UiState.Empty -> EmptyView(
                    title = "No classes scheduled",
                    message = "No timetable is published for your branch, semester and section yet.",
                    actionLabel = "Reload",
                    onAction = { vm.load() }
                )

                is UiState.Error -> ErrorView(
                    message = friendlyError(
                        vm.lastError(),
                        fallback = s.message.ifBlank { "Please try again." }
                    ),
                    onRetry = { vm.load() }
                )

                is UiState.Success -> Content(
                    data = s.data,
                    view = view,
                    onViewChange = { view = it },
                    onFilter = { vm.setFilter(it) },
                    onResetFilter = { vm.resetFilter() },
                    onReload = { vm.load() },
                    onTick = { vm.refreshNow() },
                    onOpenCell = { cell, concurrent -> detail = cell to concurrent }
                )
            }
        }
    }

    val shown = (state as? UiState.Success)?.data
    detail?.let { (cell, concurrentCount) ->
        SlotDetailDialog(
            cell = cell,
            data = shown,
            concurrentCount = concurrentCount,
            onDismiss = { detail = null }
        )
    }
}

/* ------------------------------------------------------------------ content */

@Composable
private fun Content(
    data: TimetableScreenData,
    view: TimetableView,
    onViewChange: (TimetableView) -> Unit,
    onFilter: (TimetableFilter) -> Unit,
    onResetFilter: () -> Unit,
    onReload: () -> Unit,
    onTick: () -> Unit,
    onOpenCell: (TimetableCell, Int) -> Unit
) {
    // Keeps the "Now" / "Next" markers honest while the screen is open. The
    // actual getCurrentClass/getNextClass pass stays in the ViewModel.
    LaunchedEffect(data.todayName) {
        while (true) {
            delay(30_000L)
            onTick()
        }
    }

    val todaySlots = remember(data.todayCells, data.currentCell, data.nextCell) {
        buildSlots(data.todayCells, data.currentCell, data.nextCell)
    }
    val weekSlots = remember(data.weekGrid, data.currentCell, data.nextCell, data.todayName) {
        AcademicData.DAYS.map { day ->
            day to buildSlots(
                cells = data.weekGrid[day].orEmpty(),
                current = if (day == data.todayName) data.currentCell else null,
                next = if (day == data.todayName) data.nextCell else null
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        FilterBar(data = data, onFilter = onFilter, onResetFilter = onResetFilter)

        ViewSwitcher(view = view, onViewChange = onViewChange)

        when (view) {
            TimetableView.Today -> TodayList(
                data = data,
                slots = todaySlots,
                onReload = onReload,
                onOpenCell = onOpenCell
            )

            TimetableView.Week -> WeekList(
                data = data,
                sections = weekSlots,
                onReload = onReload,
                onOpenCell = onOpenCell
            )
        }
    }
}

/* --------------------------------------------------------------- filter bar */

/**
 * Branch · Semester · Section · Tutorial group · Practical group, in one
 * horizontally scrollable row. Each chip shows its current value and opens the
 * options that genuinely exist in the timetable data, so the student can never
 * pick a combination that renders an empty week.
 */
@Composable
private fun FilterBar(
    data: TimetableScreenData,
    onFilter: (TimetableFilter) -> Unit,
    onResetFilter: () -> Unit
) {
    val f = data.filter

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.screen, end = Spacing.sm, top = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.branchName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(Spacing.sm))
                if (data.isOwnTimetable) {
                    StatusChip("My section", ChipTone.Success)
                } else {
                    StatusChip("Other section", ChipTone.Warning)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Semester ${f.semester} · Section ${f.section} · " +
                        "Tutorial ${groupLabel(f.tutorialGroup)} · " +
                        "Practical ${groupLabel(f.practicalGroup)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!data.isOwnTimetable) {
                    TextButton(onClick = onResetFilter) { Text("My section") }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = Spacing.screen,
                    vertical = Spacing.sm
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item(key = "branch") {
                    FilterDropdown(
                        label = "Branch",
                        current = f.branchId,
                        display = data.branchOptions.firstOrNull { it.value == f.branchId }?.label
                            ?: data.branchName,
                        options = data.branchOptions,
                        onSelect = { onFilter(f.copy(branchId = it)) }
                    )
                }
                item(key = "semester") {
                    FilterDropdown(
                        label = "Semester",
                        current = f.semester.toString(),
                        display = "Sem ${f.semester}",
                        options = data.semesterOptions,
                        onSelect = { chosen ->
                            chosen.toIntOrNull()?.let { onFilter(f.copy(semester = it)) }
                        }
                    )
                }
                item(key = "section") {
                    FilterDropdown(
                        label = "Section",
                        current = f.section,
                        display = "Sec ${f.section}",
                        options = data.sectionOptions,
                        onSelect = { onFilter(f.copy(section = it)) }
                    )
                }
                item(key = "tutorial") {
                    FilterDropdown(
                        label = "Tutorial",
                        current = f.tutorialGroup,
                        display = "Tut ${groupLabel(f.tutorialGroup)}",
                        options = data.tutorialOptions,
                        onSelect = { onFilter(f.copy(tutorialGroup = it)) }
                    )
                }
                item(key = "practical") {
                    FilterDropdown(
                        label = "Practical",
                        current = f.practicalGroup,
                        display = "Prac ${groupLabel(f.practicalGroup)}",
                        options = data.practicalOptions,
                        onSelect = { onFilter(f.copy(practicalGroup = it)) }
                    )
                }
            }

            MmmutDivider()
        }
    }
}

/**
 * Filter chip + anchored menu. One row of these replaces the old modal
 * "Configure Timetable Groups" dialog, and it stays usable at 320dp because the
 * choices live in a popup instead of more inline chrome.
 *
 * When the current value is not offered by the data (a stored group that no
 * longer exists for this branch/semester/section), the chip says so instead of
 * leaving the student with a silently empty timetable and no explanation.
 */
@Composable
private fun FilterDropdown(
    label: String,
    current: String,
    display: String,
    options: List<TimetableOption>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val unavailable = remember(options, current) {
        options.none { it.value == current }
    }
    val shown = remember(display, unavailable) {
        val suffix = if (unavailable) " (unavailable)" else ""
        compactLabel(display).take(LIST_LABEL_MAX) + suffix
    }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Text(
                    text = "$label $shown",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(MmmutDimens.iconSm)
                )
            },
            modifier = Modifier.defaultMinSize(minHeight = MmmutDimens.touchTarget)
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No options available") },
                    enabled = false,
                    onClick = {}
                )
            }
            options.forEach { option ->
                val isChosen = option.value == current
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            style = if (isChosen) {
                                MaterialTheme.typography.bodyMedium
                                    .copy(fontWeight = FontWeight.SemiBold)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        if (isChosen) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(MmmutDimens.iconMd)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!isChosen) onSelect(option.value)
                    }
                )
            }
        }
    }
}

/* -------------------------------------------------------------- view switch */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewSwitcher(view: TimetableView, onViewChange: (TimetableView) -> Unit) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.sm)
    ) {
        SegmentedButton(
            selected = view == TimetableView.Today,
            onClick = { onViewChange(TimetableView.Today) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            label = { Text("Today") }
        )
        SegmentedButton(
            selected = view == TimetableView.Week,
            onClick = { onViewChange(TimetableView.Week) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            label = { Text("Week") }
        )
    }
}

/* --------------------------------------------------------------- today view */

@Composable
private fun TodayList(
    data: TimetableScreenData,
    slots: List<Slot>,
    onReload: () -> Unit,
    onOpenCell: (TimetableCell, Int) -> Unit
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.xs,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        item(key = "today-label") {
            Text(
                text = data.todayName + " · " + classCountLabel(data.todayCells.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (slots.isEmpty()) {
            item(key = "today-empty") {
                EmptyView(
                    title = "No classes today",
                    message = "Nothing is scheduled for ${data.todayName}. " +
                        "Switch to Week to see the rest of your timetable.",
                    actionLabel = "Reload",
                    onAction = onReload,
                    modifier = Modifier.heightIn(min = 240.dp)
                )
            }
        } else {
            items(items = slots, key = { it.key }) { slot ->
                SlotCard(
                    slot = slot,
                    fallbackRoom = data.room,
                    onOpenCell = onOpenCell
                )
            }
        }
    }
}

/* ---------------------------------------------------------------- week view */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeekList(
    data: TimetableScreenData,
    sections: List<Pair<String, List<Slot>>>,
    onReload: () -> Unit,
    onOpenCell: (TimetableCell, Int) -> Unit
) {
    val todayName = data.todayName
    val anyClasses = remember(sections) { sections.any { it.second.isNotEmpty() } }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxxl)
    ) {
        item(key = "week-hint") {
            Text(
                text = "Every day of the week, Monday to Saturday. Free days are listed too.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = Spacing.screen,
                    end = Spacing.screen,
                    top = Spacing.xs,
                    bottom = Spacing.sm
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!anyClasses) {
            item(key = "week-empty") {
                EmptyView(
                    title = "No classes scheduled",
                    message = "Nothing is scheduled for Section ${data.filter.section} in " +
                        "Semester ${data.filter.semester}. Try another section or group.",
                    actionLabel = "Reload",
                    onAction = onReload,
                    modifier = Modifier.heightIn(min = 240.dp)
                )
            }
        } else {
            sections.forEach { (day, slots) ->
                stickyHeader(key = "header-$day") {
                    DayHeader(day = day, isToday = day == todayName, count = slots.size)
                }

                if (slots.isEmpty()) {
                    item(key = "free-$day") {
                        Text(
                            text = "No classes scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = Spacing.screen,
                                end = Spacing.screen,
                                top = Spacing.sm,
                                bottom = Spacing.lg
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    items(items = slots, key = { "$day|${it.key}" }) { slot ->
                        Box(
                            Modifier.padding(
                                start = Spacing.screen,
                                end = Spacing.screen,
                                top = Spacing.xxs,
                                bottom = Spacing.xxs
                            )
                        ) {
                            SlotCard(
                                slot = slot,
                                fallbackRoom = data.room,
                                onOpenCell = onOpenCell
                            )
                        }
                    }
                    item(key = "gap-$day") { Spacer(Modifier.height(Spacing.md)) }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: String, isToday: Boolean, count: Int) {
    val container = if (isToday) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(color = container, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = content,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isToday) {
                StatusChip("Today", ChipTone.Info)
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = if (count == 0) "Free" else classCountLabel(count),
                style = MaterialTheme.typography.labelMedium,
                color = content,
                maxLines = 1
            )
        }
    }
}

/* ---------------------------------------------------------------- slot card */

/**
 * One time window.
 *
 * When `entries.size > 1` the student genuinely has several classes at the same
 * time: they are stacked inside a single card, the header states how many, and
 * each entry keeps its own type chip, group and room — nothing overlaps, clips
 * or overwrites anything else.
 */
@Composable
private fun SlotCard(
    slot: Slot,
    fallbackRoom: String,
    onOpenCell: (TimetableCell, Int) -> Unit
) {
    val isLive = slot.state == SlotState.Live
    val isNext = slot.state == SlotState.Next
    val concurrent = slot.entries.size

    val container = when {
        isLive -> MaterialTheme.colorScheme.primaryContainer
        isNext -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val accent = when {
        isLive -> MaterialTheme.colorScheme.primary
        isNext -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        onClick = { slot.entries.firstOrNull()?.let { onOpenCell(it, concurrent) } },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MmmutDimens.minRowHeight)
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent: strong for the class in progress, muted otherwise.
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            TimeRail(start = slot.start, end = slot.end)

            MmmutDivider(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )

            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                SlotStatusRow(slot = slot)

                slot.entries.forEachIndexed { index, cell ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                    ClassEntry(
                        cell = cell,
                        fallbackRoom = fallbackRoom
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotStatusRow(slot: Slot) {
    // "Later" is the default state and would only be noise on every card; the
    // accent stripe and time rail already carry that. Only the two states a
    // student actually needs to notice get a badge.
    val status = when (slot.state) {
        SlotState.Live -> "Now" to ChipTone.Danger
        SlotState.Next -> "Next" to ChipTone.Info
        SlotState.Later -> null
    }
    val concurrent = slot.entries.size > 1

    if (status == null && !concurrent) return

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (status != null) {
            StatusChip(status.first, status.second)
        }
        if (concurrent) {
            if (status != null) Spacer(Modifier.width(Spacing.sm))
            StatusChip("Concurrent", ChipTone.Warning)
        }
    }
}

/**
 * The body of one class: subject name (up to two lines, so a long name is never
 * truncated to nothing), then type and group chips, then code · room · faculty.
 * The whole card is the touch target, so this row needs no click handler of its
 * own — nesting them would only create an ambiguous hit area.
 */
@Composable
private fun ClassEntry(
    cell: TimetableCell,
    fallbackRoom: String
) {
    val room = cell.room.ifBlank { fallbackRoom }
    val group = entryGroup(cell)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MmmutDimens.touchTarget),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = cell.subjectName.ifBlank { cell.subjectCode.ifBlank { "Class" } },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(text = cell.type.ifBlank { "Class" }, tone = typeTone(cell.type))
            if (group != null) {
                Spacer(Modifier.width(Spacing.sm))
                StatusChip(text = group, tone = groupTone(cell))
            }
        }

        val meta = buildList {
            if (cell.subjectCode.isNotBlank() && cell.subjectCode != "—") add(cell.subjectCode)
            if (room.isNotBlank()) add("Room $room")
            if (cell.instructor.isNotBlank()) add(cell.instructor)
        }.joinToString(" · ")

        if (meta.isNotBlank()) {
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* --------------------------------------------------------------- time rail */

/**
 * Fixed-width rail carrying start and end time. `MmmutDimens.timeRailWidth`
 * fits "09:10" at labelLarge, so the times never wrap and never collide with
 * the subject text beside them.
 */
@Composable
private fun TimeRail(start: String, end: String) {
    Column(
        modifier = Modifier
            .width(MmmutDimens.timeRailWidth)
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        Text(
            text = start,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = end,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ------------------------------------------------------------ detail dialog */

@Composable
private fun SlotDetailDialog(
    cell: TimetableCell,
    data: TimetableScreenData?,
    concurrentCount: Int,
    onDismiss: () -> Unit
) {
    val group = entryGroup(cell)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = cell.subjectName.ifBlank { cell.subjectCode.ifBlank { "Class details" } },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MmmutDimens.iconMd)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "${cell.start} – ${cell.end}",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(cell.type.ifBlank { "Class" }, typeTone(cell.type))
                    if (group != null) {
                        Spacer(Modifier.width(Spacing.sm))
                        StatusChip(group, groupTone(cell))
                    }
                }

                if (concurrentCount > 1) {
                    Text(
                        text = "$concurrentCount classes share this time slot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MmmutDivider()

                DetailLine("Subject code", cell.subjectCode.takeIf { it.isNotBlank() && it != "—" })
                DetailLine("Period", cell.periodKey.takeIf { it.isNotBlank() })
                DetailLine("Room", cell.room.ifBlank { data?.room.orEmpty() })
                DetailLine("Faculty", cell.instructor.takeIf { it.isNotBlank() })
                DetailLine("Notes", cell.notes.takeIf { it.isNotBlank() })
                DetailLine("Branch", data?.branchName)
                DetailLine("Semester", data?.let { "Semester ${it.filter.semester}" })
                DetailLine("Section", data?.let { "Section ${it.filter.section}" })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/* ------------------------------------------------------------------ helpers */

/**
 * Collapses the day's cells into renderable slots.
 *
 * Cells share a slot when they have the same start *and* end time. That is what
 * makes a concurrent pair (a T1 tutorial while T2 has another tutorial) show as
 * one window holding two classes, while a practical that spans periods I–II
 * appears once with its real 09:10–10:50 range instead of as duplicate rows.
 *
 * ScheduleEngine already returns cells in chronological order and `sortedWith`
 * is stable, so equal times keep their original relative order.
 */
private fun buildSlots(
    cells: List<TimetableCell>,
    current: TimetableCell?,
    next: TimetableCell?
): List<Slot> {
    val currentKey = current?.let { cellKey(it) }
    val nextKey = next?.let { cellKey(it) }

    return cells
        .filter { it.isDisplayable() }
        .groupBy { it.start to it.end }
        .map { (range, entries) ->
            val state = when {
                currentKey != null && entries.any { cellKey(it) == currentKey } -> SlotState.Live
                nextKey != null && entries.any { cellKey(it) == nextKey } -> SlotState.Next
                else -> SlotState.Later
            }
            Slot(
                start = range.first,
                end = range.second,
                entries = entries.sortedWith(
                    compareBy({ typeRank(it.type) }, { it.periodKey }, { cellKey(it) })
                ),
                state = state
            )
        }
        .sortedWith(compareBy({ toMinutes(it.start) }, { toMinutes(it.end) }))
}

/**
 * A cell is shown when it carries a real subject and is not a placeholder "free
 * period" row — the same rule ScheduleEngine.getCurrentClass applies before it
 * calls a cell a live class, so the "Now" marker can never point at an empty row.
 */
private fun TimetableCell.isDisplayable(): Boolean =
    subjectCode.isNotBlank() &&
        subjectCode != "—" &&
        !type.equals("Free", ignoreCase = true)

/**
 * Stable identity for a cell, even when the data ships an empty id.
 *
 * The group, room and faculty are part of the key on purpose: two cells in the
 * same window can share day, times, subject and type (a T1 and a T2 tutorial of
 * the same subject), and a duplicate key would crash the lazy list.
 */
private fun cellKey(cell: TimetableCell): String = cell.id.ifBlank {
    listOf(
        cell.day,
        cell.start,
        cell.end,
        cell.periodKey,
        cell.subjectCode,
        cell.type,
        cell.tutorialGroup.orEmpty(),
        cell.practicalGroup.orEmpty(),
        cell.room,
        cell.instructor
    ).joinToString("|")
}

/**
 * The group badge, shown only when the class actually belongs to a group.
 * A lecture for everyone has no group and therefore gets no badge.
 */
private fun entryGroup(cell: TimetableCell): String? {
    val tutorial = cell.tutorialGroup?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", true) }
    val practical = cell.practicalGroup?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", true) }
    return when {
        cell.type.equals("Practical", ignoreCase = true) -> practical ?: tutorial
        cell.type.equals("Tutorial", ignoreCase = true) -> tutorial ?: practical
        else -> null
    }
}

private fun typeTone(type: String): ChipTone = when {
    type.equals("Practical", ignoreCase = true) -> ChipTone.Success
    type.equals("Tutorial", ignoreCase = true) -> ChipTone.Info
    else -> ChipTone.Neutral
}

/** Practical groups use the success ramp, tutorial groups the info ramp. */
private fun groupTone(cell: TimetableCell): ChipTone =
    if (cell.type.equals("Practical", ignoreCase = true)) ChipTone.Success else ChipTone.Info

/** Lectures first inside a shared slot, then tutorials, then practicals. */
private fun typeRank(type: String): Int = when {
    type.equals("Lecture", ignoreCase = true) -> 0
    type.equals("Tutorial", ignoreCase = true) -> 1
    type.equals("Practical", ignoreCase = true) -> 2
    else -> 3
}

private fun groupLabel(value: String): String {
    val clean = value.trim()
    return if (clean.isEmpty() || clean.equals("N/A", ignoreCase = true)) "—" else clean
}

/**
 * Keeps the chip readable on a 320dp phone: the full branch name stays in the
 * menu and the header, while the chip gets a compact form.
 */
private fun compactLabel(label: String): String = when {
    label.startsWith("B.Tech — ") -> "B.Tech " + label.removePrefix("B.Tech — ")
    else -> label
}

private fun classCountLabel(count: Int): String =
    if (count == 1) "1 class" else "$count classes"

private fun toMinutes(time: String): Int {
    val parts = time.trim().split(":")
    if (parts.size != 2) return Int.MAX_VALUE
    return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
}
