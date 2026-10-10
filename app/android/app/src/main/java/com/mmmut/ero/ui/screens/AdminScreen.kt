package com.mmmut.ero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.repository.TelegramApplicationWithUser
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.InfoRow
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.SectionHeader
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.AdminScreenData
import com.mmmut.ero.ui.viewmodel.AdminSection
import com.mmmut.ero.ui.viewmodel.AdminViewModel
import com.mmmut.ero.ui.viewmodel.TelegramQueueFilter
import com.mmmut.ero.util.TimeUtils

/* ==========================================================================
 * Admin console
 * --------------------------------------------------------------------------
 * Everything here is presentation. The authorization gate lives in
 * AdminViewModel.load() (isAdmin required, unchanged) and `isAdmin` is checked
 * again below as a defensive guard. No existing admin capability was removed:
 * Telegram approvals, roll verification, notice publish/delete and admin
 * request promotion are all still reachable from this screen.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    var section by remember { mutableStateOf(AdminSection.TELEGRAM) }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(msg) {
        val m = msg
        if (m != null) {
            snackbar.showSnackbar(m)
            vm.clearMsg()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin console") },
                actions = {
                    IconButton(onClick = { vm.load() }, enabled = !busy) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (val s = state) {
                is UiState.Loading -> ListSkeleton(rows = 4)
                is UiState.Error -> ErrorView(s.message, onRetry = { vm.load() })
                is UiState.Empty -> EmptyView(
                    "No admin data",
                    "Nothing to manage right now.",
                    actionLabel = "Refresh",
                    onAction = { vm.load() }
                )
                is UiState.Success -> {
                    val d = s.data
                    if (!d.isAdmin) {
                        // Defensive guard only. Gating itself stays in the ViewModel.
                        EmptyView(
                            "Administrator access required",
                            "This account does not have administrator permissions. " +
                                "Contact the administrator if you believe this is wrong."
                        )
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            ConsoleHeader(section = section, onSectionChange = { section = it })
                            SummaryStrip(d)
                            ConsoleBody(
                                d = d,
                                section = section,
                                busy = busy,
                                modifier = Modifier.weight(1f),
                                onApproveTelegram = { vm.approveTelegram(it) },
                                onRejectTelegram = { vm.rejectTelegram(it) },
                                onApproveRoll = { uid, roll -> vm.approveRoll(uid, roll) },
                                onApproveAdminRequest = { id, uid -> vm.approveAdminRequest(id, uid) },
                                onPublishNotice = { t, c, cat, imp, pin -> vm.createNotice(t, c, cat, imp, pin) },
                                onDeleteNotice = { vm.deleteNotice(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* --------------------------------------------------------------- header --- */

@Composable
private fun ConsoleHeader(section: AdminSection, onSectionChange: (AdminSection) -> Unit) {
    val sections = listOf(
        AdminSection.TELEGRAM,
        AdminSection.ROLL_VERIFY,
        AdminSection.PUBLISH_NOTICE,
        AdminSection.ADMIN_REQUESTS
    )
    val titles = listOf("Telegram", "Roll verify", "Publish", "Admin requests")
    ScrollableTabRow(
        selectedTabIndex = sections.indexOf(section).coerceAtLeast(0),
        edgePadding = Spacing.sm,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        sections.forEachIndexed { index, s ->
            Tab(
                selected = section == s,
                onClick = { onSectionChange(s) },
                text = { Text(titles[index], maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
    MmmutDivider()
}

@Composable
private fun SummaryStrip(d: AdminScreenData) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        StatusChip(
            text = "${d.pendingTelegramApps.size} Telegram pending",
            tone = if (d.pendingTelegramApps.isEmpty()) ChipTone.Neutral else ChipTone.Warning
        )
        StatusChip(
            text = "${d.pendingRolls.size} roll checks",
            tone = if (d.pendingRolls.isEmpty()) ChipTone.Neutral else ChipTone.Info
        )
        StatusChip(
            text = "${d.adminRequests.size} admin requests",
            tone = if (d.adminRequests.isEmpty()) ChipTone.Neutral else ChipTone.Info
        )
        StatusChip(text = "${d.notices.size} notices", tone = ChipTone.Neutral)
    }
}

/* ----------------------------------------------------------------- body --- */

@Composable
private fun ConsoleBody(
    d: AdminScreenData,
    section: AdminSection,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onApproveTelegram: (String) -> Unit,
    onRejectTelegram: (String) -> Unit,
    onApproveRoll: (String, String) -> Unit,
    onApproveAdminRequest: (String, String) -> Unit,
    onPublishNotice: (String, String, String, Boolean, Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    when (section) {
        AdminSection.TELEGRAM -> TelegramQueue(d, busy, modifier, onApproveTelegram, onRejectTelegram)
        AdminSection.ROLL_VERIFY -> RollQueue(d.pendingRolls, busy, modifier, onApproveRoll)
        AdminSection.PUBLISH_NOTICE -> NoticeConsole(d.notices, busy, modifier, onPublishNotice, onDeleteNotice)
        AdminSection.ADMIN_REQUESTS -> AdminRequestQueue(d, busy, modifier, onApproveAdminRequest)
    }
}

/* -------------------------------------------------------- telegram queue --- */

@Composable
private fun TelegramQueue(
    d: AdminScreenData,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    var filter by remember { mutableStateOf(TelegramQueueFilter.PENDING) }
    var rejectTarget by remember { mutableStateOf<TelegramApplicationWithUser?>(null) }

    val rows = when (filter) {
        TelegramQueueFilter.PENDING -> d.pendingTelegramApps
        TelegramQueueFilter.DECIDED -> d.decidedTelegramApps
        TelegramQueueFilter.ALL -> d.telegramApps
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        item(key = "tg_filters") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterChip(
                    selected = filter == TelegramQueueFilter.PENDING,
                    onClick = { filter = TelegramQueueFilter.PENDING },
                    label = { Text("Pending (${d.pendingTelegramApps.size})") }
                )
                FilterChip(
                    selected = filter == TelegramQueueFilter.DECIDED,
                    onClick = { filter = TelegramQueueFilter.DECIDED },
                    label = { Text("Decided (${d.decidedTelegramApps.size})") }
                )
                FilterChip(
                    selected = filter == TelegramQueueFilter.ALL,
                    onClick = { filter = TelegramQueueFilter.ALL },
                    label = { Text("All (${d.telegramApps.size})") }
                )
            }
        }

        if (rows.isEmpty()) {
            item(key = "tg_empty") {
                Box(Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
                    EmptyView(
                        title = if (filter == TelegramQueueFilter.PENDING) {
                            "No pending applications"
                        } else {
                            "Nothing here"
                        },
                        message = if (filter == TelegramQueueFilter.PENDING) {
                            "Every Telegram access request has been reviewed."
                        } else {
                            "No applications match this filter."
                        }
                    )
                }
            }
        } else {
            items(rows, key = { it.uid }) { app ->
                TelegramQueueRow(
                    app = app,
                    busy = busy,
                    onApprove = { onApprove(app.uid) },
                    onReject = { rejectTarget = app }
                )
            }
        }
    }

    val target = rejectTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("Reject this application?") },
            text = {
                Text(
                    "Rejecting removes Telegram access for ${target.name.ifBlank { target.uid }}" +
                        (if (target.rollNumber.isNotBlank()) " (${target.rollNumber})" else "") +
                        ". The student can resubmit, but they lose access until you approve a new request."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        rejectTarget = null
                        onReject(target.uid)
                    }
                ) { Text("Reject application") }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TelegramQueueRow(
    app: TelegramApplicationWithUser,
    busy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        app.name.ifBlank { app.uid },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "@${app.username.ifBlank { app.uid }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                StatusChip(telegramStatusLabel(app.status), telegramStatusTone(app.status))
            }

            if (app.rollNumber.isNotBlank()) {
                InfoRow("Roll number", app.rollNumber)
            }
            if (!app.telegramUsername.isNullOrBlank()) {
                InfoRow("Telegram handle", "@${app.telegramUsername}")
            }
            if (app.appliedAt > 0L) {
                InfoRow("Applied on", TimeUtils.formatMillis(app.appliedAt))
            }

            if (app.status == "PENDING_ADMIN_APPROVAL") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Button(
                        onClick = onApprove,
                        enabled = !busy,
                        modifier = Modifier.weight(1f).heightIn(min = MmmutDimens.touchTarget)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !busy,
                        modifier = Modifier.weight(1f).heightIn(min = MmmutDimens.touchTarget)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Reject")
                    }
                }
            }
        }
    }
}

private fun telegramStatusLabel(status: String): String = when (status) {
    "PENDING_ADMIN_APPROVAL" -> "Pending"
    "ADMIN_APPROVED" -> "Approved"
    "ADMIN_REJECTED", "REJECTED" -> "Rejected"
    "CHANNEL_APPROVED" -> "In channel"
    "TELEGRAM_NOT_CONNECTED" -> "Not linked"
    else -> status.replace('_', ' ')
}

private fun telegramStatusTone(status: String): ChipTone = when (status) {
    "PENDING_ADMIN_APPROVAL" -> ChipTone.Warning
    "ADMIN_APPROVED", "CHANNEL_APPROVED" -> ChipTone.Success
    "ADMIN_REJECTED", "REJECTED" -> ChipTone.Danger
    else -> ChipTone.Neutral
}

/* ------------------------------------------------------------ roll queue --- */

@Composable
private fun RollQueue(
    rows: List<StudentProfile>,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onApprove: (String, String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        if (rows.isEmpty()) {
            item(key = "roll_empty") {
                Box(Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
                    EmptyView(
                        "No roll verification requests",
                        "Roll numbers are verified as students submit them."
                    )
                }
            }
        } else {
            items(rows, key = { "roll_${it.uid}" }) { user ->
                val rollToVerify = user.pendingRollNumber.ifBlank { user.rollNumber }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(Spacing.card),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    user.name.ifBlank { user.uid },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "@${user.username.ifBlank { user.uid }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            StatusChip(
                                if (user.migrationStatus.lowercase() == "pending") "Migrating" else "Unverified",
                                ChipTone.Warning
                            )
                        }
                        InfoRow("Branch / section", "${user.branchId.uppercase()} · Section ${user.section}")
                        InfoRow("Requested roll", rollToVerify.ifBlank { "Not provided" }, emphasize = true)
                        InfoRow("Current roll", user.rollNumber)
                        if (user.migrationReviewReason.isNotBlank()) {
                            InfoRow("Review note", user.migrationReviewReason)
                        }

                        if (rollToVerify.isNotBlank()) {
                            Button(
                                onClick = { onApprove(user.uid, rollToVerify) },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = MmmutDimens.touchTarget)
                            ) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                                Spacer(Modifier.width(Spacing.xs))
                                Text("Verify roll number")
                            }
                        } else {
                            Text(
                                "This account has no roll number to verify yet.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/* --------------------------------------------------------- notice console --- */

@Composable
private fun NoticeConsole(
    notices: List<Notice>,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onPublish: (String, String, String, Boolean, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("general") }
    var important by remember { mutableStateOf(false) }
    var pinned by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Notice?>(null) }

    val canPublish = title.isNotBlank() && content.isNotBlank() && !busy

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        item(key = "publish_form") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text("Publish a campus notice", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        supportingText = { Text("general, academic, examination, hostel, events") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = important, onCheckedChange = { important = it })
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text("Mark as urgent", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Shown with an urgent tag and pushed to everyone.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pinned, onCheckedChange = { pinned = it })
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text("Pin to top", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Stays at the top of the notices feed.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onPublish(title.trim(), content.trim(), category.trim(), important, pinned)
                            title = ""
                            content = ""
                            important = false
                            pinned = false
                        },
                        enabled = canPublish,
                        modifier = Modifier.fillMaxWidth().heightIn(min = MmmutDimens.touchTarget)
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(MmmutDimens.iconMd),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Publish notice")
                    }
                    if (!canPublish && !busy) {
                        Text(
                            "A title and some content are required before publishing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "existing_header") {
            SectionHeader("Published notices (${notices.size})")
        }

        if (notices.isEmpty()) {
            item(key = "notices_empty") {
                Box(Modifier.fillMaxWidth().heightIn(min = 180.dp)) {
                    EmptyView("No notices published", "Anything you publish appears here and in the student feed.")
                }
            }
        } else {
            items(notices, key = { "notice_${it.id}" }) { n ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(Spacing.card).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                n.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                n.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                StatusChip(n.category.ifBlank { "general" }, ChipTone.Neutral)
                                if (n.important) StatusChip("Urgent", ChipTone.Danger)
                                if (n.pinned) StatusChip("Pinned", ChipTone.Info)
                            }
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                TimeUtils.formatMillis(n.createdAtMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        IconButton(
                            onClick = { deleteTarget = n },
                            enabled = !busy,
                            modifier = Modifier.size(MmmutDimens.touchTarget)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete notice",
                                tint = MaterialTheme.semantic.danger
                            )
                        }
                    }
                }
            }
        }
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this notice?") },
            text = {
                Text(
                    "\"${target.title}\" will be removed for every student immediately. " +
                        "This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        onDelete(target.id)
                    }
                ) { Text("Delete notice") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

/* --------------------------------------------------- admin request queue --- */

@Composable
private fun AdminRequestQueue(
    d: AdminScreenData,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onApprove: (String, String) -> Unit
) {
    var promoteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var promoteLabel by remember { mutableStateOf("") }

    val hasNotices = d.notices.isNotEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        item(key = "admin_warning") {
            Surface(
                color = MaterialTheme.semantic.warningContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.semantic.onWarningContainer,
                        modifier = Modifier.size(MmmutDimens.iconMd)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "Promoting an account grants full console access, including approving other " +
                            "students. Only promote people you can identify.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.semantic.onWarningContainer
                    )
                }
            }
        }

        if (d.adminRequests.isEmpty()) {
            item(key = "admin_empty") {
                Box(Modifier.fillMaxWidth().heightIn(min = 200.dp)) {
                    EmptyView("No admin requests", "Students who request admin access appear here.")
                }
            }
        } else {
            items(d.adminRequests, key = { "req_${it.id}" }) { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(Spacing.card),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    req.name.ifBlank { req.uid },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "@${req.username.ifBlank { req.uid }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            StatusChip("Pending", ChipTone.Warning)
                        }
                        InfoRow("UID", req.uid)
                        if (req.requestedAt > 0L) {
                            InfoRow("Requested on", TimeUtils.formatMillis(req.requestedAt))
                        }
                        Button(
                            onClick = {
                                promoteTarget = req.id to req.uid
                                promoteLabel = req.name.ifBlank { req.username.ifBlank { req.uid } }
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                            Spacer(Modifier.width(Spacing.xs))
                            Text("Promote to admin")
                        }
                    }
                }
            }
        }

        item(key = "admin_tools") {
            Column {
                SectionHeader("Other admin tools")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(Spacing.card), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        ToolRow(
                            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            title = "Publish or delete notices",
                            subtitle = if (hasNotices) {
                                "${d.notices.size} notices are currently live."
                            } else {
                                "No notices are live right now."
                            }
                        )
                        MmmutDivider()
                        ToolRow(
                            icon = { Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            title = "Roll number verification",
                            subtitle = "${d.pendingRolls.size} requests awaiting a decision."
                        )
                        MmmutDivider()
                        ToolRow(
                            icon = { Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            title = "Telegram access queue",
                            subtitle = "${d.pendingTelegramApps.size} applications awaiting a decision."
                        )
                    }
                }
            }
        }
    }

    val target = promoteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { promoteTarget = null },
            title = { Text("Promote to administrator?") },
            text = {
                Text(
                    "$promoteLabel will get full access to this console, including approving " +
                        "Telegram applications, verifying roll numbers and publishing notices."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        promoteTarget = null
                        onApprove(target.first, target.second)
                    }
                ) { Text("Promote") }
            },
            dismissButton = {
                TextButton(onClick = { promoteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ToolRow(icon: @Composable () -> Unit, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(MmmutDimens.touchTarget)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
