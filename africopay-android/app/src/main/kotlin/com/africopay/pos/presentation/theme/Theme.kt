package com.africopay.pos.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// ─── AfricoPay Brand Colors ───────────────────────────────────────────────────
// Inspired by UEMOA/Francophone Africa — Deep emerald meets bold amber gold.

val AfricoGreen        = Color(0xFF00C853)   // Primary brand green
val AfricoGreenDark    = Color(0xFF00963E)
val AfricoGreenLight   = Color(0xFF69F0AE)

val AfricoGold         = Color(0xFFFFAB00)   // Accent amber gold
val AfricoGoldDark     = Color(0xFFFF6F00)

val AfricoDark         = Color(0xFF0D1117)   // Background near-black
val AfricoDarkSurface  = Color(0xFF161B22)   // Surface dark card
val AfricoDarkElevated = Color(0xFF21262D)   // Elevated elements

val AfricoOnDark       = Color(0xFFEAECEF)   // Primary text on dark
val AfricoOnDarkMuted  = Color(0xFF8B949E)   // Secondary text

val AfricoError        = Color(0xFFF85149)
val AfricoSuccess      = Color(0xFF3FB950)
val AfricoWarning      = Color(0xFFD29922)

// ─── Dark Color Scheme ────────────────────────────────────────────────────────

private val AfricoColorScheme = darkColorScheme(
    primary          = AfricoGreen,
    onPrimary        = Color.Black,
    primaryContainer = AfricoGreenDark,
    onPrimaryContainer = AfricoGreenLight,

    secondary        = AfricoGold,
    onSecondary      = Color.Black,
    secondaryContainer = AfricoGoldDark,
    onSecondaryContainer = Color(0xFFFFF3E0),

    background       = AfricoDark,
    onBackground     = AfricoOnDark,

    surface          = AfricoDarkSurface,
    onSurface        = AfricoOnDark,
    surfaceVariant   = AfricoDarkElevated,
    onSurfaceVariant = AfricoOnDarkMuted,

    error            = AfricoError,
    onError          = Color.White,

    outline          = Color(0xFF30363D)
)

// ─── Typography ───────────────────────────────────────────────────────────────
// Uses system default sans-serif; in a full implementation, load Inter/Outfit from Google Fonts.

private val AfricoTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp, fontWeight = FontWeight.Bold
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp, fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp, fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp
    )
)

// ─── Theme Composable ─────────────────────────────────────────────────────────

@Composable
fun AfricoPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AfricoColorScheme,
        typography = AfricoTypography,
        content = content
    )
}
