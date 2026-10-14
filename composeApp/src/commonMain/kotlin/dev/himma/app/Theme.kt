package dev.himma.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Orange = Color(0xFFFF5A1F)

private val Light = lightColorScheme(
    primary = Color(0xFFE5481A), onPrimary = Color.White, primaryContainer = Color(0xFFFFE2D4), onPrimaryContainer = Color(0xFF3A0E00),
    secondary = Color(0xFF8A6A4F), tertiary = Color(0xFFC79A3B), background = Color(0xFFFFF8F0), surface = Color(0xFFFFF8F0), surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1E7DA), onSurface = Color(0xFF1A1613), onSurfaceVariant = Color(0xFF6B6157), outlineVariant = Color(0xFFE6DACB),
)
private val Dark = darkColorScheme(
    primary = Orange, onPrimary = Color(0xFF1A0A02), primaryContainer = Color(0xFF3B1A0C), onPrimaryContainer = Color(0xFFFFDDD0),
    secondary = Color(0xFFD9B99A), tertiary = Color(0xFFE2B857), background = Color(0xFF0F0F0F), surface = Color(0xFF0F0F0F), surfaceContainer = Color(0xFF1A1613),
    surfaceContainerHigh = Color(0xFF2A241F), onSurface = Color(0xFFF5EFE6), onSurfaceVariant = Color(0xFFA89E92), outlineVariant = Color(0xFF332C26),
)

@Composable
fun HimmaTheme(mode: String = "system", content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content,
    )
}
