package com.xpensetrack.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Purple700,
    onPrimary = White,
    primaryContainer = PurpleLight,
    secondary = Gold,
    onSecondary = Color.Black,
    background = White,
    surface = White,
    onBackground = DarkText,
    onSurface = DarkText,
    error = Red500
)

@Composable
fun XpenseTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
