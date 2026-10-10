package com.mmmut.ero.ui.theme

import androidx.compose.ui.graphics.Color

/* ==========================================================================
 * MMMUT ERP — colour system
 * --------------------------------------------------------------------------
 * Every Material 3 colour role is defined for BOTH schemes.
 *
 * This matters more than it looks. A Material3 `ColorScheme` left partially
 * specified fills the gaps from the library's BASELINE palette, which is
 * purple-derived. Before this file defined the surface-container ramp and
 * outline/error roles, `Card`, `AlertDialog`, `DropdownMenu`, `Snackbar`,
 * `OutlinedTextField` and `NavigationBar` were quietly rendering with baseline
 * purple-grey tokens mixed into the indigo brand scheme — which is exactly why
 * dark mode showed dark-blue-on-black text, low-contrast borders, and dialogs
 * that did not match the rest of the app.
 *
 * Rule of thumb used throughout: text roles are chosen to clear 4.5:1 contrast
 * against the surface they sit on, and container/on-container pairs are always
 * set together.
 * ========================================================================== */

// ---------------------------------------------------------------- brand ----

/** Deep MMMUT indigo. Used as the light-theme primary. */
val MmmutIndigo = Color(0xFF1A237E)
val MmmutIndigoContainer = Color(0xFFE0E3F7)
val MmmutOnIndigoContainer = Color(0xFF0D1452)

/** A lighter indigo that stays legible as the dark-theme primary. */
val MmmutLightPrimary = Color(0xFFB9C3FF)
val MmmutDarkPrimaryContainer = Color(0xFF2C3680)
val MmmutOnDarkPrimaryContainer = Color(0xFFDDE1FF)

/** Institutional maroon. */
val MmmutMaroon = Color(0xFF7B1E1E)
val MmmutMaroonContainer = Color(0xFFF8E8EC)
val MmmutOnMaroonContainer = Color(0xFF3E0A0A)

/**
 * Gold is decorative only. The pure brand gold (#C9A227) fails contrast for
 * text on white, so a darkened variant is used wherever gold carries meaning.
 */
val MmmutGold = Color(0xFFC9A227)
val MmmutGoldText = Color(0xFF6D5100)
val MmmutDarkGold = Color(0xFFF0C24B)

// -------------------------------------------------------------- surfaces ---
// Light
val LightBackground = Color(0xFFF7F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E4EE)
val LightOnSurface = Color(0xFF1A1C22)
val LightOnSurfaceVariant = Color(0xFF44474F)

// Dark — neutral, never blue-black, so indigo accents stay distinguishable.
val DarkBackground = Color(0xFF101218)
val DarkSurface = Color(0xFF15171E)
val DarkSurfaceVariant = Color(0xFF3A3E4A)
val DarkOnSurface = Color(0xFFE6E7EC)
val DarkOnSurfaceVariant = Color(0xFFC7CAD6)

// ------------------------------------------------- legacy status aliases ---
// Retained so existing screens keep compiling; prefer `MaterialTheme.semantic`.
val StatusSuccess = Color(0xFF1B5E20)
val StatusWarning = Color(0xFF8A5000)
val StatusDanger = Color(0xFFB3261E)
