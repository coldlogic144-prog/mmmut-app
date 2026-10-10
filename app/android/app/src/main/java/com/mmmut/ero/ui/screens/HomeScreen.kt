package com.mmmut.ero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.HomeAttendanceSummary
import com.mmmut.ero.ui.viewmodel.HomeScreenData
import com.mmmut.ero.ui.viewmodel.HomeViewModel
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ==========================================================================
 * Home — "what do I need to know today?"
 * --------------------------------------------------------------------------
 * One LazyColumn, gutters from contentPadding, no nested scrolling.
 * Order of information: who am I -> what is happening now -> today -> where
 * do I stand (attendance) -> what was announced -> where do I need to go.
 *
 * Everything shown here is real: profile from Firestore, timetable from the
 * official schedule, attendance from the same document Academics reads.
 * A section with no data renders an explicit empty line, never a blank gap.
 * ========================================================================== */

@Composable
fun HomeScreen(
    onNotices: () -> Unit,
    onNotice: (String) -> Unit,
    onAcademics: () -> Unit,
    onProfile: () -> Unit,
    onHostel: () -> Unit,
    onMessMenu: () -> Unit = {},
    onNotifications: () -> Unit,
    onTelegram: () -> Unit,
    onCampus3D: () -> Unit = {},
    // New params (existing callers keep compiling): all three fall back to the
    // Academics hub, which is where these lived before. AppNav can wire them to
    // Routes.TIMETABLE / SYLLABUS / ATTENDANCE without touching this file.
    onSyllabus: () -> Unit = onAcademics,
    onAttendance: () -> Unit = onAcademics,
    onTimetable: () -> Unit = onAcademics,
    vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        is UiState.Loading -> ListSkeleton(rows = 4)

        is UiState.Error -> ErrorView(s.message) { vm.load() }

        is UiState.Empty -> EmptyView(
            title = "Nothing to show yet",
            message = "Your dashboard appears once your profile and timetable are ready.",
            actionLabel = "Refresh",
            onAction = { vm.load() }
        )

        is UiState.Success -> HomeDashboard(
            data = s.data,
            onNotices = onNotices,
            onNotice = onNotice,
            onProfile = onProfile,
            onHostel = onHostel,
            onMessMenu = onMessMenu,
            onNotifications = onNotifications,
            onTelegram = onTelegram,
            onCampus3D = onCampus3D,
            onSyllabus = onSyllabus,
            onAttendance = onAttendance,
            onTimetable = onTimetable
        )
    }
}

@Composable
private fun HomeDashboard(
    data: HomeScreenData,
    onNotices: () -> Unit,
    onNotice: (String) -> Unit,
    onProfile: () -> Unit,
    onHostel: () -> Unit,
    onMessMenu: () -> Unit,
    onNotifications: () -> Unit,
    onTelegram: () -> Unit,
    onCampus3D: () -> Unit,
    onSyllabus: () -> Unit,
    onAttendance: () -> Unit,
    onTimetable: () -> Unit
) {
    val p = data.profile
    val now = rememberClock()

    val branch = remember(p.branchId) { AcademicDataExtra.getBranch(p.branchId) }

    // Re-derived only when the timetable or the (30s) clock changes.
    val current = remember(data.todayCells, now) { ScheduleEngine.getCurrentClass(data.todayCells, now) }
    val next = remember(data.todayCells, now) { ScheduleEngine.getNextClass(data.todayCells, now) }
    val focused = current ?: next
    val topNotices = remember(data.notices) { data.notices.take(3) }
    val unread = data.unreadNoticeCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        /* ---------------------------------------------------- who am I ---- */
        item(key = "header") {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            TimeUtils.greeting(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            p.name.ifBlank { p.username }.ifBlank { "Student" },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box {
                        IconButton(
                            onClick = onNotifications,
                            modifier = Modifier.size(MmmutDimens.touchTarget)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = if (unread > 0) "$unread unread notifications" else "Notifications",
                                modifier = Modifier.size(MmmutDimens.iconLg)
                            )
                        }
                        if (unread > 0) {
                            UnreadDot(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = Spacing.md, end = Spacing.md)
                            )
                        }
                    }
                    IconButton(
                        onClick = onProfile,
                        modifier = Modifier.size(MmmutDimens.touchTarget)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Open profile",
                            modifier = Modifier.size(MmmutDimens.iconLg)
                        )
                    }
                }

                Text(
                    "${branch.name} · Sem ${p.semester} · Sec ${p.section}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    buildMetaLine(p.rollNumber, p.tutorialGroup, p.practicalGroup, p.hostel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        /* ------------------------------------------------- next class ----- */
        item(key = "focus-class") {
            val cell = focused
            if (cell == null) {
                NoClassCard(
                    hasClassesToday = data.todayCells.isNotEmpty(),
                    dayName = data.todayName
                )
            } else {
                FocusClassCard(
                    cell = cell,
                    inProgress = current != null,
                    fallbackRoom = branch.room
                )
            }
        }

        /* -------------------------------------------- today's classes ----- */
        item(key = "today-header") {
            HomeSectionHeader(
                title = if (data.todayName != null) "Today · ${data.todayName}" else "Today",
                actionLabel = "Full timetable",
                onAction = onTimetable
            )
        }

        if (data.todayCells.isEmpty()) {
            item(key = "today-empty") {
                InlineEmpty(
                    if (data.todayName == null) "No classes scheduled today."
                    else "No classes scheduled for ${data.todayName}."
                )
            }
        } else {
            itemsIndexed(
                items = data.todayCells,
                key = { index, cell ->
                    "cell-" + cell.id.ifBlank { "${cell.periodKey}|${cell.subjectCode}|$index" }
                }
            ) { _, cell ->
                val isCurrent = current != null &&
                    if (cell.id.isNotBlank()) cell.id == current.id else cell == current
                TodayClassRow(
                    cell = cell,
                    isCurrent = isCurrent,
                    isPast = !isCurrent && cell.end.isNotBlank() && cell.end < now,
                    fallbackRoom = branch.room,
                    onClick = onTimetable
                )
            }
        }

        /* ------------------------------------------------- attendance ----- */
        item(key = "attendance") {
            AttendanceCard(data.attendance, onAttendance)
        }

        /* ---------------------------------------------------- notices ----- */
        item(key = "notices-header") {
            HomeSectionHeader("Important Notices", "View all", onNotices)
        }

        if (topNotices.isEmpty()) {
            item(key = "notices-empty") { InlineEmpty("No notices have been posted yet.") }
        } else {
            itemsIndexed(
                items = topNotices,
                key = { index, n -> "notice-" + n.id.ifBlank { index.toString() } }
            ) { _, n ->
                NoticeCard(
                    n = n,
                    unread = n.createdAtMillis.toDouble() > p.lastReadPosts,
                    onClick = { onNotice(n.id) }
                )
            }
        }

        /* ----------------------------------------------- quick access ----- */
        item(key = "quick-header") { HomeSectionHeader("Quick access") }

        item(key = "quick-grid") {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    QuickAction(Icons.Default.Schedule, "Timetable", onTimetable, Modifier.weight(1f))
                    QuickAction(Icons.AutoMirrored.Filled.FactCheck, "Attendance", onAttendance, Modifier.weight(1f))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    QuickAction(Icons.AutoMirrored.Filled.MenuBook, "Syllabus", onSyllabus, Modifier.weight(1f))
                    QuickAction(Icons.Default.RestaurantMenu, "Mess Menu", onMessMenu, Modifier.weight(1f))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    QuickAction(Icons.Default.Hotel, "Hostel", onHostel, Modifier.weight(1f))
                    QuickAction(Icons.Default.ViewInAr, "3D Campus", onCampus3D, Modifier.weight(1f))
                }

                // Telegram is a primary service for this campus — kept as a
                // full-width, explicitly labelled button rather than an icon.
                Button(
                    onClick = onTelegram,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MmmutDimens.touchTarget),
                    contentPadding = PaddingValues(horizontal = Spacing.md)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(MmmutDimens.iconMd)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "Telegram Channel",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item(key = "bottom-space") { Spacer(Modifier.height(Spacing.xxl)) }
    }
}

/* --------------------------------------------------------------- header --- */

private fun buildMetaLine(roll: String, tut: String, prac: String, hostel: String): String {
    val parts = mutableListOf<String>()
    if (roll.isNotBlank()) parts += "Roll $roll"
    if (tut.isNotBlank() && !tut.equals("N/A", true)) parts += "Tutorial $tut"
    if (prac.isNotBlank() && !prac.equals("N/A", true)) parts += "Practical $prac"
    if (hostel.isNotBlank()) parts += hostel
    return parts.joinToString(" · ").ifBlank { "Profile incomplete" }
}

/**
 * Deliberately NOT the shared [SectionHeader]: that one hardcodes a
 * `Spacing.screen` start padding, which would double up against the
 * LazyColumn's horizontal contentPadding gutter.
 */
@Composable
private fun HomeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = Spacing.sm),
                modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}

/* ----------------------------------------------------------- next class --- */

@Composable
private fun FocusClassCard(
    cell: TimetableCell,
    inProgress: Boolean,
    fallbackRoom: String
) {
    val rail = if (inProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val room = cell.room.ifBlank { fallbackRoom }.ifBlank { "Not listed" }
    val group = cell.practicalGroup ?: cell.tutorialGroup

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(Spacing.xs)
                    .fillMaxHeight()
                    .background(rail)
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(Spacing.card),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (inProgress) StatusChip("In progress", ChipTone.Success)
                    else StatusChip("Next", ChipTone.Info)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${cell.start} – ${cell.end}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    cell.subjectName.ifBlank { "Class" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "Period ${cell.periodKey} · ${cell.subjectCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cell.type.isNotBlank()) StatusChip(cell.type, ChipTone.Neutral)
                    // The T1/T2 (tutorial) or P1/P2 (practical) group, only when
                    // this class is group-specific.
                    if (!group.isNullOrBlank()) StatusChip("Group $group", ChipTone.Info)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MmmutDimens.iconSm)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        "Room $room",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NoClassCard(hasClassesToday: Boolean, dayName: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.semantic.success,
                modifier = Modifier.size(MmmutDimens.iconLg)
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    if (hasClassesToday) "No more classes today" else "No classes today",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when {
                        hasClassesToday -> "All of today's classes are done."
                        dayName == null -> "Sunday is not a teaching day."
                        else -> "Nothing is scheduled for $dayName."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* -------------------------------------------------------- today's rows ---- */

@Composable
private fun TodayClassRow(
    cell: TimetableCell,
    isCurrent: Boolean,
    isPast: Boolean,
    fallbackRoom: String,
    onClick: () -> Unit
) {
    val room = cell.room.ifBlank { fallbackRoom }.ifBlank { "Not listed" }
    val meta = buildString {
        append("Period ").append(cell.periodKey)
        append(" · Room ").append(room)
        if (cell.type.isNotBlank()) append(" · ").append(cell.type)
    }

    Surface(
        onClick = onClick,
        color = if (isCurrent) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MmmutDimens.minRowHeight)
            .alpha(if (isPast) 0.55f else 1f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (isCurrent) {
                Box(
                    Modifier
                        .width(Spacing.xs)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Row(
                Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.width(MmmutDimens.timeRailWidth)) {
                    Text(
                        cell.start.ifBlank { "—" },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        cell.end.ifBlank { "—" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            cell.subjectName.ifBlank { cell.subjectCode.ifBlank { "Class" } },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isCurrent) {
                            Spacer(Modifier.width(Spacing.sm))
                            StatusChip("Now", ChipTone.Info)
                        }
                    }
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* ----------------------------------------------------------- attendance --- */

@Composable
private fun AttendanceCard(a: HomeAttendanceSummary, onOpenAttendance: () -> Unit) {
    val semantic = MaterialTheme.semantic
    val tone = when {
        !a.hasRecords -> semantic.info
        a.percent >= 75.0 -> semantic.success
        a.percent >= 65.0 -> semantic.warning
        else -> semantic.danger
    }

    Card(
        onClick = onOpenAttendance,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Attendance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Overall",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (!a.hasRecords) {
                Text(
                    "No attendance recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Tap to log today's classes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${String.format(Locale.US, "%.0f", a.percent)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = tone,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "${a.present} of ${a.total} classes attended",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                ThinProgressBar(
                    fraction = (a.percent / 100.0).toFloat(),
                    color = tone
                )

                a.projection?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (a.needsAttention.isNotEmpty()) {
                    MmmutDivider(Modifier.padding(vertical = Spacing.xxs))
                    Text(
                        "Needs attention",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    a.needsAttention.forEach { stat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stat.subjectName.ifBlank { stat.subjectCode },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${stat.present} of ${stat.total} attended · ${stat.subjectCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            StatusChip(
                                text = "${String.format(Locale.US, "%.0f", stat.pct)}%",
                                tone = if (stat.pct >= 65.0) ChipTone.Warning else ChipTone.Danger
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Thin bar (MmmutDimens.barHeight) — the stock indicator is taller than the design allows. */
@Composable
private fun ThinProgressBar(fraction: Float, color: Color) {
    val shape = RoundedCornerShape(MmmutDimens.barHeight / 2)
    Box(
        Modifier
            .fillMaxWidth()
            .height(MmmutDimens.barHeight)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(color)
        )
    }
}

/* ------------------------------------------------------- quick access ----- */

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MmmutDimens.touchTarget),
        contentPadding = PaddingValues(horizontal = Spacing.sm)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(MmmutDimens.iconMd)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/* -------------------------------------------------------------- shared ---- */

@Composable
private fun InlineEmpty(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card)
        )
    }
}

/**
 * Wall-clock "HH:mm", refreshed every 30s so "in progress" and the dimming of
 * finished classes stay correct while the screen is open (the old code sampled
 * the clock once per data load, so a finished class could stay "in progress").
 */
@Composable
private fun rememberClock(): String {
    val now by produceState(initialValue = clockHHmm(), key1 = Unit) {
        while (true) {
            delay(30_000L)
            value = clockHHmm()
        }
    }
    return now
}

private fun clockHHmm(): String = SimpleDateFormat("HH:mm", Locale.US).format(Date())
