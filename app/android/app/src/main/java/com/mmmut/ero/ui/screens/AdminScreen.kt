package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.AdminViewModel

@Composable
fun AdminScreen(
    vm: AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()
    var tab by remember { mutableStateOf(0) }

    var noticeTitle by remember { mutableStateOf("") }
    var noticeContent by remember { mutableStateOf("") }
    var noticeCategory by remember { mutableStateOf("general") }
    var noticeImportant by remember { mutableStateOf(false) }
    var noticePinned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Column(Modifier.padding(16.dp)) {
                Text("MMMUT ERP Admin Control Panel", style = MaterialTheme.typography.titleLarge)
                Text("Administrative tools for Telegram access, student roll verification, and notices.", style = MaterialTheme.typography.bodySmall)
            }
        }

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Telegram") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Roll Verify") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Publish Notice") })
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Admin Reqs") })
        }

        msg?.let { feedback ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(feedback, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }

        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(s.message) { vm.load() }
            is UiState.Empty -> EmptyView("No admin data.")
            is UiState.Success -> {
                val d = s.data
                when (tab) {
                    0 -> { // Telegram Applications
                        if (d.telegramApps.isEmpty()) {
                            EmptyView("No Telegram access applications.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(d.telegramApps, key = { it.uid }) { app ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("${app.name} (@${app.username})", style = MaterialTheme.typography.titleSmall)
                                            if (app.rollNumber.isNotBlank()) Text("Roll: ${app.rollNumber}", style = MaterialTheme.typography.bodySmall)
                                            Text("Status: ${app.status}", style = MaterialTheme.typography.labelSmall)
                                            if (!app.telegramUsername.isNullOrBlank()) Text("Telegram Handle: @${app.telegramUsername}", style = MaterialTheme.typography.bodySmall)

                                            if (app.status == "PENDING_ADMIN_APPROVAL") {
                                                Spacer(Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = { vm.approveTelegram(app.uid) },
                                                        enabled = !busy,
                                                        modifier = Modifier.weight(1f)
                                                    ) { Text("Approve") }

                                                    OutlinedButton(
                                                        onClick = { vm.rejectTelegram(app.uid) },
                                                        enabled = !busy,
                                                        modifier = Modifier.weight(1f)
                                                    ) { Text("Reject") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Roll Verification
                        if (d.pendingRolls.isEmpty()) {
                            EmptyView("No pending roll verification requests.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(d.pendingRolls, key = { it.uid }) { user ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("${user.name} (@${user.username})", style = MaterialTheme.typography.titleSmall)
                                            Text("Branch: ${user.branchId.uppercase()} · Section: ${user.section}", style = MaterialTheme.typography.bodySmall)
                                            val rollToVerify = user.pendingRollNumber.ifBlank { user.rollNumber }
                                            Text("Requested Roll: ${rollToVerify.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)

                                            if (rollToVerify.isNotBlank()) {
                                                Spacer(Modifier.height(8.dp))
                                                Button(
                                                    onClick = { vm.approveRoll(user.uid, rollToVerify) },
                                                    enabled = !busy,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) { Text("Verify Roll Number") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Publish Notice
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Publish Campus Announcement", style = MaterialTheme.typography.titleSmall)
                                        OutlinedTextField(noticeTitle, { noticeTitle = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(noticeContent, { noticeContent = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(noticeCategory, { noticeCategory = it }, label = { Text("Category (general, academic, examination, hostel)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(noticeImportant, { noticeImportant = it })
                                            Text("Mark as Important / Urgent")
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(noticePinned, { noticePinned = it })
                                            Text("Pin to Top of Feed")
                                        }

                                        Button(
                                            onClick = {
                                                if (noticeTitle.isNotBlank() && noticeContent.isNotBlank()) {
                                                    vm.createNotice(noticeTitle, noticeContent, noticeCategory, noticeImportant, noticePinned)
                                                    noticeTitle = ""; noticeContent = ""
                                                }
                                            },
                                            enabled = !busy && noticeTitle.isNotBlank() && noticeContent.isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Publish Notice")
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Existing Notices", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                            }

                            items(d.notices, key = { it.id }) { n ->
                                Card(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(n.title, style = MaterialTheme.typography.titleSmall)
                                            Text("${n.category.uppercase()} · ${n.content}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                        }
                                        IconButton(onClick = { vm.deleteNotice(n.id) }, enabled = !busy) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Admin Requests
                        if (d.adminRequests.isEmpty()) {
                            EmptyView("No pending admin requests.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(d.adminRequests, key = { it.id }) { req ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("${req.name} (@${req.username})", style = MaterialTheme.typography.titleSmall)
                                            Text("UID: ${req.uid}", style = MaterialTheme.typography.labelSmall)
                                            Spacer(Modifier.height(8.dp))
                                            Button(
                                                onClick = { vm.approveAdminRequest(req.id, req.uid) },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) { Text("Promote to Admin") }
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
