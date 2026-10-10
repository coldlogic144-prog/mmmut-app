package com.mmmut.ero.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.ui.components.ChipTone
import com.mmmut.ero.ui.components.EmptyView
import com.mmmut.ero.ui.components.ErrorView
import com.mmmut.ero.ui.components.ListSkeleton
import com.mmmut.ero.ui.components.MmmutDivider
import com.mmmut.ero.ui.components.StatusChip
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.ui.viewmodel.NoticeDetailViewModel
import com.mmmut.ero.util.TimeUtils

/* ==========================================================================
 * Notice detail
 * --------------------------------------------------------------------------
 * One LazyColumn for the scroll body, so a long circular scrolls cleanly with
 * no nested same-axis scrolling. The optional attachment is treated as a
 * free-form link: an image URL gets a clearly labelled preview placeholder plus
 * an "open" action, and a null/absent link renders nothing at all. No code path
 * can throw on a missing attachment.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailScreen(
    id: String,
    vm: NoticeDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    // Resolved from the composition, never from Activity.context — inside a
    // NavHost that context is the NavBackStackEntry, so a cast would crash.
    val backOwner = LocalOnBackPressedDispatcherOwner.current
    val canGoBack = backOwner != null

    LaunchedEffect(id) { vm.load(id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notice", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { backOwner?.onBackPressedDispatcher?.onBackPressed() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (val s = state) {
                is UiState.Loading -> ListSkeleton(rows = 4)

                is UiState.Error -> ErrorView(
                    message = s.message,
                    title = "Couldn't load this notice",
                    onRetry = { vm.load(id) }
                )

                is UiState.Empty -> EmptyView(
                    title = "Notice not found",
                    message = "It may have been removed by the administrator.",
                    actionLabel = "Retry",
                    onAction = { vm.load(id) }
                )

                is UiState.Success -> NoticeDetailBody(s.data, ctx)
            }
        }
    }
}

@Composable
private fun NoticeDetailBody(n: Notice, ctx: android.content.Context) {
    val s = MaterialTheme.semantic

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.md,
            bottom = Spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item(key = "chips") {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusChip(n.category.ifBlank { "general" }, ChipTone.Info)
                if (n.important) StatusChip("Urgent", ChipTone.Danger)
                if (n.pinned) StatusChip("Pinned", ChipTone.Neutral)
            }
        }

        item(key = "title") {
            Text(n.title.ifBlank { "Untitled notice" }, style = MaterialTheme.typography.titleLarge)
        }

        item(key = "meta") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    TimeUtils.formatMillis(n.createdAtMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (n.important) {
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = s.danger,
                        modifier = Modifier.size(MmmutDimens.iconSm)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Important", style = MaterialTheme.typography.labelSmall, color = s.danger)
                }
                if (n.pinned) {
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MmmutDimens.iconSm)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "divider") { MmmutDivider() }

        if (n.linkUrl.isNotBlank()) {
            item(key = "attachment") { AttachmentBlock(n.linkUrl, ctx) }
        }

        item(key = "content") {
            SelectionContainer {
                Text(
                    n.content.ifBlank { "This notice has no body text." },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/* ---------------------------------------------------------- attachment --- */

@Composable
private fun AttachmentBlock(link: String, ctx: android.content.Context) {
    var openFailed by remember(link) { mutableStateOf(false) }
    val isImage = remember(link) { looksLikeImage(link) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (isImage) {
            // This build has no image loader on the classpath, so the preview is an
            // explicit placeholder rather than a broken image box. Opening the URL
            // hands the file to the system viewer.
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "Image attachment",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Preview is not available in the app — open it below.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { openFailed = !openSafely(ctx, link) },
            modifier = Modifier.fillMaxWidth().heightIn(min = MmmutDimens.touchTarget)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(MmmutDimens.iconMd)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(if (isImage) "Open image" else "Open attachment / link")
        }

        if (openFailed) {
            Text(
                "No app on this device could open that link.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.semantic.danger
            )
        }
    }
}

private fun looksLikeImage(url: String): Boolean {
    val u = url.substringBefore('?').substringBefore('#').lowercase()
    return u.endsWith(".png") || u.endsWith(".jpg") || u.endsWith(".jpeg") ||
        u.endsWith(".webp") || u.endsWith(".gif")
}

private fun openSafely(ctx: android.content.Context, url: String): Boolean = try {
    ctx.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: Exception) {
    false
}
