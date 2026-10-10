package com.mmmut.ero.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.NoticeCard
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.NoticesViewModel

/* ==========================================================================
 * Notices feed
 * --------------------------------------------------------------------------
 * One LazyColumn for the whole body (header controls are items, so there is no
 * nested same-axis scrolling). Unread dots are only rendered when the data
 * carries unread state — `Notice` has no unread field and NoticesViewModel
 * exposes no read marker, so none is faked here.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    onOpen: (String) -> Unit,
    vm: NoticesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ALL_CATEGORIES) }

    LaunchedEffect(Unit) { vm.load() }

    // Categories are derived from what the backend actually returned.
    val availableCategories = remember(state) {
        val s = state
        if (s is UiState.Success) {
            s.data.map { it.category.lowercase().ifBlank { "general" } }.distinct().sorted()
        } else {
            emptyList()
        }
    }

    val filtered = remember(state, query, category) {
        val s = state
        if (s !is UiState.Success) {
            emptyList()
        } else {
            val q = query.trim()
            s.data.filter { n ->
                val cat = n.category.lowercase().ifBlank { "general" }
                val catOk = category == ALL_CATEGORIES || cat == category ||
                    (category == "examination" && cat == "exam")
                val qOk = q.isBlank() ||
                    n.title.contains(q, ignoreCase = true) ||
                    n.content.contains(q, ignoreCase = true)
                catOk && qOk
            }
        }
    }
    val totalNotices = (state as? UiState.Success)?.data?.size ?: 0

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notices") }) }
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
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search notices") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(MmmutDimens.iconMd)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(MmmutDimens.iconMd)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (availableCategories.size > 1) {
                item(key = "category_filters") {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        FilterChip(
                            selected = category == ALL_CATEGORIES,
                            onClick = { category = ALL_CATEGORIES },
                            label = { Text("All") }
                        )
                        availableCategories.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            when (val s = state) {
                is UiState.Loading -> item(key = "loading") { ListSkeleton(rows = 4) }

                is UiState.Error -> item(key = "error") {
                    Box(Modifier.fillMaxWidth().heightIn(min = 320.dp)) {
                        ErrorView(
                            message = s.message,
                            title = "Couldn't load notices",
                            onRetry = { vm.load() }
                        )
                    }
                }

                is UiState.Empty -> item(key = "empty") {
                    Box(Modifier.fillMaxWidth().heightIn(min = 320.dp)) {
                        EmptyView(
                            title = "No notices yet",
                            message = "Announcements from the university will appear here.",
                            actionLabel = "Refresh",
                            onAction = { vm.load() }
                        )
                    }
                }

                is UiState.Success -> {
                    if (filtered.isEmpty()) {
                        item(key = "no_match") {
                            Box(Modifier.fillMaxWidth().heightIn(min = 320.dp)) {
                                EmptyView(
                                    title = "No matching notices",
                                    message = "Try a different search term or category.",
                                    actionLabel = "Clear filters",
                                    onAction = {
                                        query = ""
                                        category = ALL_CATEGORIES
                                    }
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { n ->
                            NoticeCard(n = n, onClick = { onOpen(n.id) })
                        }
                    }
                }
            }

            if (filtered.isNotEmpty() && totalNotices > 0) {
                item(key = "count_footer") {
                    Text(
                        "${filtered.size} of $totalNotices notices",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
            }
        }
    }
}

private const val ALL_CATEGORIES = "__all__"
