package com.mmmut.ero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.ProfileViewModel

/* ==========================================================================
 * Profile
 * --------------------------------------------------------------------------
 * A read-only summary of the Firestore `users/{uid}` document, grouped so a
 * student can scan it: Personal -> Academic -> Timetable -> Hostel -> Account.
 *
 * The only writes this screen performs are the ones the existing
 * ProfileViewModel already exposed (`updateAcademicProfile`, `logout`). The
 * roll-number link flow still routes through `onVerifyRoll` and the Telegram
 * flow through `onTelegram`, exactly as before.
 *
 * Every colour comes from a Material role or the semantic layer, so both the
 * light and the dark scheme stay legible.
 * ========================================================================== */

/**
 * Display names for the branch ids the signup flow can store.
 * Mirrors the branch catalogue in AuthScreen / the roster mapping.
 */
private val BRANCH_LABELS = mapOf(
    "civil" to "Civil Engineering",
    "cse" to "Computer Science & Engineering",
    "it" to "Information Technology",
    "ece" to "Electronics & Communication Engineering",
    "eceiot" to "ECE (IoT)",
    "ee" to "Electrical Engineering",
    "me" to "Mechanical Engineering",
    "chemical" to "Chemical Engineering",
    "bba" to "BBA",
    "bpharm" to "B.Pharm"
)

private val HOSTELS = listOf(
    "Day Scholar", "Tagore Hostel", "Subhash Hostel", "Ramanujan Hostel",
    "Gautam Hostel", "Tilak Hostel", "Ambedkar Hostel", "Sarojini Hostel",
    "Kailash Hostel", "Shastri Hostel"
)

private fun branchLabel(id: String): String =
    BRANCH_LABELS[id.trim().lowercase()] ?: id.trim().uppercase().ifBlank { "Not set" }

/** Up to two initials, used for the monogram avatar. Never an emoji. */
private fun monogramOf(name: String, fallback: String): String {
    val source = name.ifBlank { fallback }
    val initials = source.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
    return initials.ifBlank { "?" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onVerifyRoll: () -> Unit,
    onTelegram: () -> Unit,
    onAdmin: (() -> Unit)? = null,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var loggingOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        UiState.Loading -> LoadingView("Loading your profile…")

        is UiState.Error -> ErrorView(s.message, onRetry = { vm.load() })

        UiState.Empty -> EmptyView(
            title = "No profile found",
            message = "We couldn't find your student profile yet. Try loading it again.",
            actionLabel = "Reload profile",
            onAction = { vm.load() }
        )

        is UiState.Success -> {
            val p = s.data

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = Spacing.xxl)
            ) {
                ProfileHeader(p, onRefresh = { vm.load() })

                /* -------------------------------------------------- personal --- */
                ProfileSection("Personal") {
                    InfoRowList(
                        listOf(
                            "Full name" to p.name.ifBlank { "Not set" },
                            "Roll number" to p.rollNumber.ifBlank { "Not linked" },
                            "Username" to p.username.ifBlank { "Not set" },
                            "Gender" to p.gender.ifBlank { "Not specified" }
                        ),
                        emphasizeFirst = true
                    )
                }

                /* -------------------------------------------------- academic --- */
                ProfileSection("Academic") {
                    InfoRowList(
                        listOf(
                            "Branch" to branchLabel(p.branchId),
                            "Semester" to "Semester ${p.semester}",
                            "Section" to "Section ${p.section}"
                        )
                    )
                }

                /* ------------------------------------------------- timetable --- */
                ProfileSection("Timetable") {
                    InfoRowList(
                        listOf(
                            "Tutorial group" to p.tutorialGroup.ifBlank { "N/A" },
                            "Practical group" to p.practicalGroup.ifBlank { "N/A" }
                        )
                    )
                }

                /* ---------------------------------------------------- hostel --- */
                ProfileSection("Hostel") {
                    InfoRowList(
                        listOf(
                            "Residence" to p.hostel.ifBlank { "Day Scholar" },
                            "Room" to p.roomNumber.ifBlank { "Not set" }
                        )
                    )
                }

                /* --------------------------------------------------- account --- */
                ProfileSection("Account") {
                    InfoRowList(
                        listOf(
                            "Account status" to accountStatusLabel(p.migrationStatus),
                            "Role" to when {
                                p.isAdmin -> "Administrator"
                                p.adminRequested -> "Student · admin access requested"
                                else -> "Student"
                            }
                        )
                    )
                }

                /* --------------------------------------------------- actions --- */
                SectionHeader("Actions")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screen)
                ) {
                    Column(
                        Modifier.padding(Spacing.card),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        /* Roll-number verification — reflects the stored flag, it
                         * never claims "verified" for an unverified account. */
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Roll number verification",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    if (p.rollNumberVerified)
                                        "Your roll number is linked to the admission roster."
                                    else
                                        "Link your official roll number to unlock roster data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            StatusChip(
                                text = if (p.rollNumberVerified) "Verified" else "Not verified",
                                tone = if (p.rollNumberVerified) ChipTone.Success else ChipTone.Warning
                            )
                        }

                        if (!p.rollNumberVerified) {
                            OutlinedButton(
                                onClick = onVerifyRoll,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MmmutDimens.touchTarget)
                            ) {
                                Text("Link official roll number", maxLines = 1)
                            }
                        }

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Text("Edit academic & hostel details", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onTelegram,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Text("Telegram private channel", maxLines = 1)
                        }

                        /* Rendered only for a real admin, and only when the host
                         * navigation supplied a destination. */
                        if (p.isAdmin && onAdmin != null) {
                            OutlinedButton(
                                onClick = onAdmin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MmmutDimens.touchTarget)
                            ) {
                                Text("Admin console", maxLines = 1)
                            }
                        }

                        Button(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Text("Log out", maxLines = 1)
                        }
                    }
                }
            }

            /* ------------------------------------------------- edit dialog --- */
            if (showEditDialog) {
                EditProfileDialog(
                    profile = p,
                    onDismiss = { showEditDialog = false },
                    onSave = { semester, section, tutGroup, pracGroup, hostel, roomNumber ->
                        vm.updateAcademicProfile(
                            semester = semester,
                            section = section,
                            tutGroup = tutGroup,
                            pracGroup = pracGroup,
                            hostel = hostel,
                            roomNumber = roomNumber
                        ) {
                            showEditDialog = false
                        }
                    }
                )
            }

            /* ----------------------------------------------- logout dialog --- */
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { if (!loggingOut) showLogoutDialog = false },
                    title = { Text("Log out?") },
                    text = {
                        Text(
                            "You will need your username and password to sign in again. " +
                                "Notifications for this device will stop."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                loggingOut = true
                                vm.logout {
                                    loggingOut = false
                                    showLogoutDialog = false
                                    onLoggedOut()
                                }
                            },
                            enabled = !loggingOut,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Text(if (loggingOut) "Logging out…" else "Log out")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false },
                            enabled = !loggingOut,
                            modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

/* ------------------------------------------------------------- header ------ */

@Composable
private fun ProfileHeader(p: StudentProfile, onRefresh: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.md)
    ) {
        Column(Modifier.padding(Spacing.card)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(MmmutDimens.avatar)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        monogramOf(p.name, p.username),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.width(Spacing.md))

                Column(Modifier.weight(1f)) {
                    Text(
                        p.name.ifBlank { p.username.ifBlank { "Student" } },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (p.username.isBlank()) "—" else "@${p.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        if (p.rollNumber.isBlank()) "Roll number not linked" else "Roll ${p.rollNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(
                    text = if (p.rollNumberVerified) "Roll verified" else "Roll not verified",
                    tone = if (p.rollNumberVerified) ChipTone.Success else ChipTone.Warning
                )
                Spacer(Modifier.width(Spacing.sm))
                StatusChip(
                    text = accountStatusLabel(p.migrationStatus),
                    tone = if (p.migrationStatus.equals("verified", ignoreCase = true))
                        ChipTone.Success else ChipTone.Warning
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                ) {
                    Text("Refresh")
                }
            }
        }
    }
}

private fun accountStatusLabel(raw: String): String = when (raw.trim().lowercase()) {
    "verified" -> "Verified"
    "pending" -> "Pending review"
    "rejected" -> "Rejected"
    "" -> "Unknown"
    else -> raw.replaceFirstChar { it.uppercase() }
}

/* ------------------------------------------------------------ sections ----- */

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    SectionHeader(title)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
    ) {
        Column(
            Modifier.padding(horizontal = Spacing.card, vertical = Spacing.xs),
            content = content
        )
    }
}

@Composable
private fun InfoRowList(rows: List<Pair<String, String>>, emphasizeFirst: Boolean = false) {
    rows.forEachIndexed { index, (label, value) ->
        if (index > 0) MmmutDivider()
        InfoRow(label, value, emphasize = emphasizeFirst && index == 0)
    }
}

/* --------------------------------------------------------- edit dialog ----- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (
        semester: Int,
        section: String,
        tutGroup: String,
        pracGroup: String,
        hostel: String,
        roomNumber: String
    ) -> Unit
) {
    var editSemester by remember { mutableIntStateOf(profile.semester) }
    var editSection by remember { mutableStateOf(profile.section) }
    var editTutGroup by remember { mutableStateOf(profile.tutorialGroup) }
    var editPracGroup by remember { mutableStateOf(profile.practicalGroup) }
    var editHostel by remember { mutableStateOf(profile.hostel) }
    var editRoom by remember { mutableStateOf(profile.roomNumber) }

    var semExpanded by remember { mutableStateOf(false) }
    var secExpanded by remember { mutableStateOf(false) }
    var hostelExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit academic & hostel details") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    "Updating your academic settings immediately reloads your timetable and subject syllabus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = semExpanded,
                    onExpandedChange = { semExpanded = !semExpanded }
                ) {
                    OutlinedTextField(
                        value = "Semester $editSemester",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Semester") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = semExpanded, onDismissRequest = { semExpanded = false }) {
                        (1..8).forEach { sem ->
                            DropdownMenuItem(
                                text = { Text("Semester $sem") },
                                onClick = { editSemester = sem; semExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = secExpanded,
                    onExpandedChange = { secExpanded = !secExpanded }
                ) {
                    OutlinedTextField(
                        value = "Section $editSection",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = secExpanded, onDismissRequest = { secExpanded = false }) {
                        listOf("A", "B", "C", "D").forEach { sec ->
                            DropdownMenuItem(
                                text = { Text("Section $sec") },
                                onClick = { editSection = sec; secExpanded = false }
                            )
                        }
                    }
                }

                Text("Tutorial group", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    listOf("T1", "T2", "N/A").forEach { tg ->
                        FilterChip(
                            selected = editTutGroup == tg,
                            onClick = { editTutGroup = tg },
                            label = { Text("Group $tg", maxLines = 1) },
                            modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                        )
                    }
                }

                Text("Practical group", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    listOf("P1", "P2", "P3", "P4", "N/A").forEach { pg ->
                        FilterChip(
                            selected = editPracGroup == pg,
                            onClick = { editPracGroup = pg },
                            label = { Text("Group $pg", maxLines = 1) },
                            modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = hostelExpanded,
                    onExpandedChange = { hostelExpanded = !hostelExpanded }
                ) {
                    OutlinedTextField(
                        value = editHostel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hostel / Residence") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hostelExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = hostelExpanded, onDismissRequest = { hostelExpanded = false }) {
                        HOSTELS.forEach { h ->
                            DropdownMenuItem(
                                text = { Text(h) },
                                onClick = { editHostel = h; hostelExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = editRoom,
                    onValueChange = { editRoom = it },
                    label = { Text("Room number") },
                    supportingText = { Text("Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(editSemester, editSection, editTutGroup, editPracGroup, editHostel, editRoom)
                },
                modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text("Save changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text("Cancel")
            }
        }
    )
}
