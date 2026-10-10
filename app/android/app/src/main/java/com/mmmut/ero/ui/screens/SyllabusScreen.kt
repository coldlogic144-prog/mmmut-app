package com.mmmut.ero.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.SubjectSyllabus
import com.mmmut.ero.data.model.SyllabusUnit
import com.mmmut.ero.data.model.TopicItem
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.SyllabusData
import com.mmmut.ero.ui.viewmodel.SyllabusViewModel

/* ==========================================================================
 * THE LEDGER — SYLLABUS & CURRICULUM TRACKER
 * --------------------------------------------------------------------------
 * Complete parity with "The Ledger — Syllabus Tracker":
 *   • Multi-branch curriculum exploration (Civil, CSE, IT, ECE, EE, ME, etc.)
 *   • Semester selector strip (Sem 1 to 8, Electives, Skill, Audit)
 *   • Instant search across course codes, titles, and topic content
 *   • Master course toggles & unit-by-unit topic checkboxes
 *   • Tight, elegant mobile layout with clear hierarchy and zero wasted space
 * ========================================================================== */

internal const val KEY_HEADER = "ledger_hdr"
internal const val KEY_STRIP = "ledger_strip"
internal const val KEY_OVERALL = "ledger_overall"
internal const val KEY_FOOTER = "ledger_footer"
internal fun subjectKey(code: String) = "sub|$code"

@Composable
fun SyllabusScreen(
    onBack: (() -> Unit)? = null,
    vm: SyllabusViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var expandedSubjects by remember { mutableStateOf(emptySet<String>()) }
    var expandedModules by remember { mutableStateOf(emptySet<String>()) }
    var branchMenuOpen by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    when (val s = state) {
        is UiState.Loading -> ListSkeleton(rows = 6)
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("No syllabus data available.")
        is UiState.Success -> {
            val data = s.data

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset ${data.branchName} Progress?") },
                    text = { Text("This will uncheck all completed topics for this branch. Your checklist progress cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showResetDialog = false
                                vm.resetBranchProgress()
                            }
                        ) {
                            Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = Spacing.sm,
                    bottom = Spacing.xxxl
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header & Branch Selector & Search Field
                item(key = KEY_HEADER) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "The Ledger",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    "Official MMMUT syllabus tracker · tick topics off as covered",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Branch selection dropdown card
                        Box(Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { branchMenuOpen = true },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "ACTIVE BRANCH",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            data.branchName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select branch",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = branchMenuOpen,
                                onDismissRequest = { branchMenuOpen = false }
                            ) {
                                data.availableBranches.forEach { (bId, bName) ->
                                    DropdownMenuItem(
                                        text = { Text(bName, fontWeight = if (bId == data.branchId) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            branchMenuOpen = false
                                            vm.selectBranch(bId)
                                        },
                                        leadingIcon = if (bId == data.branchId) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null
                                    )
                                }
                            }
                        }

                        // Instant search field
                        OutlinedTextField(
                            value = data.searchQuery,
                            onValueChange = { vm.setSearchQuery(it) },
                            placeholder = { Text("Search subjects or topics...", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (data.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { vm.setSearchQuery("") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )
                    }
                }

                // Semester and Category Horizontal Filter Strip
                if (data.availableViews.isNotEmpty()) {
                    item(key = KEY_STRIP) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            data.availableViews.forEach { (vId, vLabel) ->
                                val selected = vId == data.selectedView
                                FilterChip(
                                    selected = selected,
                                    onClick = { vm.selectView(vId) },
                                    label = { Text(vLabel, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = MaterialTheme.shapes.extraSmall,
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                    }
                }

                // Overall Progress Card
                item(key = KEY_OVERALL) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${data.availableViews.firstOrNull { it.first == data.selectedView }?.second ?: data.selectedView} Progress",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "${data.totalCompleted}/${data.totalTopics} topics",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${formatPercent(data.overallPercent)}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.weight(1f))
                                Surface(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "${(data.totalTopics - data.totalCompleted).coerceAtLeast(0)} left",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (data.overallPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // If empty search or no courses
                if (data.subjectSyllabi.isEmpty()) {
                    item(key = "empty_results") {
                        EmptyView(
                            "No courses found",
                            if (data.searchQuery.isNotBlank()) "No courses or topics match '${data.searchQuery}'."
                            else "No syllabus units published for this section."
                        )
                    }
                }

                // Course list items
                syllabusBodyItems(
                    data = data,
                    expandedSubjects = expandedSubjects,
                    expandedModules = expandedModules,
                    onToggleSubject = { code ->
                        expandedSubjects = if (expandedSubjects.contains(code)) expandedSubjects - code else expandedSubjects + code
                    },
                    onToggleModule = { moduleId ->
                        expandedModules = if (expandedModules.contains(moduleId)) expandedModules - moduleId else expandedModules + moduleId
                    },
                    onToggleTopic = { code, topicKey, completed ->
                        vm.toggleTopic(code, topicKey, completed)
                    },
                    onToggleCourse = { code, completed ->
                        vm.toggleCourse(code, completed)
                    }
                )

                // Footer with reset button
                item(key = KEY_FOOTER) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Curriculum aligned with official MMMUT academic ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reset this branch progress", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the courses with tight, coherent nesting:
 *   SubjectCard
 *     ├── Master Checkbox + Code pill + L-T-P + Name + Mini Progress Bar + Chevron
 *     └── (When open)
 *           ├── Course Meta Section
 *           ├── UnitCard (Module)
 *           │     ├── Unit Header (title, hours, done/total pill, chevron)
 *           │     └── (When open) Topics list (Checkbox + Title, compact 36dp row)
 *           └── Reference Books Section
 */
fun LazyListScope.syllabusBodyItems(
    data: SyllabusData,
    expandedSubjects: Set<String>,
    expandedModules: Set<String>,
    onToggleSubject: (String) -> Unit,
    onToggleModule: (String) -> Unit,
    onToggleTopic: (subjectCode: String, topicKey: String, completed: Boolean) -> Unit,
    onToggleCourse: ((subjectCode: String, completed: Boolean) -> Unit)? = null
) {
    data.subjectSyllabi.forEach { subject ->
        val isSubjectOpen = expandedSubjects.contains(subject.subjectCode)

        item(key = subjectKey(subject.subjectCode)) {
            SubjectCard(
                subject = subject,
                isExpanded = isSubjectOpen,
                expandedModules = expandedModules,
                onToggleSubject = { onToggleSubject(subject.subjectCode) },
                onToggleCourse = { completed ->
                    onToggleCourse?.invoke(subject.subjectCode, completed)
                },
                onToggleModule = onToggleModule,
                onToggleTopic = onToggleTopic
            )
        }
    }
}

/* ------------------------------------------------------------- SubjectCard --- */

@Composable
private fun SubjectCard(
    subject: SubjectSyllabus,
    isExpanded: Boolean,
    expandedModules: Set<String>,
    onToggleSubject: () -> Unit,
    onToggleCourse: (Boolean) -> Unit,
    onToggleModule: (String) -> Unit,
    onToggleTopic: (subjectCode: String, topicKey: String, completed: Boolean) -> Unit
) {
    val pct = subject.percent
    val remaining = (subject.totalTopics - subject.completedTopics).coerceAtLeast(0)
    val isAllDone = subject.totalTopics > 0 && remaining == 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(180))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Subject Clickable Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSubject() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Master Course Checkbox
                Checkbox(
                    checked = isAllDone,
                    onCheckedChange = { onToggleCourse(it) },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(10.dp))

                // Course Info Column
                Column(modifier = Modifier.weight(1f)) {
                    // Badges row: Code pill + LTP + Credits
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = subject.subjectCode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                        if (subject.ltp.isNotBlank()) {
                            Text(
                                text = "· ${subject.ltp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "· ${subject.credits} Cr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    // Subject Name
                    Text(
                        text = subject.subjectName.ifBlank { subject.subjectCode },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isAllDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isAllDone) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // Progress Bar & Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = if (isAllDone) MaterialTheme.semantic.success else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        Text(
                            text = "${formatPercent(pct)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isAllDone) MaterialTheme.semantic.success else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(${subject.completedTopics}/${subject.totalTopics})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // Expand / Collapse Chevron
                IconButton(
                    onClick = onToggleSubject,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded Body: Course Meta, Units, References
            if (isExpanded) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    thickness = 0.8.dp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Course Meta Card (Category / Objectives)
                    if (subject.objectives.isNotBlank() || subject.prerequisite.isNotBlank() || subject.category.isNotBlank()) {
                        CourseMetaSection(subject)
                    }

                    // Modules / Units
                    subject.units.forEachIndexed { unitIndex, unit ->
                        val moduleId = "${subject.subjectCode}|$unitIndex"
                        val isModuleOpen = expandedModules.contains(moduleId)

                        UnitCard(
                            unit = unit,
                            isExpanded = isModuleOpen,
                            onToggle = { onToggleModule(moduleId) },
                            onToggleTopic = { topicKey, completed ->
                                onToggleTopic(subject.subjectCode, topicKey, completed)
                            }
                        )
                    }

                    // Reference Books
                    if (subject.referenceBooks.isNotEmpty()) {
                        BooksSection(subject.referenceBooks)
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- UnitCard --- */

@Composable
private fun UnitCard(
    unit: SyllabusUnit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onToggleTopic: (topicKey: String, completed: Boolean) -> Unit
) {
    val total = unit.totalCount
    val done = unit.completedCount
    val isUnitDone = total > 0 && done == total

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(150))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Unit Header Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = unit.unitNumber.ifBlank { "MODULE" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (unit.hours > 0) {
                            Text(
                                text = "· ${unit.hours} Hours",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Surface(
                            color = if (isUnitDone) MaterialTheme.semantic.success.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "$done/$total",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isUnitDone) MaterialTheme.semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = unit.unitTitle.ifBlank { unit.unitNumber.ifBlank { "Module Content" } },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse module" else "Expand module",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Compact Topics Checklist (Directly inside the Unit Card)
            if (isExpanded && unit.topics.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.5.dp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    unit.topics.forEachIndexed { index, topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTopic(topic.key, !topic.completed) }
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = topic.completed,
                                onCheckedChange = { onToggleTopic(topic.key, it) },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = topic.name.ifBlank { "Topic ${index + 1}" },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 17.sp
                                ),
                                color = if (topic.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (topic.completed) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (index < unit.topics.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 34.dp, end = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------- CourseMeta --- */

@Composable
private fun CourseMetaSection(subject: SubjectSyllabus) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (subject.category.isNotBlank() || subject.prerequisite.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subject.category.isNotBlank()) {
                        Text(
                            text = "Category: ${subject.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (subject.prerequisite.isNotBlank()) {
                        Text(
                            text = "· Prereq: ${subject.prerequisite}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (subject.objectives.isNotBlank()) {
                Text(
                    text = "Objectives: ${subject.objectives}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ----------------------------------------------------------- BooksSection --- */

@Composable
private fun BooksSection(books: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "RECOMMENDED TEXTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            books.forEach { book ->
                Text(
                    text = "• $book",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatPercent(value: Double): String =
    String.format(java.util.Locale.US, "%.1f", value)
