package com.example.fitfusion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.fitfusion.data.ThemeMode

private val LightM3Scheme = lightColorScheme(
    primary          = androidx.compose.ui.graphics.Color(0xFF006E0A),
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF32CD32),
    secondary        = androidx.compose.ui.graphics.Color(0xFF476083),
    tertiary         = androidx.compose.ui.graphics.Color(0xFFB02F00),
    background       = androidx.compose.ui.graphics.Color(0xFFFBF8FE),
    surface          = androidx.compose.ui.graphics.Color(0xFFFBF8FE),
    onSurface        = androidx.compose.ui.graphics.Color(0xFF1B1B1F),
)

private val DarkNeonM3Scheme = darkColorScheme(
    primary          = androidx.compose.ui.graphics.Color(0xFF00FF7F),
    onPrimary        = androidx.compose.ui.graphics.Color(0xFF003919),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF00CC5A),
    secondary        = androidx.compose.ui.graphics.Color(0xFF00D4FF),
    tertiary         = androidx.compose.ui.graphics.Color(0xFFFF6B35),
    background       = androidx.compose.ui.graphics.Color(0xFF080D18),
    surface          = androidx.compose.ui.graphics.Color(0xFF080D18),
    onSurface        = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
)

@Composable
fun FitFusionTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val fitFusionColors = when (themeMode) {
        ThemeMode.LIGHT     -> lightFitFusionColors()
        ThemeMode.DARK_NEON -> darkNeonFitFusionColors()
    }
    val m3Scheme = when (themeMode) {
        ThemeMode.LIGHT     -> LightM3Scheme
        ThemeMode.DARK_NEON -> DarkNeonM3Scheme
    }
    CompositionLocalProvider(LocalFitFusionColors provides fitFusionColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography  = Typography,
            content     = content,
        )
    }
}
