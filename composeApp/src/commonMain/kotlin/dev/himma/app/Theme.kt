package dev.himma.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.himma.core.Palettes

/** What a colour theme changes: the accent and its container, for light and for dark. Surfaces and text stay the Zimam warm greys. */
internal class Accent(val light: Color, val lightContainer: Color, val dark: Color, val darkContainer: Color, val onLight: Color = Color.White, val onDark: Color = Color(0xFF14110F))

/** Every theme is defined here once; screens only read MaterialTheme colours. */
internal val accents: Map<String, Accent> = mapOf(
    "zimam" to Accent(Color(0xFFE5481A), Color(0xFFFFE2D4), Color(0xFFFF5A1F), Color(0xFF3B1A0C), onDark = Color(0xFF1A0A02)),
    "ocean" to Accent(Color(0xFF0B63BF), Color(0xFFD6E8FF), Color(0xFF55A8FF), Color(0xFF0F2A47)),
    "forest" to Accent(Color(0xFF2A7430), Color(0xFFD7EDD8), Color(0xFF6CC070), Color(0xFF12301A)),
    "violet" to Accent(Color(0xFF6A3FC8), Color(0xFFE6DCFF), Color(0xFFB095FF), Color(0xFF2A1B52)),
    "rose" to Accent(Color(0xFFC0245A), Color(0xFFFFD9E5), Color(0xFFFF7AA3), Color(0xFF4A1428)),
    "graphite" to Accent(Color(0xFF3F4A55), Color(0xFFE1E6EB), Color(0xFFAEBBC9), Color(0xFF232A31)),
)

internal fun colorSchemeFor(palette: String, dark: Boolean): ColorScheme {
    val a = accents[palette] ?: accents.getValue(Palettes.DEFAULT)
    return if (dark) darkColorScheme(
        primary = a.dark, onPrimary = a.onDark, primaryContainer = a.darkContainer, onPrimaryContainer = Color(0xFFFFEDE6), secondaryContainer = a.darkContainer, onSecondaryContainer = Color(0xFFFFEDE6),
        secondary = Color(0xFFD9B99A), tertiary = Color(0xFFE2B857), background = Color(0xFF0F0F0F), surface = Color(0xFF0F0F0F), surfaceContainer = Color(0xFF1A1613),
        surfaceContainerHigh = Color(0xFF2A241F), onSurface = Color(0xFFF5EFE6), onSurfaceVariant = Color(0xFFA89E92), outlineVariant = Color(0xFF332C26),
    ) else lightColorScheme(
        primary = a.light, onPrimary = a.onLight, primaryContainer = a.lightContainer, onPrimaryContainer = Color(0xFF1A1613), secondaryContainer = a.lightContainer, onSecondaryContainer = Color(0xFF1A1613),
        secondary = Color(0xFF8A6A4F), tertiary = Color(0xFFC79A3B), background = Color(0xFFFFF8F0), surface = Color(0xFFFFF8F0), surfaceContainer = Color(0xFFFFFFFF),
        surfaceContainerHigh = Color(0xFFF1E7DA), onSurface = Color(0xFF1A1613), onSurfaceVariant = Color(0xFF6B6157), outlineVariant = Color(0xFFE6DACB),
    )
}

/** Font scale for each text size step: small, normal, large, largest. */
internal val textScales = floatArrayOf(0.88f, 1f, 1.15f, 1.3f)

@Composable
fun HimmaTheme(mode: String = "system", palette: String = Palettes.DEFAULT, textSize: Int = 1, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * textScales[textSize.coerceIn(0, 3)])) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette, dark),
            shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
            content = content,
        )
    }
}
