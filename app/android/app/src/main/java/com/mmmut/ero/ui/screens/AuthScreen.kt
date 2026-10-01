package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mmmut.ero.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(onLoggedIn: () -> Unit, vm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var tab by remember { mutableStateOf(0) }
    var useRoll by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var roll by remember { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    val err by vm.error.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("MMMUT", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("Student ERP — Gorakhpur", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Login") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Sign up") })
        }
        Spacer(Modifier.height(16.dp))
        if (tab == 0) {
            Row { FilterChip(selected = !useRoll, onClick = { useRoll = false }, label = { Text("Username") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = useRoll, onClick = { useRoll = true }, label = { Text("Roll number") }) }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = id,
                onValueChange = { id = it; vm.clearError() },
                label = { Text(if (useRoll) "Roll number (10 digits)" else "Username or Roll number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.signIn(id, pw, useRoll) { onLoggedIn() } },
                enabled = !busy && id.isNotBlank() && pw.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "Logging in…" else "Login")
            }
        } else {
            OutlinedTextField(name, { name = it }, label = { Text("Full name (or roll auto-fill)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(id, { id = it }, label = { Text("Choose username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(pw, { pw = it }, label = { Text("Password (min 6)") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(roll, { roll = it }, label = { Text("Roll number (optional, verifies instantly)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))
            Button({ vm.signUp(id, pw, name, "cse", "A", "Day Scholar", "Not specified", roll) { onLoggedIn() } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Please wait…" else "Create account") }
        }
    }
}
