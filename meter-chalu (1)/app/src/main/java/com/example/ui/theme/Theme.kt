package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MeterChaluDarkColorScheme = darkColorScheme(
    primary = RickshawYellow,
    onPrimary = RickshawBlack,
    primaryContainer = RickshawYellowDark,
    onPrimaryContainer = PureWhite,
    secondary = RickshawYellowLight,
    onSecondary = RickshawBlack,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle
)

private val MeterChaluLightColorScheme = lightColorScheme(
    primary = RickshawYellow,
    onPrimary = RickshawBlack,
    primaryContainer = RickshawYellowLight,
    onPrimaryContainer = TextDark,
    secondary = RickshawYellowDark,
    onSecondary = PureWhite,
    background = Color(0xFFF6F6F8),
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFEBEBEF),
    onSurfaceVariant = Color(0xFF4A4A50),
    outline = Color(0xFFD4D4D8),
    outlineVariant = Color(0xFFE4E4E7)
)

@Composable
fun MeterChaluTheme(
    darkTheme: Boolean = true, // Default to refined high-contrast dark theme (auto rickshaw yellow & black)
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MeterChaluDarkColorScheme else MeterChaluLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
