package com.mmmut.ero.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.InfoRow
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.SectionHeader
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.TelegramAccessPhase
import com.mmmut.ero.ui.viewmodel.TelegramAccessState
import com.mmmut.ero.ui.viewmodel.TelegramStepState
import com.mmmut.ero.ui.viewmodel.TelegramViewModel

/* ==========================================================================
 * Telegram private-channel access
 * --------------------------------------------------------------------------
 * Every branch below is driven by TelegramAccessState, which is derived in
 * TelegramViewModel from the real Firestore status plus the bot-token poll.
 * There is deliberately no branch that claims "connected" from anything other
 * than the backend (status CHANNEL_APPROVED / isMember, or an observed LINKED
 * token), and while the status is being fetched we render a loading or
 * explicit unknown state instead of guessing.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramScreen(
    onBack: () -> Unit,
    vm: TelegramViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val state by vm.state.collectAsState()
    val access by vm.access.collectAsState()
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()

    var handle by remember { mutableStateOf("") }
    var confirmReapply by remember { mutableStateOf(false) }
    var openFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(msg) {
        val m = msg
        if (m != null) {
            snackbar.showSnackbar(m)
            vm.clearMsg()
        }
    }
    DisposableEffect(Unit) {
        onDispose { vm.stopPolling() }
    }
    fun openUrl(url: String) {
        try {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            openFailed = true
        } catch (_: Exception) {
            openFailed = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telegram Access") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (val s = state) {
                is UiState.Loading -> ListSkeleton(rows = 3)
                is UiState.Error -> ErrorView(s.message, onRetry = { vm.load() })
                is UiState.Empty -> EmptyView("Telegram access unavailable")
                is UiState.Success -> {
                    val a = access
                    if (a == null) {
                        // Status not resolved yet. Show an explicit unknown state;
                        // never a default that implies connected or not applied.
                        ListSkeleton(rows = 3)
                    } else {
                        TelegramBody(
                            username = s.data.username,
                            access = a,
                            busy = busy,
                            handle = handle,
                            onHandleChange = { handle = it },
                            onApply = { vm.apply() },
                            onReapply = { confirmReapply = true },
                            onSaveHandle = { vm.saveHandle(handle) },
                            onConnect = { vm.connectTelegram { url -> openUrl(url) } },
                            onJoinChannel = { vm.joinChannel { url -> openUrl(url) } },
                            onCheckMembership = { vm.checkMembership() },
                            onReload = { vm.load() }
                        )
                    }
                }
            }
        }
    }

    if (confirmReapply) {
        AlertDialog(
            onDismissRequest = { confirmReapply = false },
            title = { Text("Resubmit application?") },
            text = {
                Text(
                    "This replaces your current declined application with a new request. " +
                        "An administrator will review it again."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReapply = false
                        vm.reapply()
                    }
                ) { Text("Resubmit") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReapply = false }) { Text("Cancel") }
            }
        )
    }

    if (openFailed) {
        AlertDialog(
            onDismissRequest = { openFailed = false },
            title = { Text("Couldn't open Telegram") },
            text = { Text("No app on this device can open that link. Install Telegram and try again.") },
            confirmButton = {
                TextButton(onClick = { openFailed = false }) { Text("OK") }
            }
        )
    }
}

/* ------------------------------------------------------------------ body --- */

@Composable
private fun TelegramBody(
    username: String,
    access: TelegramAccessState,
    busy: Boolean,
    handle: String,
    onHandleChange: (String) -> Unit,
    onApply: () -> Unit,
    onReapply: () -> Unit,
    onSaveHandle: () -> Unit,
    onConnect: () -> Unit,
    onJoinChannel: () -> Unit,
    onCheckMembership: () -> Unit,
    onReload: () -> Unit
) {
    val label = phaseLabel(access)
    val tone = phaseTone(access)
    val explanation = phaseExplanation(access)
    val title = phaseTitle(access)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        /* ---------------------------------------------------------- header -- */
        item(key = "status") {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        StatusChip(label, tone)
                    }
                    Text(
                        explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        /* -------------------------------------------------- load-failure --- */
        if (access.loadFailed) {
            item(key = "load_failed") {
                NoticeBlock(
                    text = "We couldn't reach the server to check your Telegram access, so the " +
                        "status above is unknown. Retry to load the real state.",
                    tone = ChipTone.Warning
                )
            }
            item(key = "load_failed_action") {
                Button(onClick = onReload, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Retry")
                }
            }
        } else {
            /* ------------------------------------------------------ action -- */
            item(key = "action") {
                PrimaryAction(
                    access = access,
                    busy = busy,
                    handle = handle,
                    onHandleChange = onHandleChange,
                    onApply = onApply,
                    onReapply = onReapply,
                    onSaveHandle = onSaveHandle,
                    onConnect = onConnect,
                    onJoinChannel = onJoinChannel,
                    onCheckMembership = onCheckMembership,
                    onReload = onReload
                )
            }

            TelegramProgressSections(access, username)
        }
    }
}

/**
 * Stepper, details and channel blurb. Deliberately not rendered when the status
 * could not be loaded: showing "Not applied" steps for an unknown state would be
 * an invented state.
 */
private fun LazyListScope.TelegramProgressSections(
    access: TelegramAccessState,
    username: String
) {
    /* ---------------------------------------------------------- stepper -- */
    item(key = "stepper") {
        Column {
            SectionHeader("Your progress")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.none)
                ) {
                    StepperRow(
                        index = 1,
                        title = "Application submitted",
                        subtitle = stepSubtitle(access, 1),
                        state = access.steps.getOrElse(0) { TelegramStepState.TODO },
                        showConnector = true
                    )
                    StepperRow(
                        index = 2,
                        title = "ERP admin approval",
                        subtitle = stepSubtitle(access, 2),
                        state = access.steps.getOrElse(1) { TelegramStepState.TODO },
                        showConnector = true
                    )
                    StepperRow(
                        index = 3,
                        title = "Telegram account linked",
                        subtitle = stepSubtitle(access, 3),
                        state = access.steps.getOrElse(2) { TelegramStepState.TODO },
                        showConnector = true
                    )
                    StepperRow(
                        index = 4,
                        title = "Channel membership verified",
                        subtitle = stepSubtitle(access, 4),
                        state = access.steps.getOrElse(3) { TelegramStepState.TODO },
                        showConnector = false
                    )
                }
            }
        }
    }

    /* ---------------------------------------------------------- details -- */
    item(key = "details") {
        Column {
            SectionHeader("Details")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = Spacing.card, vertical = Spacing.sm)) {
                    InfoRow("ERP account", username)
                    InfoRow("Application status", rawStatusLabel(access))
                    InfoRow(
                        "Telegram account",
                        if (access.telegramLinked) "Linked" else "Not linked yet"
                    )
                    InfoRow(
                        "Channel membership",
                        if (access.channelVerified) "Verified" else "Not verified"
                    )
                    InfoRow("Telegram handle", access.application?.telegramUsername)
                }
            }
        }
    }

    /* ------------------------------------------------------- what next --- */
    item(key = "about") {
        Column {
            SectionHeader("About this channel")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Bullet("Official circulars, semester schedules, exam notices and emergency updates.")
                    Bullet("Access is restricted to verified MMMUT students; applications are reviewed by the ERP administrator.")
                    Bullet("Linking your Telegram account shares only your Telegram user id with the ERP bot.")
                }
            }
        }
    }

    item(key = "privacy") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(MmmutDimens.iconSm),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "Your Telegram account is never posted publicly and the bot cannot post on your behalf.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* --------------------------------------------------------------- action --- */

@Composable
private fun PrimaryAction(
    access: TelegramAccessState,
    busy: Boolean,
    handle: String,
    onHandleChange: (String) -> Unit,
    onApply: () -> Unit,
    onReapply: () -> Unit,
    onSaveHandle: () -> Unit,
    onConnect: () -> Unit,
    onJoinChannel: () -> Unit,
    onCheckMembership: () -> Unit,
    onReload: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            when (access.phase) {
                TelegramAccessPhase.NOT_APPLIED -> {
                    Text("Apply for access", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Submit a request to the ERP administrator. You will be able to link " +
                            "Telegram once it is approved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onApply, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        if (busy) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(MmmutDimens.iconMd),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Submitting…" else "Apply for Telegram access")
                    }
                }

                TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL -> {
                    Text("Waiting for approval", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Your application is in the administrator's queue. Nothing else is needed " +
                            "from you right now — this screen updates as soon as it is reviewed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onReload, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Checking…" else "Refresh status")
                    }
                }

                TelegramAccessPhase.REJECTED -> {
                    Text("Application declined", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "The administrator declined this request. You can resubmit it for another review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onReapply, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Resubmitting…" else "Resubmit application")
                    }
                }

                TelegramAccessPhase.APPLICATION_APPROVED -> {
                    Text("Link your Telegram account", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Tap below to open the official ERP bot in Telegram and press Start. " +
                            "We then wait for Telegram to confirm the link — nothing is connected until it does.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onConnect, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        if (busy) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(MmmutDimens.iconMd),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Opening…" else "Connect via Telegram bot")
                    }
                    HandleFallback(handle, onHandleChange, onSaveHandle, busy)
                }

                TelegramAccessPhase.CONNECT_TELEGRAM -> {
                    Text("Finish linking in Telegram", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "The bot link was opened, but Telegram has not confirmed the connection yet. " +
                            "Press Start in the bot, then check again below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onConnect, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Opening…" else "Open Telegram bot link again")
                    }
                    OutlinedButton(onClick = onReload, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("I've pressed Start — check again")
                    }
                    HandleFallback(handle, onHandleChange, onSaveHandle, busy)
                }

                TelegramAccessPhase.TELEGRAM_CONNECTED,
                TelegramAccessPhase.CHANNEL_VERIFICATION -> {
                    Text("Verify channel membership", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Your Telegram account is linked. Open the private channel invite in " +
                            "Telegram, request to join, then run the membership check here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onJoinChannel, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Fetching link…" else "Open private channel invite")
                    }
                    OutlinedButton(onClick = onCheckMembership, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        if (busy) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(MmmutDimens.iconMd)
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Checking…" else "Check membership")
                    }
                }

                TelegramAccessPhase.ACCESS_ACTIVE -> {
                    Text("Access active", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "You are verified in the MMMUT private channel. Announcements arrive there " +
                            "as well as in this app's notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onJoinChannel, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Open Telegram channel")
                    }
                    OutlinedButton(onClick = onCheckMembership, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Checking…" else "Re-check membership")
                    }
                }

                TelegramAccessPhase.UNKNOWN -> {
                    Text("Status needs checking", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "We could not map the status the server returned for your application, so " +
                            "no access is assumed. Refresh to fetch it again, or contact the administrator.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onReload, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(MmmutDimens.iconMd))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(if (busy) "Checking…" else "Refresh status")
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleFallback(
    handle: String,
    onHandleChange: (String) -> Unit,
    onSaveHandle: () -> Unit,
    busy: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        MmmutDividerSpacer()
        Text(
            "Bot link not working? Save your Telegram @username instead and the administrator " +
                "can match it manually.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = handle,
            onValueChange = onHandleChange,
            label = { Text("Telegram @username") },
            placeholder = { Text("e.g. rahul_student") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = onSaveHandle,
            enabled = !busy && handle.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save handle") }
    }
}

@Composable
private fun MmmutDividerSpacer() {
    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/* -------------------------------------------------------------- stepper --- */

@Composable
private fun StepperRow(
    index: Int,
    title: String,
    subtitle: String,
    state: TelegramStepState,
    showConnector: Boolean
) {
    val s = MaterialTheme.semantic
    val markerColor = when (state) {
        TelegramStepState.DONE -> s.success
        TelegramStepState.CURRENT -> MaterialTheme.colorScheme.primary
        TelegramStepState.TODO -> MaterialTheme.colorScheme.outlineVariant
    }
    val onMarker = when (state) {
        TelegramStepState.DONE -> s.onSuccess
        TelegramStepState.CURRENT -> MaterialTheme.colorScheme.onPrimary
        TelegramStepState.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(markerColor),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    TelegramStepState.DONE -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = onMarker,
                        modifier = Modifier.size(MmmutDimens.iconSm)
                    )
                    TelegramStepState.CURRENT -> Text(
                        index.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = onMarker
                    )
                    TelegramStepState.TODO -> Text(
                        index.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = onMarker
                    )
                }
            }
            if (showConnector) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(Spacing.xxl)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(Modifier.width(Spacing.md))

        Column(
            Modifier
                .weight(1f)
                .padding(bottom = if (showConnector) Spacing.md else Spacing.none)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = when (state) {
                    TelegramStepState.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
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

/* --------------------------------------------------------------- blocks --- */

@Composable
private fun NoticeBlock(text: String, tone: ChipTone) {
    val s = MaterialTheme.semantic
    val (bg, fg) = when (tone) {
        ChipTone.Warning -> s.warningContainer to s.onWarningContainer
        ChipTone.Danger -> s.dangerContainer to s.onDangerContainer
        ChipTone.Success -> s.successContainer to s.onSuccessContainer
        ChipTone.Info -> s.infoContainer to s.onInfoContainer
        ChipTone.Neutral -> s.neutralContainer to s.onNeutralContainer
    }
    Surface(color = bg, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(MmmutDimens.iconMd),
                tint = fg
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(text, style = MaterialTheme.typography.bodySmall, color = fg)
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* -------------------------------------------------------------- mapping --- */

private fun phaseTitle(a: TelegramAccessState): String = when {
    a.loadFailed -> "Status unknown"
    else -> when (a.phase) {
        TelegramAccessPhase.NOT_APPLIED -> "Not applied yet"
        TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL -> "Waiting for admin approval"
        TelegramAccessPhase.APPLICATION_APPROVED -> "Application approved"
        TelegramAccessPhase.CONNECT_TELEGRAM -> "Connect Telegram"
        TelegramAccessPhase.TELEGRAM_CONNECTED -> "Telegram connected"
        TelegramAccessPhase.CHANNEL_VERIFICATION -> "Channel verification"
        TelegramAccessPhase.ACCESS_ACTIVE -> "Access active"
        TelegramAccessPhase.REJECTED -> "Application declined"
        TelegramAccessPhase.UNKNOWN -> "Status unknown"
    }
}

private fun phaseLabel(a: TelegramAccessState): String = when {
    a.loadFailed -> "Unknown"
    else -> when (a.phase) {
        TelegramAccessPhase.NOT_APPLIED -> "Not applied"
        TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL -> "Pending admin"
        TelegramAccessPhase.APPLICATION_APPROVED -> "Approved"
        TelegramAccessPhase.CONNECT_TELEGRAM -> "Not connected"
        TelegramAccessPhase.TELEGRAM_CONNECTED -> "Connected"
        TelegramAccessPhase.CHANNEL_VERIFICATION -> "Verifying"
        TelegramAccessPhase.ACCESS_ACTIVE -> "Active"
        TelegramAccessPhase.REJECTED -> "Declined"
        TelegramAccessPhase.UNKNOWN -> "Unknown"
    }
}

private fun phaseTone(a: TelegramAccessState): ChipTone = when {
    a.loadFailed -> ChipTone.Warning
    else -> when (a.phase) {
        TelegramAccessPhase.ACCESS_ACTIVE -> ChipTone.Success
        TelegramAccessPhase.APPLICATION_APPROVED,
        TelegramAccessPhase.TELEGRAM_CONNECTED -> ChipTone.Info
        TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL,
        TelegramAccessPhase.CONNECT_TELEGRAM,
        TelegramAccessPhase.CHANNEL_VERIFICATION -> ChipTone.Warning
        TelegramAccessPhase.REJECTED -> ChipTone.Danger
        TelegramAccessPhase.NOT_APPLIED,
        TelegramAccessPhase.UNKNOWN -> ChipTone.Neutral
    }
}

private fun phaseExplanation(a: TelegramAccessState): String = when {
    a.loadFailed -> "We could not read your Telegram access status from the server. " +
        "Nothing is assumed until it loads successfully."
    else -> when (a.phase) {
        TelegramAccessPhase.NOT_APPLIED ->
            "You have not requested private-channel access yet. Apply and an administrator will review it."
        TelegramAccessPhase.WAITING_FOR_ADMIN_APPROVAL ->
            "Your application is queued for an administrator. Once approved, you will be asked to link your Telegram account."
        TelegramAccessPhase.APPLICATION_APPROVED ->
            "An administrator approved your application. Next you must link your Telegram account to the ERP bot."
        TelegramAccessPhase.CONNECT_TELEGRAM ->
            "A bot link was opened but Telegram has not confirmed it yet, so your account is not linked."
        TelegramAccessPhase.TELEGRAM_CONNECTED ->
            "Your Telegram account is linked. Channel membership is not verified yet, so channel content is still locked."
        TelegramAccessPhase.CHANNEL_VERIFICATION ->
            "Your account is linked but the server has not confirmed that you joined the private channel. " +
                "Open the invite in Telegram and join, then check membership."
        TelegramAccessPhase.ACCESS_ACTIVE ->
            "You are a verified member of the private channel and will receive its announcements."
        TelegramAccessPhase.REJECTED ->
            "An administrator declined this application. You can resubmit it for another review."
        TelegramAccessPhase.UNKNOWN ->
            "The server returned a status this app does not recognise. No access is assumed."
    }
}

private fun rawStatusLabel(a: TelegramAccessState): String {
    val raw = a.application?.status
    return when {
        a.loadFailed -> "Could not load"
        raw.isNullOrBlank() -> "Not applied"
        raw == "NOT_APPLIED" -> "Not applied"
        else -> raw.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun stepSubtitle(a: TelegramAccessState, step: Int): String = when (step) {
    1 -> when (a.steps.getOrElse(0) { TelegramStepState.TODO }) {
        TelegramStepState.DONE -> "Submitted"
        else -> "Not submitted"
    }
    2 -> when (a.steps.getOrElse(1) { TelegramStepState.TODO }) {
        TelegramStepState.DONE -> "Approved by an administrator"
        TelegramStepState.CURRENT -> "Waiting for an administrator"
        TelegramStepState.TODO -> "Not reviewed yet"
    }
    3 -> when (a.steps.getOrElse(2) { TelegramStepState.TODO }) {
        TelegramStepState.DONE ->
            a.application?.telegramUsername?.let { "@$it" } ?: "Linked"
        else -> "Open the ERP bot and press Start"
    }
    else -> when (a.steps.getOrElse(3) { TelegramStepState.TODO }) {
        TelegramStepState.DONE -> "Verified channel member"
        TelegramStepState.CURRENT -> "Request access in the Telegram channel"
        TelegramStepState.TODO -> "Available after linking Telegram"
    }
}
