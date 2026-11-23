package com.example.placeverify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    background = LightNeutral,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White,
    outline = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    secondary = DeepNavy,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF334155)
)

@Composable
fun PlaceVerifyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
