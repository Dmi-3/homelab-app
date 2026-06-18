package com.homelab.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Sky = Color(0xFF38BDF8)
private val SkyDark = Color(0xFF0EA5E9)
private val Amber = Color(0xFFFACC15)

private val DarkColors = darkColorScheme(
    primary = Sky,
    onPrimary = Color(0xFF00131D),
    primaryContainer = Color(0xFF0B4A63),
    onPrimaryContainer = Color(0xFFBDE8FB),
    secondary = Amber,
    onSecondary = Color(0xFF241A00),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF131C2E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF273349),
    onSurfaceVariant = Color(0xFFB6C2D6),
    outline = Color(0xFF3A465C),
    error = Color(0xFFFF6B6B),
)

private val LightColors = lightColorScheme(
    primary = SkyDark,
    secondary = Color(0xFFB58900),
)

@Composable
fun HomelabTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
