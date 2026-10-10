package com.mmmut.ero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* ==========================================================================
 * MMMUT ERP — Theme
 * --------------------------------------------------------------------------
 * Both schemes specify every role explicitly. See Color.kt for why that is not
 * optional: any role omitted from a Material3 ColorScheme is filled from the
 * library's baseline purple palette, so partially-specified schemes produce
 * components that belong to a different visual system than the rest of the app.
 *
 * Dynamic colour is intentionally NOT used. The MMMUT brand is part of the
 * product's identity and a student's ERP should look the same on every device.
 * ========================================================================== */

private val LightColors = lightColorScheme(
    primary = MmmutIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E3F7),
    onPrimaryContainer = Color(0xFF0D1452),

    secondary = MmmutMaroon,
    onSecondary = Color.White,
    secondaryContainer = MmmutMaroonContainer,
    onSecondaryContainer = MmmutOnMaroonContainer,

    // Gold alone fails text contrast on white, so the "meaning" roles are darker
    // and the brand gold lives in tertiaryContainer.
    tertiary = MmmutGoldText,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEFC2),
    onTertiaryContainer = Color(0xFF3E2D00),

    background = LightBackground,
    onBackground = LightOnSurface,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    surfaceTint = MmmutIndigo,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD9DBE3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F4FA),
    surfaceContainer = Color(0xFFEEF0F7),
    surfaceContainerHigh = Color(0xFFE8EAF2),
    surfaceContainerHighest = Color(0xFFE2E4EE),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFB9C3FF),
    scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF0F1B5C),
    primaryContainer = MmmutDarkPrimaryContainer,
    onPrimaryContainer = MmmutOnDarkPrimaryContainer,

    secondary = Color(0xFFFFB3B4),
    onSecondary = Color(0xFF5F1313),
    secondaryContainer = Color(0xFF5C1A1D),
    onSecondaryContainer = Color(0xFFFFDADB),

    tertiary = MmmutDarkGold,
    onTertiary = Color(0xFF3E2D00),
    tertiaryContainer = Color(0xFF544200),
    onTertiaryContainer = Color(0xFFFFE08A),

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    // The container ramp is what Card / Dialog / Menu / TextField actually use.
    // It is a neutral lift from the background, never blue-black, so indigo
    // accents remain legible instead of sinking into it.
    surfaceTint = Color(0xFFB9C3FF),
    surfaceBright = Color(0xFF3A3F4C),
    surfaceDim = DarkBackground,
    surfaceContainerLowest = Color(0xFF0B0D12),
    surfaceContainerLow = Color(0xFF171A21),
    surfaceContainer = Color(0xFF1C1F27),
    surfaceContainerHigh = Color(0xFF242833),
    surfaceContainerHighest = Color(0xFF2E323D),

    outline = Color(0xFF8F93A0),
    outlineVariant = Color(0xFF43474F),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = Color(0xFFE6E7EC),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = MmmutIndigo,
    scrim = Color(0xFF000000)
)

/* ------------------------------------------------------------ semantics ---
 * Status colours that Material does not model (success / warning / neutral).
 * Exposed through the theme so screens stop hardcoding greens and oranges that
 * were only legible in one scheme.
 */

@Immutable
data class MmmutSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val neutralContainer: Color,
    val onNeutralContainer: Color
)

private val LightSemantics = MmmutSemanticColors(
    success = Color(0xFF1B5E20), onSuccess = Color.White,
    successContainer = Color(0xFFCDEBCD), onSuccessContainer = Color(0xFF07210A),
    warning = Color(0xFF8A5000), onWarning = Color.White,
    warningContainer = Color(0xFFFFE0B2), onWarningContainer = Color(0xFF2E1A00),
    danger = Color(0xFFB3261E), onDanger = Color.White,
    dangerContainer = Color(0xFFF9DEDC), onDangerContainer = Color(0xFF410E0B),
    info = MmmutIndigo, infoContainer = Color(0xFFE0E3F7), onInfoContainer = Color(0xFF0D1452),
    neutralContainer = Color(0xFFE8EAF2), onNeutralContainer = Color(0xFF44474F)
)

private val DarkSemantics = MmmutSemanticColors(
    success = Color(0xFF7BD88F), onSuccess = Color(0xFF00390F),
    successContainer = Color(0xFF13521F), onSuccessContainer = Color(0xFFB9F3C0),
    warning = Color(0xFFFFB95C), onWarning = Color(0xFF482A00),
    warningContainer = Color(0xFF5C3A00), onWarningContainer = Color(0xFFFFDFAE),
    danger = Color(0xFFFFB4AB), onDanger = Color(0xFF690005),
    dangerContainer = Color(0xFF93000A), onDangerContainer = Color(0xFFFFDAD6),
    info = Color(0xFFB9C3FF), infoContainer = Color(0xFF2C3680), onInfoContainer = Color(0xFFDDE1FF),
    neutralContainer = Color(0xFF2E323D), onNeutralContainer = Color(0xFFC7CAD6)
)

private val LocalSemanticColors = staticCompositionLocalOf { LightSemantics }

/** `MaterialTheme.semantic.success`, etc. */
val MaterialTheme.semantic: MmmutSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSemanticColors.current

@Composable
fun MmmutTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSemanticColors provides if (dark) DarkSemantics else LightSemantics
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = MmmutTypography,
            shapes = MmmutShapes,
            content = content
        )
    }
}
