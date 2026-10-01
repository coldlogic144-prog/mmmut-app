package com.mmmut.ero.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmmut.ero.ui.viewmodel.VerifyRollViewModel

@Composable
fun VerifyRollScreen(onDone: () -> Unit, vm: VerifyRollViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var roll by remember { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    val msg by vm.msg.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Verify roll number", style = MaterialTheme.typography.titleLarge)
        Text("Optional. Links your roster identity (name + branch auto-assigned).", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(roll, { roll = it }, label = { Text("10-digit roll number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        msg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button({ vm.claim(roll) { onDone() } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Verify") }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") }
    }
}
