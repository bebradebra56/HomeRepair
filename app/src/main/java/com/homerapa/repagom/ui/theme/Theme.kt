package com.homerapa.repagom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme background: very slightly blue-tinted white — distinct from pure-white surface
private val LightBackground = Color(0xFFEFF6FB)

// Dark theme palette — strong contrast between layers
private val DarkBackground    = Color(0xFF07111A)  // deepest: near-black navy
private val DarkSurface       = Color(0xFF111D2B)  // cards, clearly visible on background
private val DarkSurfaceVariant= Color(0xFF173149)  // elevated elements (more contrast)
private val DarkOnSurface     = Color(0xFFE2EEFA)  // primary text — bright
private val DarkOnSurfaceVar  = Color(0xFF90BAD8)  // secondary text — readable light blue
private val DarkPrimary       = Color(0xFF6EC6F0)  // primary action color — bright sky blue
private val DarkOutline       = Color(0xFF2E5068)  // borders
private val DarkOutlineVariant= Color(0xFF1A3347)  // subtle dividers

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = WhiteText,
    primaryContainer = SkyBlue,
    onPrimaryContainer = NavyBlue,
    secondary = ConstructionYellow,
    onSecondary = DarkText,
    secondaryContainer = SoftYellow,
    onSecondaryContainer = DarkText,
    tertiary = WarmBrown,
    onTertiary = WhiteText,
    tertiaryContainer = LightWood,
    onTertiaryContainer = DarkText,
    background = LightBackground,
    onBackground = DarkText,
    surface = CardWhite,
    onSurface = DarkText,
    surfaceVariant = CardOffWhite,
    onSurfaceVariant = SecondaryText,
    error = WarmRed,
    onError = WhiteText,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF7FA8C0),
    outlineVariant = Color(0xFFB8D8EC)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF07111A),
    primaryContainer = Color(0xFF214E6E),
    onPrimaryContainer = Color(0xFFBDE4FF),
    secondary = ConstructionYellow,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = Color(0xFF3A2800),
    onSecondaryContainer = SoftYellow,
    tertiary = LightWood,
    onTertiary = Color(0xFF120A00),
    tertiaryContainer = Color(0xFF3A2210),
    onTertiaryContainer = LightWood,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVar,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A0505),
    errorContainer = Color(0xFF5C1F1A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun HomeRepairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
