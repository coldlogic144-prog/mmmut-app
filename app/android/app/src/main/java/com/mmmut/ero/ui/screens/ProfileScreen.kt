package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.ui.components.*
import com.mmmut.ero.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onVerifyRoll: () -> Unit,
    onTelegram: () -> Unit,
    vm: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(s.message) { vm.load() }
        is UiState.Empty -> EmptyView("No profile found.")
        is UiState.Success -> {
            val p = s.data

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.name.ifBlank { p.username }, style = MaterialTheme.typography.titleLarge)
                        Text("@${p.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Roll Number: ${p.rollNumber.ifBlank { "Not verified" }}")
                        Text("Branch: ${p.branchId.uppercase()} · Semester ${p.semester} · Section ${p.section}")
                        Text("Hostel: ${p.hostel} ${if (p.roomNumber.isNotBlank()) "(Room ${p.roomNumber})" else ""}")
                        Text("Gender: ${p.gender}")
                        Text("Account Status: ${p.migrationStatus}")
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!p.rollNumberVerified) {
                    OutlinedButton(onClick = onVerifyRoll, modifier = Modifier.fillMaxWidth()) {
                        Text("Link Official Roll Number")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Timetable Groups & Academic Settings", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))

                        Text("Tutorial Group: ${p.tutorialGroup}", style = MaterialTheme.typography.bodyMedium)
                        Text("Practical Group: ${p.practicalGroup}", style = MaterialTheme.typography.bodyMedium)
                        Text("Section: Section ${p.section} · Semester ${p.semester}", style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(12.dp))

                        Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Academic & Hostel Profile")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Services & Security", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedButton(onClick = onTelegram, modifier = Modifier.fillMaxWidth()) {
                    Text("Telegram Private Channel Access")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { vm.logout(onLoggedOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout from Account")
                }

                // Edit Profile Dialog
                if (showEditDialog) {
                    var editSemester by remember { mutableStateOf(p.semester) }
                    var editSection by remember { mutableStateOf(p.section) }
                    var editTutGroup by remember { mutableStateOf(p.tutorialGroup) }
                    var editPracGroup by remember { mutableStateOf(p.practicalGroup) }
                    var editHostel by remember { mutableStateOf(p.hostel) }
                    var editRoom by remember { mutableStateOf(p.roomNumber) }

                    var semExpanded by remember { mutableStateOf(false) }
                    var secExpanded by remember { mutableStateOf(false) }
                    var hostelExpanded by remember { mutableStateOf(false) }

                    val hostelsList = listOf(
                        "Day Scholar", "Tagore Hostel", "Subhash Hostel", "Ramanujan Hostel",
                        "Gautam Hostel", "Tilak Hostel", "Ambedkar Hostel", "Sarojini Hostel",
                        "Kailash Hostel", "Shastri Hostel"
                    )

                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        title = { Text("Edit Profile Configuration") },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Updating your academic settings immediately reloads your timetable and subject syllabus.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                                // Semester Dropdown
                                ExposedDropdownMenuBox(expanded = semExpanded, onExpandedChange = { semExpanded = !semExpanded }) {
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

                                // Section Dropdown
                                ExposedDropdownMenuBox(expanded = secExpanded, onExpandedChange = { secExpanded = !secExpanded }) {
                                    OutlinedTextField(
                                        value = "Section $editSection",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Section") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(expanded = secExpanded, onDismissRequest = { secExpanded = false }) {
                                        listOf("A", "B", "C").forEach { sec ->
                                            DropdownMenuItem(
                                                text = { Text("Section $sec") },
                                                onClick = { editSection = sec; secExpanded = false }
                                            )
                                        }
                                    }
                                }

                                Text("Tutorial Group", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("T1", "T2", "N/A").forEach { tg ->
                                        FilterChip(
                                            selected = editTutGroup == tg,
                                            onClick = {
                                                editTutGroup = tg
                                                if (tg == "T1") editPracGroup = "P1"
                                                if (tg == "T2") editPracGroup = "P2"
                                            },
                                            label = { Text("Group $tg") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                Text("Practical Group", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("P1", "P2", "N/A").forEach { pg ->
                                        FilterChip(
                                            selected = editPracGroup == pg,
                                            onClick = {
                                                editPracGroup = pg
                                                if (pg == "P1") editTutGroup = "T1"
                                                if (pg == "P2") editTutGroup = "T2"
                                            },
                                            label = { Text("Group $pg") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                // Hostel Dropdown
                                ExposedDropdownMenuBox(expanded = hostelExpanded, onExpandedChange = { hostelExpanded = !hostelExpanded }) {
                                    OutlinedTextField(
                                        value = editHostel,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Hostel / Residence") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hostelExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(expanded = hostelExpanded, onDismissRequest = { hostelExpanded = false }) {
                                        hostelsList.forEach { h ->
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
                                    label = { Text("Room Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                vm.updateAcademicProfile(
                                    semester = editSemester,
                                    section = editSection,
                                    tutGroup = editTutGroup,
                                    pracGroup = editPracGroup,
                                    hostel = editHostel,
                                    roomNumber = editRoom
                                ) {
                                    showEditDialog = false
                                }
                            }) {
                                Text("Save & Apply Changes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}
