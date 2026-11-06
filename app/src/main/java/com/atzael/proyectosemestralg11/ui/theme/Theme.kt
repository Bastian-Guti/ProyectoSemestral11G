package com.atzael.proyectosemestralg11.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBlueTopBar = Color(0xFF1E3A8A) // Dark blue TopBar
val DarkGrayBackground = Color(0xFF121212) // Dark gray background
val DarkGraySurface = Color(0xFF1E1E1E) // Surface / Cards
val LightGrayPanel = Color(0xFF2D2D2D) // Light gray text panels / selection boxes
val TextWhite = Color(0xFFFFFFFF) // White text
val TextLightGray = Color(0xFFD1D5DB) // Light gray secondary text

private val CustomDarkColorScheme = darkColorScheme(
    primary = DarkBlueTopBar,
    onPrimary = TextWhite,
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = TextWhite,
    secondary = Color(0xFF3B82F6),
    onSecondary = TextWhite,
    background = DarkGrayBackground,
    onBackground = TextWhite,
    surface = DarkGraySurface,
    onSurface = TextWhite,
    surfaceVariant = LightGrayPanel,
    onSurfaceVariant = TextLightGray,
    error = Color(0xFFEF4444),
    onError = TextWhite
)

@Composable
fun ProyectoSemestralG11Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
