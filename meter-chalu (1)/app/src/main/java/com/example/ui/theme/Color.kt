package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Premium Auto-Rickshaw Yellow & Dark Palette (Fintech/Calm Dark Quality)
// Brand identity: Auto-rickshaw yellow (#FFC400) and deep charcoal blacks.
// No red used anywhere; errors & misses use amber or road gray; green is success.
// ============================================================================

// Core Yellow Brand & Warm Gradient
val RickshawYellow = Color(0xFFFFC400)
val RickshawYellowLight = Color(0xFFFFD54A)
val RickshawYellowDark = Color(0xFFFFA000)
val RickshawYellowDeep = Color(0xFFE69500)
val RickshawYellowContainer = Color(0xFF261E05)

val YellowGradient = Brush.horizontalGradient(
    colors = listOf(RickshawYellowLight, RickshawYellow, RickshawYellowDark)
)
val YellowVerticalGradient = Brush.verticalGradient(
    colors = listOf(RickshawYellowLight, RickshawYellow)
)
val HeroGlowBrush = Brush.radialGradient(
    colors = listOf(RickshawYellow.copy(alpha = 0.22f), Color.Transparent)
)

// Layered Dark Surfaces (Refined Depth)
val RickshawBlack = Color(0xFF0B0B0C)          // Deepest background
val DarkBackground = Color(0xFF0B0B0C)
val RickshawDarkSurface = Color(0xFF141417)    // Card base
val DarkSurface = Color(0xFF141417)
val DarkSurfaceElevated = Color(0xFF1C1C21)    // Nested / elevated cards
val DarkSurfaceHighlight = Color(0xFF24242A)   // Selected state / hover

// Borders & Dividers
val RickshawCardBorder = Color(0xFF26262C)
val DarkBorder = Color(0xFF26262C)
val DarkBorderSubtle = Color(0xFF1F1F24)
val DarkBorderHighlight = Color(0xFFFFC400).copy(alpha = 0.45f)
val RoadGray = Color(0xFF26262C)

// Typography & Content Tones
val PureWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFFF5F5F7)
val TextLight = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFFA0A0AA)
val TextMuted = Color(0xFF71717A)
val TextDark = Color(0xFF121214)

// Tinted Success (Green - Only for success, "शाबास!", mastered, strong)
val RickshawGreen = Color(0xFF2E7D32)
val GreenAccent = Color(0xFF43A047)
val RickshawGreenSurface = Color(0xFF0E2313)
val RickshawGreenBorder = Color(0xFF1B4D27)
val GreenText = Color(0xFF66BB6A)

// Tinted Warning / Miss (Amber - Never red)
val RickshawAmber = Color(0xFFF57F17)
val AmberAccent = Color(0xFFFFB300)
val RickshawAmberSurface = Color(0xFF261B07)
val RickshawAmberBorder = Color(0xFF593E0A)
val AmberText = Color(0xFFFFCA28)
