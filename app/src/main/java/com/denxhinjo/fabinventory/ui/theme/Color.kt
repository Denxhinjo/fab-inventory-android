package com.denxhinjo.fabinventory.ui.theme

import androidx.compose.ui.graphics.Color

// Mirrors the web app's design tokens (frontend/tailwind.config.js `brand`
// palette + Tailwind's stock `slate`/`red`/`green` scales used throughout
// frontend/src/index.css) so both clients read as the same product: a light,
// amber-accented, construction/industrial identity -- not two unrelated apps.
val ColorAmber50 = Color(0xFFFFFBEB)
val ColorAmber100 = Color(0xFFFEF3C7)
val ColorAmber200 = Color(0xFFFDE68A)
val ColorAmber500 = Color(0xFFF59E0B)
val ColorAmber600 = Color(0xFFD97706)
val ColorAmber700 = Color(0xFFB45309)
val ColorAmber800 = Color(0xFF92400E)

val ColorSlate50 = Color(0xFFF8FAFC)
val ColorSlate100 = Color(0xFFF1F5F9)
val ColorSlate200 = Color(0xFFE2E8F0)
val ColorSlate300 = Color(0xFFCBD5E1)
val ColorSlate400 = Color(0xFF94A3B8)
val ColorSlate500 = Color(0xFF64748B)
val ColorSlate600 = Color(0xFF475569)
val ColorSlate700 = Color(0xFF334155)
val ColorSlate800 = Color(0xFF1E293B)
val ColorSlate900 = Color(0xFF0F172A)

val ColorRed500 = Color(0xFFEF4444)
val ColorRed600 = Color(0xFFDC2626)
val ColorRed700 = Color(0xFFB91C1C)

val ColorGreen500 = Color(0xFF22C55E)
val ColorGreen600 = Color(0xFF16A34A)
val ColorGreen700 = Color(0xFF15803D)

// Semantic aliases used across the app -- prefer these over the raw palette
// above so a future re-theme only has to change values in one place.
val ColorPrimary = ColorAmber500
val ColorPrimaryDark = ColorAmber600
val ColorPrimaryContainer = ColorAmber100
val ColorOnPrimaryContainer = ColorAmber800

val ColorBackground = ColorSlate50
val ColorSurface = Color.White
val ColorSurfaceVariant = ColorSlate100
val ColorBorder = ColorSlate200
val ColorBorderStrong = ColorSlate300

val ColorForeground = ColorSlate900
val ColorMuted = ColorSlate500
val ColorMutedDim = ColorSlate400

val ColorError = ColorRed600
val ColorSuccess = ColorGreen600

// Kept for the handful of dashboard charts/gauges that want a second accent
// distinct from the primary amber (e.g. a two-series chart legend, or a
// dark-to-amber gradient on the dashboard's hero card).
val ColorAccent = ColorAmber500
val ColorAccent2 = ColorSlate700
