package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var tab by remember { mutableStateOf(0) }
    var useRoll by remember { mutableStateOf(false) }

    // Form inputs
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var roll by remember { mutableStateOf("") }

    var selectedBranchId by remember { mutableStateOf("civil") }
    var selectedSemester by remember { mutableStateOf(1) }
    var selectedSection by remember { mutableStateOf("A") }
    var selectedTutGroup by remember { mutableStateOf("T1") }
    var selectedPracGroup by remember { mutableStateOf("P1") }
    var selectedHostel by remember { mutableStateOf("Day Scholar") }
    var roomNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    // Dropdown expanded states
    var branchExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var hostelExpanded by remember { mutableStateOf(false) }

    val busy by vm.busy.collectAsState()
    val err by vm.error.collectAsState()

    val branches = listOf(
        "civil" to "Civil Engineering",
        "cse" to "Computer Science & Engg",
        "it" to "Information Technology",
        "ece" to "Electronics & Comm Engg",
        "eceiot" to "ECE (IoT)",
        "ee" to "Electrical Engineering",
        "me" to "Mechanical Engineering",
        "chemical" to "Chemical Engineering",
        "bba" to "BBA",
        "bpharm" to "B.Pharm"
    )

    val hostels = listOf(
        "Day Scholar",
        "Tagore Hostel",
        "Subhash Hostel",
        "Ramanujan Hostel",
        "Gautam Hostel",
        "Tilak Hostel",
        "Ambedkar Hostel",
        "Sarojini Hostel",
        "Kailash Hostel",
        "Shastri Hostel"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("MMMUT ERP", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Madan Mohan Malaviya University of Technology, Gorakhpur", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Login") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Sign up") })
        }

        Spacer(Modifier.height(16.dp))

        if (tab == 0) {
            Row {
                FilterChip(
                    selected = !useRoll,
                    onClick = { useRoll = false },
                    label = { Text("Username") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = useRoll,
                    onClick = { useRoll = true },
                    label = { Text("Roll Number") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it; vm.clearError() },
                label = { Text(if (useRoll) "Roll number (10 digits)" else "Username or Roll number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it; vm.clearError() },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            err?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.signIn(id, pw, useRoll) { onLoggedIn() } },
                enabled = !busy && id.isNotBlank() && pw.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "Logging in…" else "Login to ERP")
            }
        } else {
            Text("Create Student Profile", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; vm.clearError() },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it; vm.clearError() },
                label = { Text("Username (e.g. rahul.ce26)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it; vm.clearError() },
                label = { Text("Password (min 6 chars)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = roll,
                onValueChange = { roll = it; vm.clearError() },
                label = { Text("Roll Number (10 digits, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            Text("Academic Configuration", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Branch Dropdown
            ExposedDropdownMenuBox(
                expanded = branchExpanded,
                onExpandedChange = { branchExpanded = !branchExpanded }
            ) {
                OutlinedTextField(
                    value = branches.find { it.first == selectedBranchId }?.second ?: selectedBranchId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branch / Department") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = branchExpanded,
                    onDismissRequest = { branchExpanded = false }
                ) {
                    branches.forEach { (bId, bName) ->
                        DropdownMenuItem(
                            text = { Text(bName) },
                            onClick = {
                                selectedBranchId = bId
                                branchExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Semester Dropdown
                ExposedDropdownMenuBox(
                    expanded = semesterExpanded,
                    onExpandedChange = { semesterExpanded = !semesterExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "Semester $selectedSemester",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Semester") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = semesterExpanded,
                        onDismissRequest = { semesterExpanded = false }
                    ) {
                        (1..8).forEach { sem ->
                            DropdownMenuItem(
                                text = { Text("Semester $sem") },
                                onClick = {
                                    selectedSemester = sem
                                    semesterExpanded = false
                                }
                            )
                        }
                    }
                }

                // Section Dropdown
                ExposedDropdownMenuBox(
                    expanded = sectionExpanded,
                    onExpandedChange = { sectionExpanded = !sectionExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "Section $selectedSection",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sectionExpanded,
                        onDismissRequest = { sectionExpanded = false }
                    ) {
                        listOf("A", "B", "C").forEach { sec ->
                            DropdownMenuItem(
                                text = { Text("Section $sec") },
                                onClick = {
                                    selectedSection = sec
                                    sectionExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Timetable Groups", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))

            Text("Tutorial Group", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("T1", "T2", "N/A").forEach { tg ->
                    FilterChip(
                        selected = selectedTutGroup == tg,
                        onClick = {
                            selectedTutGroup = tg
                            if (tg == "T1") selectedPracGroup = "P1"
                            if (tg == "T2") selectedPracGroup = "P2"
                        },
                        label = { Text("Group $tg") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Practical Group", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("P1", "P2", "N/A").forEach { pg ->
                    FilterChip(
                        selected = selectedPracGroup == pg,
                        onClick = {
                            selectedPracGroup = pg
                            if (pg == "P1") selectedTutGroup = "T1"
                            if (pg == "P2") selectedTutGroup = "T2"
                        },
                        label = { Text("Group $pg") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Hostel & Residence", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Hostel Dropdown
            ExposedDropdownMenuBox(
                expanded = hostelExpanded,
                onExpandedChange = { hostelExpanded = !hostelExpanded }
            ) {
                OutlinedTextField(
                    value = selectedHostel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Residence / Hostel") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hostelExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = hostelExpanded,
                    onDismissRequest = { hostelExpanded = false }
                ) {
                    hostels.forEach { h ->
                        DropdownMenuItem(
                            text = { Text(h) },
                            onClick = {
                                selectedHostel = h
                                hostelExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room No. (Optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = gender == "Male",
                        onClick = { gender = "Male" },
                        label = { Text("Male") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = gender == "Female",
                        onClick = { gender = "Female" },
                        label = { Text("Female") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            err?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    vm.signUp(
                        u = id,
                        pw = pw,
                        name = name,
                        branch = selectedBranchId,
                        semester = selectedSemester,
                        sec = selectedSection,
                        tutGroup = selectedTutGroup,
                        pracGroup = selectedPracGroup,
                        hostel = selectedHostel,
                        roomNumber = roomNumber,
                        gender = gender,
                        roll = roll
                    ) { onLoggedIn() }
                },
                enabled = !busy && id.isNotBlank() && pw.isNotBlank() && (roll.isNotBlank() || name.isNotBlank()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "Creating Account…" else "Create Student Account")
            }
        }
    }
}
