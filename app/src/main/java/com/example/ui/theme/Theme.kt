package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekCustomColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekBlueBg,
    onPrimaryContainer = SleekBlueText,
    secondary = SleekTextLight,
    onSecondary = Color.White,
    background = SleekBg,
    onBackground = SleekTextDark,
    surface = SleekSurface,
    onSurface = SleekTextDark,
    surfaceVariant = SleekBorderLight,
    onSurfaceVariant = SleekTextMedium,
    outline = SleekBorderDark,
    error = Color(0xFFEF4444)
)

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekBlueBg,
    onPrimaryContainer = SleekBlueText,
    secondary = SleekTextMuted,
    onSecondary = Color.White,
    background = SleekCodeBg,
    onBackground = Color.White,
    surface = SleekCodeBorder,
    onSurface = Color.White,
    outline = SleekCodeBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep constant Sleek Interface branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SleekDarkColorScheme else SleekCustomColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
