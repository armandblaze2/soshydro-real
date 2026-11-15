package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SosHydroDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Slate800,
    onPrimaryContainer = SkyTelemetry,
    secondary = GasOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2B1A12),
    onSecondaryContainer = GasAmber,
    tertiary = StockEmerald,
    onTertiary = Color(0xFF04261B),
    background = Slate950,
    onBackground = Color.White,
    surface = Slate900,
    onSurface = Color.White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate850,
    error = DangerRed,
    onError = Color.White
)

private val SosHydroShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun SosHydroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SosHydroDarkColorScheme,
        typography = SosHydroTypography,
        shapes = SosHydroShapes,
        content = content
    )
}
