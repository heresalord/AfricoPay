package com.africopay.pos.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── AfricoPay Brand Colors ───────────────────────────────────────────────────
// Emerald + amber gold, tuned down from neon so it reads as fintech-premium
// rather than a saturated Material-Holo palette.

val AfricoGreen        = Color(0xFF17C787)
val AfricoGreenDark    = Color(0xFF0E9D6A)
val AfricoGreenLight   = Color(0xFF7FE8C4)

val AfricoGold         = Color(0xFFF2B33D)
val AfricoGoldDark     = Color(0xFFD98F1E)

// Dark scheme surfaces — layered, not flat-black.
val AfricoDark         = Color(0xFF0B0F14)
val AfricoDarkSurface  = Color(0xFF141A21)
val AfricoDarkElevated = Color(0xFF1D242D)

val AfricoOnDark       = Color(0xFFF2F4F6)
val AfricoOnDarkMuted  = Color(0xFF8D98A3)

// Light scheme surfaces — soft off-white, not stark white.
val AfricoLight         = Color(0xFFF6F8F7)
val AfricoLightSurface  = Color(0xFFFFFFFF)
val AfricoLightElevated = Color(0xFFEDF1EF)

val AfricoOnLight       = Color(0xFF15201C)
val AfricoOnLightMuted  = Color(0xFF5B6B65)

val AfricoError         = Color(0xFFEF5350)
val AfricoSuccess       = Color(0xFF2FBF71)
val AfricoWarning       = Color(0xFFE0A93B)

// ─── Color Schemes ─────────────────────────────────────────────────────────────

private val AfricoDarkColorScheme = darkColorScheme(
    primary = AfricoGreen,
    onPrimary = Color(0xFF002014),
    primaryContainer = AfricoGreenDark,
    onPrimaryContainer = AfricoGreenLight,

    secondary = AfricoGold,
    onSecondary = Color(0xFF2B1B00),
    secondaryContainer = AfricoGoldDark,
    onSecondaryContainer = Color(0xFFFFF3DC),

    background = AfricoDark,
    onBackground = AfricoOnDark,

    surface = AfricoDarkSurface,
    onSurface = AfricoOnDark,
    surfaceVariant = AfricoDarkElevated,
    onSurfaceVariant = AfricoOnDarkMuted,
    surfaceContainerHighest = AfricoDarkElevated,

    error = AfricoError,
    onError = Color.White,

    outline = Color(0xFF2A3239),
    outlineVariant = Color(0xFF1E252C)
)

private val AfricoLightColorScheme = lightColorScheme(
    primary = AfricoGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9F5E3),
    onPrimaryContainer = Color(0xFF00382A),

    secondary = AfricoGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9C2),
    onSecondaryContainer = Color(0xFF3A2500),

    background = AfricoLight,
    onBackground = AfricoOnLight,

    surface = AfricoLightSurface,
    onSurface = AfricoOnLight,
    surfaceVariant = AfricoLightElevated,
    onSurfaceVariant = AfricoOnLightMuted,
    surfaceContainerHighest = AfricoLightElevated,

    error = AfricoError,
    onError = Color.White,

    outline = Color(0xFFD7DEDA),
    outlineVariant = Color(0xFFE7ECE9)
)

// ─── Shapes ─────────────────────────────────────────────────────────────────
// Softer, more generous rounding across the board — the flat 4dp-corner default
// Material look is a big part of what reads as dated.

val AfricoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ─── Typography ───────────────────────────────────────────────────────────────

private val AfricoTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.3.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
)

// ─── Theme Composable ─────────────────────────────────────────────────────────

/**
 * Follows the system light/dark setting like any modern app — it no longer forces
 * dark mode unconditionally. Call sites that specifically need terminal-style
 * always-dark screens can still request it via [forceDark].
 */
@Composable
fun AfricoPayTheme(
    forceDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = forceDark || isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDark) AfricoDarkColorScheme else AfricoLightColorScheme,
        typography = AfricoTypography,
        shapes = AfricoShapes,
        content = content
    )
}
