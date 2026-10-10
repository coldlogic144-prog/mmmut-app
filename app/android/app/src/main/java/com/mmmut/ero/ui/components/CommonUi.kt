package com.mmmut.ero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mmmut.ero.data.model.Notice
import com.mmmut.ero.ui.theme.MmmutDimens
import com.mmmut.ero.ui.theme.Spacing
import com.mmmut.ero.ui.theme.semantic
import com.mmmut.ero.util.TimeUtils
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/* ==========================================================================
 * Shared UI primitives
 * --------------------------------------------------------------------------
 * Everything here is used by more than one screen. The goal is that a student
 * can never be shown a blank screen, a silent failure, or a raw exception.
 *
 * `friendlyError` is the single place that turns a thrown exception into
 * something a human should read. No screen should surface `e.message` directly:
 * those strings contain exception class names, Firestore paths and HTTP codes.
 * ========================================================================== */

/**
 * Maps a throwable to a short, actionable sentence.
 * Technical detail stays in logs; the user gets a next step.
 */
fun friendlyError(error: Throwable?, fallback: String = "Something went wrong."): String {
    val cause = error?.cause ?: error
    return when (cause) {
        null -> fallback
        is UnknownHostException -> "No internet connection. Check your network and try again."
        is SocketTimeoutException -> "The server took too long to respond. Try again."
        is TimeoutException -> "The request timed out. Try again."
        is FirebaseNetworkException -> "Can't reach the server right now. Check your connection."
        is FirebaseAuthException -> when (cause.errorCode) {
            "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                "Incorrect username or password."
            "ERROR_USER_NOT_FOUND" -> "No account found for that username or roll number."
            "ERROR_USER_DISABLED" -> "This account has been disabled. Contact the administrator."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Wait a moment and try again."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Can't reach the server. Check your connection."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists for that roll number."
            "ERROR_WEAK_PASSWORD" -> "That password is too weak. Use at least 6 characters."
            else -> "Sign-in failed. Check your details and try again."
        }
        is FirebaseFirestoreException -> when (cause.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "You don't have permission to view this. If this looks wrong, contact the administrator."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Can't reach the server right now. Check your connection and try again."
            FirebaseFirestoreException.Code.NOT_FOUND -> "That information is no longer available."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "The server timed out. Try again."
            else -> fallback
        }
        is IOException -> "Network problem. Check your connection and try again."
        else -> fallback
    }
}

/* ------------------------------------------------------------ loading ----- */

/** Full-screen centred loader. Use only when there is no content to skeleton. */
@Composable
fun LoadingView(msg: String = "Loading…", modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(Spacing.md))
            Text(
                msg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A single grey placeholder block. Static on purpose — no shimmer animation. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp)
) {
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHighest))
}

/**
 * Content-shaped skeleton. Preferred over a spinner for lists because it keeps
 * the layout from jumping when the data lands.
 */
@Composable
fun ListSkeleton(rows: Int = 4, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.listGap)
    ) {
        repeat(rows) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(Spacing.card),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.55f).height(14.dp))
                    SkeletonBlock(Modifier.fillMaxWidth(0.85f).height(11.dp))
                    SkeletonBlock(Modifier.fillMaxWidth(0.35f).height(11.dp))
                }
            }
        }
    }
}

/* -------------------------------------------------------- empty / error --- */

@Composable
fun EmptyView(
    title: String,
    message: String? = null,
    actionLabel: String? = null,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    Box(modifier.fillMaxSize().padding(Spacing.xxxl), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Text(title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            if (message != null) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(Spacing.xs))
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Couldn't load this",
    onRetry: () -> Unit
) {
    Box(modifier.fillMaxSize().padding(Spacing.xxxl), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.semantic.warning,
                modifier = Modifier.size(40.dp)
            )
            Text(title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.xs))
            Button(onClick = onRetry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(MmmutDimens.iconSm)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text("Retry")
            }
        }
    }
}

/* -------------------------------------------------------------- chrome ---- */

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = Spacing.screen, end = Spacing.sm, top = Spacing.lg, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = Spacing.sm)) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

enum class ChipTone { Success, Warning, Danger, Info, Neutral }

/**
 * Status pill. Colours come from the semantic layer so they stay legible in
 * both schemes — hardcoded greens were previously unreadable in dark mode.
 */
@Composable
fun StatusChip(text: String, tone: ChipTone = ChipTone.Neutral, modifier: Modifier = Modifier) {
    val s = MaterialTheme.semantic
    val bg: androidx.compose.ui.graphics.Color
    val fg: androidx.compose.ui.graphics.Color
    when (tone) {
        ChipTone.Success -> { bg = s.successContainer; fg = s.onSuccessContainer }
        ChipTone.Warning -> { bg = s.warningContainer; fg = s.onWarningContainer }
        ChipTone.Danger -> { bg = s.dangerContainer; fg = s.onDangerContainer }
        ChipTone.Info -> { bg = s.infoContainer; fg = s.onInfoContainer }
        ChipTone.Neutral -> { bg = s.neutralContainer; fg = s.onNeutralContainer }
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp), modifier = modifier) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp)
        )
    }
}

/** Unread dot — deliberately small. A giant badge is not needed to signal this. */
@Composable
fun UnreadDot(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

/* ------------------------------------------------------------- content ---- */

@Composable
fun NoticeCard(
    n: Notice,
    modifier: Modifier = Modifier,
    unread: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (unread) {
                    UnreadDot()
                    Spacer(Modifier.width(Spacing.sm))
                }
                if (n.important) {
                    StatusChip("Urgent", ChipTone.Danger)
                    Spacer(Modifier.width(Spacing.sm))
                }
                if (n.pinned) {
                    StatusChip("Pinned", ChipTone.Info)
                    Spacer(Modifier.width(Spacing.sm))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    TimeUtils.formatMillis(n.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                n.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (n.content.isNotBlank()) {
                Text(
                    n.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (n.category.isNotBlank()) {
                Text(
                    n.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Label/value row used across Profile and detail screens. */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false
) {
    if (value.isNullOrBlank()) return
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Named to avoid clashing with `androidx.compose.material3.HorizontalDivider`. */
@Composable
fun MmmutDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
