package com.mmmut.ero.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.TelegramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramScreen(
    onBack: () -> Unit,
    vm: TelegramViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ctx = LocalContext.current
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()

    var customHandle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Exception) { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telegram Private Access") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(s.message) { vm.load() }
                is UiState.Empty -> EmptyView("No Telegram configuration.")
                is UiState.Success -> {
                    val app = s.data.application
                    val status = app?.status ?: "NOT_APPLIED"

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "MMMUT Official Announcements Channel",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Access official university circulars, semester schedules, exam notices and emergency updates via Telegram.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Status Badge
                                    val (badgeText, badgeColor) = when (status) {
                                        "CHANNEL_APPROVED" -> "Verified Channel Member" to MaterialTheme.colorScheme.primary
                                        "ADMIN_APPROVED", "TELEGRAM_NOT_CONNECTED" -> "Approved by ERP Admin" to MaterialTheme.colorScheme.secondary
                                        "JOIN_REQUEST_SUBMITTED" -> "Join Request Submitted" to MaterialTheme.colorScheme.tertiary
                                        "PENDING_ADMIN_APPROVAL" -> "Under ERP Admin Review" to MaterialTheme.colorScheme.outline
                                        "REJECTED" -> "Application Declined" to MaterialTheme.colorScheme.error
                                        else -> "Not Connected" to MaterialTheme.colorScheme.outline
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(badgeText) },
                                        leadingIcon = {
                                            Icon(
                                                if (status == "CHANNEL_APPROVED") Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = badgeColor)
                                    )
                                }
                            }
                        }

                        msg?.let { feedback ->
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        feedback,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        when (status) {
                            "NOT_APPLIED" -> {
                                item {
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("Step 1: Apply for ERP Access", style = MaterialTheme.typography.titleSmall)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Submit an application to request access to the university's private Telegram channel.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Button(
                                                onClick = { vm.apply() },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text(if (busy) "Submitting…" else "Apply for Telegram Access")
                                            }
                                        }
                                    }
                                }
                            }

                            "PENDING_ADMIN_APPROVAL" -> {
                                item {
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("Application Under Review", style = MaterialTheme.typography.titleSmall)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Your application for Telegram access was submitted and is pending review by the ERP administrator.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            "REJECTED" -> {
                                item {
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("Application Declined", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Your application was declined by the administrator. You may resubmit if needed.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Button(
                                                onClick = { vm.reapply() },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (busy) "Resubmitting…" else "Reapply for Access")
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // ADMIN_APPROVED / TELEGRAM_NOT_CONNECTED / JOIN_REQUEST_SUBMITTED / CHANNEL_APPROVED
                                item {
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("Step 2: Connect Telegram Account", style = MaterialTheme.typography.titleSmall)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Connect your Telegram account using the official Telegram bot deep-link or enter your @username handle.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.height(12.dp))

                                            Button(
                                                onClick = { vm.connectTelegram { url -> openUrl(url) } },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (busy) "Connecting…" else "Connect via Telegram Bot")
                                            }

                                            Spacer(Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(Modifier.height(12.dp))

                                            OutlinedTextField(
                                                value = customHandle,
                                                onValueChange = { customHandle = it },
                                                label = { Text("Telegram @username (optional)") },
                                                placeholder = { Text("e.g. rahul_student") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = { vm.saveHandle(customHandle) },
                                                enabled = !busy && customHandle.isNotBlank(),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Save Handle")
                                            }
                                        }
                                    }
                                }

                                item {
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("Step 3: Join Private Channel", style = MaterialTheme.typography.titleSmall)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Fetch the private invite link to request entry into the university channel.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.height(12.dp))

                                            Button(
                                                onClick = { vm.joinChannel { invite -> openUrl(invite) } },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Get Private Channel Invite")
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            OutlinedButton(
                                                onClick = { vm.checkMembership() },
                                                enabled = !busy,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Check Membership Status")
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
