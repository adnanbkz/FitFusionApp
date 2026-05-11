package com.example.fitfusion.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─── Colors — read from the active FitFusionColors via CompositionLocal ───────

val Primary: Color
    @Composable get() = LocalFitFusionColors.current.primary

val PrimaryContainer: Color
    @Composable get() = LocalFitFusionColors.current.primaryContainer

val Secondary: Color
    @Composable get() = LocalFitFusionColors.current.secondary

val SecondaryContainer: Color
    @Composable get() = LocalFitFusionColors.current.secondaryContainer

val Tertiary: Color
    @Composable get() = LocalFitFusionColors.current.tertiary

val Surface: Color
    @Composable get() = LocalFitFusionColors.current.surface

val SurfaceContainerLow: Color
    @Composable get() = LocalFitFusionColors.current.surfaceContainerLow

val SurfaceContainerLowest: Color
    @Composable get() = LocalFitFusionColors.current.surfaceContainerLowest

val SurfaceContainerHigh: Color
    @Composable get() = LocalFitFusionColors.current.surfaceContainerHigh

val OnSurface: Color
    @Composable get() = LocalFitFusionColors.current.onSurface

val OnSurfaceVariant: Color
    @Composable get() = LocalFitFusionColors.current.onSurfaceVariant

val OutlineVariant: Color
    @Composable get() = LocalFitFusionColors.current.outlineVariant

// ─── Gradients ────────────────────────────────────────────────────────────────

val GreenGradientBrush: Brush
    @Composable get() = Brush.linearGradient(listOf(Primary, PrimaryContainer))

// ─── Spacing ──────────────────────────────────────────────────────────────────

val Spacing2 = 8.dp
val Spacing4 = 16.dp
val Spacing6 = 24.dp
val Spacing8 = 32.dp
