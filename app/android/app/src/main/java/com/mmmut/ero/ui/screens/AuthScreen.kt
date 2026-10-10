package com.mmmut.ero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mmmut.ero.data.auth.AuthEmail
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.ui.components.InfoRow
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.friendlyError
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.AuthViewModel

/* ==========================================================================
 * Auth — sign in and multi-step account creation
 * --------------------------------------------------------------------------
 * The authentication rules are NOT implemented here. This screen:
 *   - calls `AuthViewModel.signIn(id, pw, useRoll)` unchanged,
 *   - calls `AuthViewModel.signUp(...)` with exactly the twelve fields
 *     `FirebaseSignupHelper` accepts, unchanged,
 *   - mirrors `AuthEmail.validateUsername` / `validatePassword` and
 *     `AcademicData.ROLL_NUMBER_PATTERN` for the inline hints, so the UI can
 *     never be stricter than the backend.
 *
 * Presentation only: a scrollable, keyboard-aware surface that works down to a
 * 320dp-wide screen, in both light and dark themes.
 * ========================================================================== */

/** Branch ids + labels — the ids are exactly what the signup helper stores in `branchId`. */
private val BRANCH_OPTIONS: List<Pair<String, String>> = listOf(
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

private val HOSTEL_OPTIONS = listOf(
    "Day Scholar", "Tagore Hostel", "Subhash Hostel", "Ramanujan Hostel",
    "Gautam Hostel", "Tilak Hostel", "Ambedkar Hostel", "Sarojini Hostel",
    "Kailash Hostel", "Shastri Hostel"
)

private val SECTION_OPTIONS = listOf("A", "B", "C")
private val TUTORIAL_OPTIONS = listOf("T1", "T2", "N/A")
private val PRACTICAL_OPTIONS = listOf("P1", "P2", "N/A")
private val GENDER_OPTIONS = listOf("Male", "Female")

private const val TOTAL_STEPS = 4
private val STEP_TITLES = listOf(
    "Personal information",
    "Academic information",
    "Timetable groups",
    "Hostel & review"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    /* 0 = sign in, 1 = create account */
    var tab by remember { mutableIntStateOf(0) }
    /* Wizard position, only meaningful while creating an account. */
    var step by remember { mutableIntStateOf(0) }
    var useRoll by remember { mutableStateOf(false) }

    /* ------------------------------------------------------------ inputs -- */
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var branchId by remember { mutableStateOf(BRANCH_OPTIONS.first().first) }
    var semester by remember { mutableIntStateOf(1) }
    var section by remember { mutableStateOf(SECTION_OPTIONS.first()) }
    var tutorialGroup by remember { mutableStateOf(TUTORIAL_OPTIONS.first()) }
    var practicalGroup by remember { mutableStateOf(PRACTICAL_OPTIONS.first()) }
    var hostel by remember { mutableStateOf(HOSTEL_OPTIONS.first()) }
    var roomNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(GENDER_OPTIONS.first()) }
    var signInTouched by remember { mutableStateOf(false) }

    val busy by vm.busy.collectAsState()
    val repoError by vm.error.collectAsState()
    val thrown by vm.lastError.collectAsState()

    /* Repository messages are already human; a thrown exception is not. */
    val banner: String? = if (thrown != null) {
        friendlyError(thrown, repoError ?: "Something went wrong. Please try again.")
    } else {
        repoError
    }

    /* ------------------------------------------------------- validation --- */
    val usernameIssue = if (username.isBlank()) null else AuthEmail.validateUsername(username)
    val passwordIssue = if (password.isBlank()) null else AuthEmail.validatePassword(password)
    val rollIssue = when {
        rollNumber.isBlank() -> null
        !AcademicData.ROLL_NUMBER_PATTERN.matches(rollNumber.trim()) ->
            "Roll number must be exactly 10 digits."
        else -> null
    }

    val personalStepValid = username.isNotBlank() && usernameIssue == null &&
        password.isNotBlank() && passwordIssue == null
    val academicStepValid = rollIssue == null && (rollNumber.isNotBlank() || fullName.isNotBlank())
    val stepValid = when (step) {
        0 -> personalStepValid
        1 -> academicStepValid
        else -> true
    }

    val signInIdIssue: String? = when {
        !signInTouched -> null
        username.isBlank() -> if (useRoll) "Enter your 10-digit roll number." else "Enter your username."
        useRoll && !AcademicData.ROLL_NUMBER_PATTERN.matches(username.trim()) ->
            "Roll number must be exactly 10 digits."
        else -> null
    }
    val signInPasswordIssue =
        if (signInTouched && password.isBlank()) "Enter your password." else null

    val submitSignIn: () -> Unit = {
        signInTouched = true
        if (!busy && username.isNotBlank() && password.isNotBlank()) {
            vm.signIn(username, password, useRoll) { onLoggedIn() }
        }
    }

    val submitSignUp: () -> Unit = {
        if (!busy) {
            /* Field-for-field identical to the original call into the repository. */
            vm.signUp(
                u = username,
                pw = password,
                name = fullName,
                branch = branchId,
                semester = semester,
                sec = section,
                tutGroup = tutorialGroup,
                pracGroup = practicalGroup,
                hostel = hostel,
                roomNumber = roomNumber,
                gender = gender,
                roll = rollNumber
            ) { onLoggedIn() }
        }
    }

    /* ----------------------------------------------------------- layout --- */
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.lg)
    ) {
        Text(
            "MMMUT ERP",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Madan Mohan Malaviya University of Technology, Gorakhpur",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Spacing.lg))

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0; vm.clearError() },
                text = { Text("Sign in", maxLines = 1) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1; vm.clearError() },
                text = { Text("Create account", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        if (tab == 0) {
            /* ============================================================
             * SIGN IN
             * ============================================================ */
            Text("Sign in to your ERP account", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(Spacing.sm))

            Text(
                if (useRoll)
                    "Use the roll number linked to your account."
                else
                    "Use the username you chose when you created your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.md))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !useRoll,
                    onClick = { useRoll = false; vm.clearError() },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                ) {
                    Text("Username", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                SegmentedButton(
                    selected = useRoll,
                    onClick = { useRoll = true; vm.clearError() },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                ) {
                    Text("Roll number", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(Spacing.md))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; vm.clearError() },
                label = { Text(if (useRoll) "10-digit roll number" else "Username") },
                singleLine = true,
                isError = signInIdIssue != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (useRoll) KeyboardType.Number else KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (signInIdIssue != null) FieldError(signInIdIssue)

            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; vm.clearError() },
                label = { Text("Password") },
                singleLine = true,
                isError = signInPasswordIssue != null,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submitSignIn() }),
                modifier = Modifier.fillMaxWidth()
            )

            if (signInPasswordIssue != null) FieldError(signInPasswordIssue)

            if (banner != null) {
                Spacer(Modifier.height(Spacing.md))
                InlineError(banner)
            }

            Spacer(Modifier.height(Spacing.xl))

            Button(
                onClick = submitSignIn,
                enabled = !busy && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MmmutDimens.touchTarget)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(MmmutDimens.iconSm),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(if (busy) "Signing in…" else "Sign in", maxLines = 1)
            }

            Spacer(Modifier.height(Spacing.sm))

            TextButton(
                onClick = { tab = 1; step = 0; vm.clearError() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text("New student? Create an account", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        } else {
            /* ============================================================
             * SIGN UP — four steps
             * ============================================================ */
            Text(
                "Step ${step + 1} of $TOTAL_STEPS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(STEP_TITLES[step], style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.sm))
            StepProgress(step = step, total = TOTAL_STEPS)
            Spacer(Modifier.height(Spacing.lg))

            when (step) {
                /* ------------------------------------------------ step 1 --- */
                0 -> {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; vm.clearError() },
                        label = { Text("Full name") },
                        singleLine = true,
                        supportingText = {
                            Text("Required unless you link a roll number in the next step.")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.md))

                    ChoiceChips(
                        label = "Gender",
                        options = GENDER_OPTIONS,
                        selected = gender,
                        onSelect = { gender = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    StepLabel("Account credentials")

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; vm.clearError() },
                        label = { Text("Username") },
                        singleLine = true,
                        isError = usernameIssue != null,
                        supportingText = {
                            Text(
                                usernameIssue
                                    ?: "At least 3 characters: letters, numbers, dots, hyphens, underscores."
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; vm.clearError() },
                        label = { Text("Password") },
                        singleLine = true,
                        isError = passwordIssue != null,
                        supportingText = { Text(passwordIssue ?: "At least 6 characters.") },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Filled.VisibilityOff
                                    } else {
                                        Icons.Filled.Visibility
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    }
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                /* ------------------------------------------------ step 2 --- */
                1 -> {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it; vm.clearError() },
                        label = { Text("Roll number (optional)") },
                        singleLine = true,
                        isError = rollIssue != null,
                        supportingText = {
                            Text(
                                rollIssue
                                    ?: "10 digits. Leave blank if you don't have it yet — you can link it later from Profile."
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.md))

                    if (rollNumber.isBlank() && fullName.isBlank()) {
                        InlineError("Enter your full name in step 1, or link your roll number here.")
                        Spacer(Modifier.height(Spacing.md))
                    }

                    DropdownField(
                        label = "Branch / Department",
                        value = branchId,
                        options = BRANCH_OPTIONS.map { it.first },
                        optionLabel = { id -> BRANCH_OPTIONS.firstOrNull { it.first == id }?.second ?: id },
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { branchId = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.md))

                    DropdownField(
                        label = "Semester",
                        value = semester,
                        options = (1..8).toList(),
                        optionLabel = { "Semester $it" },
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { semester = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.md))

                    DropdownField(
                        label = "Section",
                        value = section,
                        options = SECTION_OPTIONS,
                        optionLabel = { "Section $it" },
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { section = it; vm.clearError() }
                    )
                }

                /* ------------------------------------------------ step 3 --- */
                2 -> {
                    ChoiceChips(
                        label = "Tutorial group",
                        options = TUTORIAL_OPTIONS,
                        selected = tutorialGroup,
                        onSelect = { tutorialGroup = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    ChoiceChips(
                        label = "Practical group",
                        options = PRACTICAL_OPTIONS,
                        selected = practicalGroup,
                        onSelect = { practicalGroup = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    Text(
                        "These groups decide which tutorial and practical slots appear in your " +
                            "timetable. You can change them later from Profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                /* ------------------------------------------------ step 4 --- */
                else -> {
                    DropdownField(
                        label = "Hostel / Residence",
                        value = hostel,
                        options = HOSTEL_OPTIONS,
                        modifier = Modifier.fillMaxWidth(),
                        onSelect = { hostel = it; vm.clearError() }
                    )

                    Spacer(Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it; vm.clearError() },
                        label = { Text("Room number") },
                        singleLine = true,
                        supportingText = { Text("Optional — leave blank for a day scholar.") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    StepLabel("Review")

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(horizontal = Spacing.card, vertical = Spacing.xs)
                        ) {
                            ReviewRow("Full name", fullName.ifBlank { "Not set" })
                            MmmutDivider()
                            ReviewRow("Username", username)
                            MmmutDivider()
                            ReviewRow(
                                "Roll number",
                                rollNumber.ifBlank { "Not linked (you can add it later)" }
                            )
                            MmmutDivider()
                            ReviewRow(
                                "Branch",
                                BRANCH_OPTIONS.firstOrNull { it.first == branchId }?.second ?: branchId
                            )
                            MmmutDivider()
                            ReviewRow("Semester", "Semester $semester")
                            MmmutDivider()
                            ReviewRow("Section", "Section $section")
                            MmmutDivider()
                            ReviewRow("Tutorial group", tutorialGroup)
                            MmmutDivider()
                            ReviewRow("Practical group", practicalGroup)
                            MmmutDivider()
                            ReviewRow("Hostel", hostel)
                            MmmutDivider()
                            ReviewRow("Room", roomNumber.ifBlank { "Not set" })
                            MmmutDivider()
                            ReviewRow("Gender", gender)
                        }
                    }

                    if (rollNumber.isNotBlank()) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            "Because you linked a roll number, your name, branch and section are " +
                                "taken from the admission roster when you submit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (banner != null) {
                Spacer(Modifier.height(Spacing.lg))
                InlineError(banner)
            }

            Spacer(Modifier.height(Spacing.xl))

            /* Back / Next. On narrow screens the pair stacks, so a long label
             * such as "Create account" can never clip at 320dp. */
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 380.dp

                val backButton: @Composable (Modifier) -> Unit = { m ->
                    OutlinedButton(
                        onClick = {
                            vm.clearError()
                            if (step == 0) tab = 0 else step -= 1
                        },
                        enabled = !busy,
                        modifier = m.heightIn(min = MmmutDimens.touchTarget)
                    ) {
                        Text("Back", maxLines = 1)
                    }
                }

                val nextButton: @Composable (Modifier) -> Unit = { m ->
                    val isLast = step == TOTAL_STEPS - 1
                    Button(
                        onClick = {
                            if (isLast) {
                                submitSignUp()
                            } else if (stepValid && !busy) {
                                vm.clearError()
                                step += 1
                            }
                        },
                        enabled = !busy && (isLast || stepValid),
                        modifier = m.heightIn(min = MmmutDimens.touchTarget)
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(MmmutDimens.iconSm),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(Spacing.sm))
                        }
                        Text(
                            when {
                                isLast && busy -> "Creating…"
                                isLast -> "Create account"
                                else -> "Next"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        backButton(Modifier.fillMaxWidth())
                        nextButton(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        backButton(Modifier.weight(1f))
                        nextButton(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            TextButton(
                onClick = { tab = 0; step = 0; vm.clearError() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text("Already have an account? Sign in", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/* ------------------------------------------------------------- pieces ------ */

/** Four-segment progress bar; the "Step N of 4" text sits above it. */
@Composable
private fun StepProgress(step: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (index <= step) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
            )
        }
    }
}

@Composable
private fun StepLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(Spacing.sm))
}

/** Error surface for repository failures. Role colours only, so it reads in both themes. */
@Composable
private fun InlineError(message: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        )
    }
}

/** Per-field validation message, placed directly under the field it belongs to. */
@Composable
private fun FieldError(message: String, modifier: Modifier = Modifier) {
    Text(
        message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(start = Spacing.lg, top = Spacing.xs)
    )
}

/**
 * Labelled single-choice chip row. Every chip keeps a >=48dp touch target and
 * uses the theme's own chip colours — no hardcoded container/label pair that
 * would only be legible in one scheme.
 */
@Composable
private fun <T> ChoiceChips(
    label: String,
    options: List<T>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit
) {
    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(option.toString(), maxLines = 1) },
                    modifier = Modifier.heightIn(min = MmmutDimens.touchTarget)
                )
            }
        }
    }
}

/** Read-only dropdown backed by the theme's outlined text field. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    value: T,
    options: List<T>,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = optionLabel(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    InfoRow(label, value)
}
