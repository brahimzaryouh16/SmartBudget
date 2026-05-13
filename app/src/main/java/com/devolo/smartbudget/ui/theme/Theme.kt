package com.devolo.smartbudget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Indigo500,
    background = Color(0xFF0F172A), // Slate900
    surface = Color(0xFF1E293B),    // Slate800
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Danger,
    outline = Slate700,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald700,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Indigo50,
    onSecondaryContainer = Indigo600,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Slate100,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = Danger,
    onError = Color.White
)

@Composable
fun SmartBudgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
