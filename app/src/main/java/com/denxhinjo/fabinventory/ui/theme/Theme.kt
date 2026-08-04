package com.denxhinjo.fabinventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Matches the web app's light, amber-accented construction/industrial
// identity (frontend/tailwind.config.js + frontend/src/index.css) rather
// than following the system light/dark setting -- both clients should read
// as the same product. A dark variant can be added later as a real design
// decision, not by accident of two apps independently picking different themes.
private val AppColorScheme = lightColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorSurface,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = ColorAccent2,
    onSecondary = ColorSurface,
    secondaryContainer = ColorSurfaceVariant,
    onSecondaryContainer = ColorForeground,
    background = ColorBackground,
    onBackground = ColorForeground,
    surface = ColorSurface,
    onSurface = ColorForeground,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorMuted,
    outline = ColorBorder,
    outlineVariant = ColorBorderStrong,
    error = ColorError,
    onError = ColorSurface,
)

@Composable
fun FabInventoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = FabInventoryTypography,
        content = content,
    )
}
