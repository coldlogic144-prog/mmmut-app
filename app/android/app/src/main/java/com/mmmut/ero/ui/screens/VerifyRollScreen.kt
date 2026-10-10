package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.VerifyRollViewModel

/* ==========================================================================
 * Roll verification
 * --------------------------------------------------------------------------
 * Optional step: links the account to a roster identity so the name and branch
 * can be auto-assigned.
 *
 * Rebuilt from a bare 25-line Column. The original had no top bar (so no way
 * back), no scrolling (the on-screen keyboard covered the submit button on a
 * short phone), no format validation, and no busy indicator — the button simply
 * greyed out with no feedback. Logic and the ViewModel API are unchanged.
 *
 * The 10-digit rule mirrors the pattern used by the web ERP and asserted by
 * AppUnitTests.rollPattern_is10Digits.
 * ========================================================================== */

private val ROLL_PATTERN = Regex("^\\d{10}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyRollScreen(
    onDone: () -> Unit,
    vm: VerifyRollViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var roll by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()

    val rollError = when {
        !touched || roll.isEmpty() -> null
        !ROLL_PATTERN.matches(roll) -> "Enter all 10 digits, exactly as printed on your ID card."
        else -> null
    }
    val canSubmit = ROLL_PATTERN.matches(roll) && !busy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify roll number") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MmmutDimens.iconLg)
                )
                Text(
                    "Link your roster identity",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                "This step is optional. Verifying your roll number links your account to the " +
                    "university roster so your name and branch can be filled in automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = roll,
                onValueChange = { raw ->
                    // Keep the field numeric and capped at 10 so the student
                    // cannot enter something the roster can never match.
                    roll = raw.filter { it.isDigit() }.take(10)
                    touched = true
                },
                label = { Text("Roll number") },
                placeholder = { Text("10 digits") },
                singleLine = true,
                isError = rollError != null,
                supportingText = rollError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (msg != null) {
                Surface(
                    color = MaterialTheme.semantic.dangerContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        msg ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.semantic.onDangerContainer,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }

            Button(
                onClick = { touched = true; if (canSubmit) vm.claim(roll) { onDone() } },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MmmutDimens.touchTarget)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(MmmutDimens.iconMd),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(if (busy) "Verifying…" else "Verify roll number")
            }

            TextButton(
                onClick = onDone,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MmmutDimens.touchTarget)
            ) {
                Text("Skip for now")
            }
        }
    }
}
