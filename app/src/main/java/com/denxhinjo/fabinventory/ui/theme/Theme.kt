package com.denxhinjo.fabinventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The site is dark-only by design (`color-scheme: dark` in globals.css, no
// light-mode toggle) -- the app matches that rather than following the
// system light/dark setting.
private val AppColorScheme = darkColorScheme(
    primary = ColorAccent,
    onPrimary = ColorBg,
    primaryContainer = ColorAccentDim,
    onPrimaryContainer = ColorAccentStrong,
    secondary = ColorAccent2,
    onSecondary = ColorForeground,
    secondaryContainer = ColorSurfaceHover,
    onSecondaryContainer = ColorForeground,
    background = ColorBg,
    onBackground = ColorForeground,
    surface = ColorSurface,
    onSurface = ColorForeground,
    surfaceVariant = ColorSurfaceHover,
    onSurfaceVariant = ColorMuted,
    outline = ColorBorder,
    outlineVariant = ColorBorderStrong,
    error = ColorError,
    onError = ColorBg,
)

@Composable
fun FabInventoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = FabInventoryTypography,
        content = content,
    )
}
