package com.mmmut.ero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Light = lightColorScheme(
    primary = MmmutIndigo, onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = MmmutMaroon, tertiary = MmmutGold
)
private val Dark = darkColorScheme(
    primary = MmmutIndigoLight, secondary = MmmutGold, tertiary = MmmutGold
)

@Composable
fun MmmutTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = MmmutTypography, content = content)
}
