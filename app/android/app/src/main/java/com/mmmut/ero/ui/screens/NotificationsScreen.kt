package com.mmmut.ero.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mmmut.ero.data.model.StoredNotification
import com.mmmut.ero.notifications.NotificationPrefs
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.SectionHeader
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.NotificationsViewModel
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/* ==========================================================================
 * Notification history and preferences
 * --------------------------------------------------------------------------
 * One LazyColumn for the whole body. Taps go through the existing onOpen
 * callback with the id the deep-link layer expects (a notice id for notice
 * types, otherwise the stored reference id), so this screen never invents a
 * route the rest of the app cannot serve.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onOpen: (String) -> Unit,
    vm: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val history by vm.history.collectAsState()
    val prefs = remember { NotificationPrefs(ctx) }

    var enabledTypes by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var confirmClear by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < 33) true
            else ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        vm.load(ctx)
        val loaded = mutableMapOf<String, Boolean>()
        prefs.types.forEach { t ->
            loaded[t] = try { prefs.enabledFlow(t).first() } catch (_: Exception) { true }
        }
        enabledTypes = loaded
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear notification history")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(
                start = Spacing.screen,
                end = Spacing.screen,
                top = Spacing.md,
                bottom = Spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
        ) {
            /* ------------------------------------------------------ push --- */
            if (!hasPermission && Build.VERSION.SDK_INT >= 33) {
                item(key = "permission") {
                    Surface(
                        color = MaterialTheme.semantic.warningContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(Spacing.card),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.semantic.onWarningContainer,
                                    modifier = Modifier.size(MmmutDimens.iconMd)
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                Text(
                                    "Push notifications are off",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.semantic.onWarningContainer
                                )
                            }
                            Text(
                                "Notices, exam alerts and hostel announcements won't reach this device " +
                                    "until you allow notifications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.semantic.onWarningContainer
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = MmmutDimens.touchTarget)
                            ) { Text("Allow notifications") }
                        }
                    }
                }
            }

            /* ------------------------------------------------ preferences --- */
            item(key = "prefs_header") {
                SectionHeader("Alert preferences")
            }

            item(key = "prefs") {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.card),
                        verticalArrangement = Arrangement.spacedBy(Spacing.none)
                    ) {
                        prefs.types.forEachIndexed { index, type ->
                            if (index > 0) MmmutDivider()
                            PreferenceRow(
                                label = type.replaceFirstChar { it.uppercase() },
                                checked = enabledTypes[type] ?: true,
                                onCheckedChange = { value ->
                                    enabledTypes = enabledTypes + (type to value)
                                    scope.launch {
                                        try {
                                            prefs.setEnabled(type, value)
                                        } catch (_: Exception) {
                                            snackbar.showSnackbar("Could not save that preference. Try again.")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            /* ---------------------------------------------------- history --- */
            item(key = "history_header") {
                Row(
                    Modifier.fillMaxWidth().padding(top = Spacing.lg, bottom = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (history.isEmpty()) "History" else "History (${history.size})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { confirmClear = true }) { Text("Clear") }
                    }
                }
            }

            if (history.isEmpty()) {
                item(key = "history_empty") {
                    Box(Modifier.fillMaxWidth().heightIn(min = 240.dp)) {
                        EmptyView(
                            title = "No notifications yet",
                            message = "Alerts you receive on this device will be listed here."
                        )
                    }
                }
            } else {
                items(history, key = { "${it.id}_${it.receivedAt}" }) { n ->
                    NotificationRow(n, onOpen)
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear notification history?") },
            text = {
                Text(
                    "All ${history.size} saved notifications will be removed from this device. " +
                        "The notices themselves are not deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        vm.clearHistory(ctx)
                        scope.launch { snackbar.showSnackbar("Notification history cleared.") }
                    }
                ) { Text("Clear history") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PreferenceRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MmmutDimens.minRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(Spacing.xl))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NotificationRow(n: StoredNotification, onOpen: (String) -> Unit) {
    val target = remember(n.id, n.type, n.refId) { targetIdFor(n) }
    val clickable = target != null

    Card(
        onClick = { target?.let(onOpen) },
        enabled = clickable,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(Spacing.card).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(n.type.ifBlank { "general" }, ChipTone.Neutral)
                Spacer(Modifier.weight(1f))
                Text(
                    TimeUtils.formatMillis(n.receivedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                n.title.ifBlank { "Notification" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (n.body.isNotBlank()) {
                Text(
                    n.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (clickable) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(MmmutDimens.iconSm)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        "Open",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Maps a stored notification to the id `onOpen` accepts (AppNav routes that to
 * the notice detail screen). Notices and events carry a notice id; notifications
 * that have no reference (or a non-notice type) are not tappable rather than
 * routing somewhere meaningless.
 */
private fun targetIdFor(n: StoredNotification): String? {
    val type = n.type.lowercase()
    if (type == "examination" || type == "exam") return null
    if (type == "hostel" && n.refId.isBlank()) return null
    return n.refId.ifBlank { null }
}
