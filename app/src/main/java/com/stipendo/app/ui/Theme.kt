package com.stipendo.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Neumorphism needs three tones from one base: surface, highlight, and shadow. */
data class NeuColors(
    val background: Color,
    val light: Color,        // highlight cast to the top-left
    val dark: Color,         // shadow cast to the bottom-right
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
)

val LightNeu = NeuColors(
    background = Color(0xFFE0E5EC),
    light = Color(0xFFFFFFFF),
    dark = Color(0xFFA3B1C6),
    textPrimary = Color(0xFF1F2A37),
    textSecondary = Color(0xFF506078),
    accent = Color(0xFF16705F),   // jade
)

val DarkNeu = NeuColors(
    background = Color(0xFF2A2E35),
    light = Color(0xFF383D47),
    dark = Color(0xFF1A1D22),
    textPrimary = Color(0xFFEAEFF5),
    textSecondary = Color(0xFFA9B3C2),
    accent = Color(0xFF52D3B4),
)

val LocalNeu = staticCompositionLocalOf { LightNeu }

// Two clearly different families: a serif for headings, a clean sans for everything else.
// Swap in downloadable fonts later (e.g. Fraunces + Inter) for a stronger identity.
private val StipendoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp, lineHeight = 31.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
)

@Composable
fun StipendoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val neu = if (darkTheme) DarkNeu else LightNeu
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = neu.accent, background = neu.background, surface = neu.background,
            onBackground = neu.textPrimary, onSurface = neu.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = neu.accent, background = neu.background, surface = neu.background,
            onBackground = neu.textPrimary, onSurface = neu.textPrimary,
        )
    }
    CompositionLocalProvider(LocalNeu provides neu) {
        MaterialTheme(colorScheme = scheme, typography = StipendoTypography, content = content)
    }
}
