package com.example.placeverify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightWhiteColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    background = OffWhiteBg,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = InputBg,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = CardBorder
)

@Composable
fun PlaceVerifyTheme(
    darkTheme: Boolean = false, // Pure crisp white theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightWhiteColorScheme,
        typography = Typography,
        content = content
    )
}
