package com.example.fitfusion.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FitFusionColors(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val surface: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineVariant: Color,
    val isDark: Boolean,
)

fun lightFitFusionColors() = FitFusionColors(
    primary             = Color(0xFF006E0A),
    primaryContainer    = Color(0xFF32CD32),
    secondary           = Color(0xFF476083),
    secondaryContainer  = Color(0xFFBDD6FF),
    tertiary            = Color(0xFFB02F00),
    surface             = Color(0xFFFBF8FE),
    surfaceContainerLow     = Color(0xFFF6F2F8),
    surfaceContainerLowest  = Color(0xFFFFFFFF),
    surfaceContainerHigh    = Color(0xFFEAE7ED),
    onSurface           = Color(0xFF1B1B1F),
    onSurfaceVariant    = Color(0xFF757575),
    outlineVariant      = Color(0xFFBCCBB4),
    isDark              = false,
)

fun darkNeonFitFusionColors() = FitFusionColors(
    primary             = Color(0xFF00FF7F),
    primaryContainer    = Color(0xFF00CC5A),
    secondary           = Color(0xFF00D4FF),
    secondaryContainer  = Color(0xFF003A6B),
    tertiary            = Color(0xFFFF6B35),
    surface             = Color(0xFF080D18),
    surfaceContainerLow     = Color(0xFF0F1629),
    surfaceContainerLowest  = Color(0xFF060A12),
    surfaceContainerHigh    = Color(0xFF1E293B),
    onSurface           = Color(0xFFE2E8F0),
    onSurfaceVariant    = Color(0xFF94A3B8),
    outlineVariant      = Color(0xFF1E293B),
    isDark              = true,
)

val LocalFitFusionColors = staticCompositionLocalOf { lightFitFusionColors() }
