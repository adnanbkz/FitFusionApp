package com.example.fitfusion.ui.theme


import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─── Colors (DESIGN.md — "The Kinetic Editorial") ───

val Primary = Color(0xFF006E0A)
val PrimaryContainer = Color(0xFF32CD32)
val Secondary = Color(0xFF476083)
val SecondaryContainer = Color(0xFFBDD6FF)
val Tertiary = Color(0xFFB02F00)

val Surface = Color(0xFFFBF8FE)
val SurfaceContainerLow = Color(0xFFF6F2F8)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerHigh = Color(0xFFEAE7ED)

val OnSurface = Color(0xFF1B1B1F)
val OnSurfaceVariant = Color(0xFF757575)
val OutlineVariant = Color(0xFFBCCBB4)

// ─── Gradients ───

val GreenGradientBrush = Brush.linearGradient(
    colors = listOf(Primary, PrimaryContainer)
)

// ─── Spacing ───

val Spacing2 = 8.dp
val Spacing4 = 16.dp
val Spacing6 = 24.dp
val Spacing8 = 32.dp