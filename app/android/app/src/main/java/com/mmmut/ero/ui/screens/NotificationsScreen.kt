package com.mmmut.ero.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mmmut.ero.notifications.NotificationPrefs
import com.mmmut.ero.ui.viewmodel.NotificationsViewModel
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(onOpen: (String) -> Unit, vm: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val history by vm.history.collectAsState()
    val prefs = remember { NotificationPrefs(ctx) }
    var states by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var hasPerm by remember {
        mutableStateOf(if (Build.VERSION.SDK_INT < 33) true else ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPerm = it }
    LaunchedEffect(Unit) {
        vm.load(ctx)
        val m = mutableMapOf<String, Boolean>()
        prefs.types.forEach { t ->
            m[t] = try { prefs.enabledFlow(t).first() } catch (_: Exception) { true }
        }
        states = m
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Notifications", style = MaterialTheme.typography.titleLarge)
            if (!hasPerm && Build.VERSION.SDK_INT >= 33) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Enable push notifications") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Preferences", style = MaterialTheme.typography.titleSmall)
        }
        items(prefs.types) { t ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t.replaceFirstChar { it.uppercase() })
                Switch(checked = states[t] ?: true, onCheckedChange = { v ->
                    states = states + (t to v)
                    scope.launch { prefs.setEnabled(t, v) }
                })
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("History", style = MaterialTheme.typography.titleSmall)
            if (history.isEmpty()) Text("No notifications received yet on this device.", style = MaterialTheme.typography.bodySmall)
        }
        items(history, key = { it.id + it.receivedAt }) { n ->
            Card(onClick = {
                if (n.refId.isNotBlank()) onOpen(n.refId)
            }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(n.title, style = MaterialTheme.typography.titleSmall)
                    Text(n.body, style = MaterialTheme.typography.bodySmall)
                    Text("${n.type} · ${TimeUtils.formatMillis(n.receivedAt)}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
