package com.mmmut.ero.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.MessMenuData
import com.mmmut.ero.data.model.MessDay
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.LoadingView
import com.mmmut.ero.ui.viewmodel.MessMenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessMenuScreen(
    onBack: () -> Unit,
    vm: MessMenuViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: By Day, 1: Full Week

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hostel Mess Menu", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "MMMUT Official Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            when (val s = state) {
                is UiState.Loading -> LoadingView("Loading mess menu…")
                is UiState.Error -> ErrorView(s.message) { vm.load() }
                is UiState.Empty -> EmptyView("No mess menu available.")
                is UiState.Success -> {
                    val data = s.data
                    val today = data.todayName
                    val selectedDay = data.selectedDay

                    // Top Tab: Day View vs Full Week
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Day by Day") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Weekly Overview") }
                        )
                    }

                    if (selectedTab == 0) {
                        // Horizontal Day Selector Chips
                        Column(Modifier.fillMaxSize()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(MessMenuData.DAYS) { dayName ->
                                        val isSelected = selectedDay.day.equals(dayName, ignoreCase = true)
                                        val isToday = today.equals(dayName, ignoreCase = true)

                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { vm.selectDay(dayName) },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(dayName)
                                                    if (isToday) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            "• Today",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                            else MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            // Day's Meals Content
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    DayHeaderBanner(
                                        dayName = selectedDay.day,
                                        isToday = selectedDay.day.equals(today, ignoreCase = true)
                                    )
                                }

                                item {
                                    MealCard(
                                        mealName = "Breakfast",
                                        timing = MessMenuData.BREAKFAST_TIMING,
                                        emoji = "🌅",
                                        items = selectedDay.breakfast,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                    )
                                }

                                item {
                                    MealCard(
                                        mealName = "Lunch",
                                        timing = MessMenuData.LUNCH_TIMING,
                                        emoji = "🍱",
                                        items = selectedDay.lunch,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    )
                                }

                                item {
                                    MealCard(
                                        mealName = "Tea & Snacks",
                                        timing = MessMenuData.SNACKS_TIMING,
                                        emoji = "☕",
                                        items = selectedDay.snacks,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                    )
                                }

                                item {
                                    MealCard(
                                        mealName = "Dinner",
                                        timing = MessMenuData.DINNER_TIMING,
                                        emoji = "🍛",
                                        items = selectedDay.dinner,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }

                                item {
                                    MessNoticeFooter()
                                }
                            }
                        }
                    } else {
                        // Full 7-Day Overview
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(
                                            "MMMUT WEEKLY MESS SCHEDULE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "All 7 Days Meal Breakdown",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "Tap on any day to inspect it in detail or navigate through the tabs.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            items(data.weeklyMenu.days, key = { it.day }) { dayMenu ->
                                val isToday = dayMenu.day.equals(today, ignoreCase = true)
                                WeeklyDayCard(
                                    dayMenu = dayMenu,
                                    isToday = isToday,
                                    onSelectDay = {
                                        vm.selectDay(dayMenu.day)
                                        selectedTab = 0
                                    }
                                )
                            }

                            item {
                                MessNoticeFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayHeaderBanner(dayName: String, isToday: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isToday) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("TODAY", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    text = if (isToday) "Showing current day meal plan" else "Scheduled mess menu for $dayName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MealCard(
    mealName: String,
    timing: String,
    emoji: String,
    items: List<String>,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mealName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = timing,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            Spacer(Modifier.height(8.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyDayCard(
    dayMenu: MessDay,
    isToday: Boolean,
    onSelectDay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectDay() },
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dayMenu.day,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isToday) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = onSelectDay,
                            label = { Text("TODAY", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                TextButton(onClick = onSelectDay) {
                    Text("View Day →")
                }
            }

            Spacer(Modifier.height(6.dp))
            MealSummaryRow(label = "Breakfast", items = dayMenu.breakfast)
            MealSummaryRow(label = "Lunch", items = dayMenu.lunch)
            MealSummaryRow(label = "Snacks", items = dayMenu.snacks)
            MealSummaryRow(label = "Dinner", items = dayMenu.dinner)
        }
    }
}

@Composable
fun MealSummaryRow(label: String, items: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(76.dp)
        )
        Text(
            text = items.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MessNoticeFooter() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Note: Seasonal fruits & vegetables are subject to market availability. Special festive feasts are scheduled as notified by the Chief Warden / Mess Committee.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
