package com.mmmut.ero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.viewmodel.AuthViewModel

/* ==========================================================================
 * Splash
 * --------------------------------------------------------------------------
 * The session check now happens behind a branded screen instead of a bare
 * "Loading…" line, so the first thing a student sees is the university mark.
 * No behaviour change: it just calls the existing session check.
 * ========================================================================== */

@Composable
fun SplashScreen(onSession: (Boolean) -> Unit, vm: AuthViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.checkSession { onSession(it != null) } }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Monogram mark — drawn with theme colours, no image asset needed.
        Box(
            Modifier
                .size(MmmutDimens.avatar)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "M",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        Text(
            "MMMUT Student",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "The Ledger",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Spacing.xxl))

        CircularProgressIndicator(
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Signing you in…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
