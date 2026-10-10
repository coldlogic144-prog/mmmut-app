package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.HostelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelScreen(
    onOpen: (String) -> Unit,
    onMessMenu: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    vm: HostelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hostel & Living") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(MmmutDimens.iconMd)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> {
                ListSkeleton(rows = 3, modifier = Modifier.padding(padding))
            }
            is UiState.Error -> {
                ErrorView(
                    message = s.message,
                    modifier = Modifier.padding(padding),
                    onRetry = { vm.load() }
                )
            }
            is UiState.Empty -> {
                EmptyView(
                    title = "No hostel notices yet",
                    message = "Allotment, mess timings and warden notices will appear here.",
                    modifier = Modifier.padding(padding),
                    actionLabel = "View Mess Menu",
                    onAction = onMessMenu
                )
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = Spacing.screen,
                        vertical = Spacing.md
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
                ) {
                    item(key = "info-card") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.card),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                Text(
                                    "Hostel Information",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Official hostel allotment announcements, maintenance updates, and warden notices are published here. Your assigned hostel is listed on your Profile.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Button(
                                    onClick = onMessMenu,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(MmmutDimens.iconSm)
                                    )
                                    Spacer(Modifier.width(Spacing.sm))
                                    Text("View Mess Menu")
                                }
                            }
                        }
                    }

                    item(key = "notices-header") {
                        Text(
                            "Hostel Announcements",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (s.data.isEmpty()) {
                        item(key = "empty-inline") {
                            Text(
                                "No active hostel notices posted at this time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = Spacing.md)
                            )
                        }
                    } else {
                        items(s.data, key = { it.id }) { notice ->
                            NoticeCard(n = notice, onClick = { onOpen(notice.id) })
                        }
                    }
                }
            }
        }
    }
}
