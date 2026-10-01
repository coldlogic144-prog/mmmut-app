package com.mmmut.ero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MmmutIndigo,
    onPrimary = Color.White,
    primaryContainer = MmmutIndigoContainer,
    onPrimaryContainer = MmmutOnIndigoContainer,

    secondary = MmmutMaroon,
    onSecondary = Color.White,
    secondaryContainer = MmmutMaroonContainer,
    onSecondaryContainer = Color(0xFF33000C),

    tertiary = MmmutGold,
    onTertiary = Color.White,

    background = LightBackground,
    onBackground = LightOnSurface,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    outline = Color(0xFF757780)
)

private val DarkColors = darkColorScheme(
    primary = MmmutLightPrimary,
    onPrimary = Color(0xFF000865),
    primaryContainer = MmmutDarkPrimaryContainer,
    onPrimaryContainer = MmmutOnDarkPrimaryContainer,

    secondary = Color(0xFFEF9A9A),
    onSecondary = Color(0xFF4A0010),
    secondaryContainer = Color(0xFF670018),
    onSecondaryContainer = Color(0xFFFFDADF),

    tertiary = MmmutDarkGold,
    onTertiary = Color(0xFF3E2D00),

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = Color(0xFF8F909A)
)

@Composable
fun MmmutTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = MmmutTypography,
        content = content
    )
}
