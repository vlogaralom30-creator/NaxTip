package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TikPink,
    onPrimary = TikWhite,
    secondary = TikCyan,
    onSecondary = TikBlack,
    tertiary = TikPurple,
    onTertiary = TikWhite,
    background = TikBlack,
    onBackground = TikWhite,
    surface = TikDarkSurface,
    onSurface = TikWhite,
    surfaceVariant = TikSurfaceVariant,
    onSurfaceVariant = TikTextSecondary,
    outline = TikBorder,
    error = TikRed,
    onError = TikWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
