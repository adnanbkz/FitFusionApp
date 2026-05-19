package com.example.fitfusion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.DayLog
import com.example.fitfusion.data.models.LoggedFood
import com.example.fitfusion.data.models.MealSlot
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.OutlineVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Secondary
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.ui.components.AiMealPlanButton
import com.example.fitfusion.ui.theme.Tertiary
import com.example.fitfusion.viewmodel.TrackingViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTracking(
    navController: NavHostController,
    trackingViewModel: TrackingViewModel = viewModel(),
) {
    val state by trackingViewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showEditMacrosDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(SurfaceContainerLow, Surface))
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "DIETA",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp, color = Primary
                    )
                    Text(
                        "Seguimiento",
                        fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerLow)
                            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(10.dp))
                            .clickable { showEditMacrosDialog = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            "Editar Macros",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Primary,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow)
                            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Abrir calendario",
                                tint = Primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                state.selectedDate.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es"))),
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant,
                            )
                        }
                    }
                    AiMealPlanButton(
                        backgroundColor = SurfaceContainerLow,
                        borderColor     = SurfaceContainerHigh,
                        accent          = Primary,
                        textColor       = OnSurface,
                        mutedTextColor  = OnSurfaceVariant,
                        defaultTargetKcal = state.kcalGoal,
                        onPlanGenerated = trackingViewModel::applyAiMealPlan,
                    )
                }
            }
        }

        // ── Week strip ──────────────────────────────────────────────────────
        item {
            NeonWeekStrip(
                selectedDate    = state.selectedDate,
                weekSummaryDays = state.weekSummary.days,
                onDaySelected   = trackingViewModel::selectDate
            )
        }

        // ── Calorie ring ────────────────────────────────────────────────────
        item {
            NeonSurfaceContainerLow(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "RESUMEN DIARIO",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp, color = Primary
                    )
                    Box(contentAlignment = Alignment.Center) {
                        NeonRing(progress = state.netProgress, size = 160)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${state.kcalLeft}",
                                fontSize = 44.sp, fontWeight = FontWeight.Black, color = OnSurface
                            )
                            Text(
                                "KCAL REST.",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp, color = Primary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NeonStatColumn("INGERIDAS", "${state.kcalEaten}", Secondary)
                        NeonDividerV()
                        NeonStatColumn("QUEMADAS",  "${state.kcalBurned}", Tertiary)
                        NeonDividerV()
                        NeonStatColumn("OBJETIVO",  "${state.kcalGoal}", Primary)
                    }
                }
            }
        }

        // ── Workout today ───────────────────────────────────────────────────
        state.dailySummary?.takeIf { it.workoutCount > 0 }?.let { ds ->
            item {
                NeonSurfaceContainerLow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape).background(Tertiary)
                            )
                            Text(
                                "ENTRENAMIENTO HOY",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp, color = Tertiary
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NeonStatColumn("SESIONES", "${ds.workoutCount}", OnSurface)
                            NeonDividerV()
                            NeonStatColumn("KCAL",     "${ds.kcalBurned}", Tertiary)
                            NeonDividerV()
                            NeonStatColumn("VOLUMEN",  "${ds.totalVolumeKg.toInt()} kg", Secondary)
                        }
                    }
                }
            }
        }

        // ── Health Connect ──────────────────────────────────────────────────
        state.healthData?.let { healthData ->
            item {
                NeonSurfaceContainerLow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Secondary))
                                Text("HEALTH CONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Secondary)
                            }
                            Text("Sincronizar", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NeonStatColumn("PASOS",      "${healthData.steps}", OnSurface)
                            NeonDividerV()
                            NeonStatColumn("KCAL PASOS", "${healthData.stepCaloriesEstimated}", Tertiary)
                            NeonDividerV()
                            NeonStatColumn("FC MEDIA",   healthData.averageHeartRate?.let { "$it" } ?: "—", Secondary)
                        }
                    }
                }
            }
        }

        // ── Macros ──────────────────────────────────────────────────────────
        item {
            NeonSurfaceContainerLow(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                            Text("MACROS", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Primary)
                        }
                        TextButton(onClick = { navController.navigate(Screens.WeeklyLogScreen.name) }) {
                            Text("Ver semana →", fontSize = 12.sp, color = Secondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    NeonMacroRow("PROTEÍNAS",     state.dayLog.totalProtein, state.proteinGoal, Primary)
                    NeonMacroRow("CARBOHIDRATOS", state.dayLog.totalCarbs,   state.carbsGoal,   Secondary)
                    NeonMacroRow("GRASAS",        state.dayLog.totalFat,     state.fatsGoal,    Tertiary)

                    // AI tip
                    if (state.aiTip.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLowest)
                                .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("⚡", fontSize = 14.sp)
                                Text(state.aiTip, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Meals header ─────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Secondary))
                    Text("MIS COMIDAS HOY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Secondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary.copy(alpha = 0.12f))
                        .clickable(onClick = trackingViewModel::showAddMealDialog)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("+ Comida", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Meal sections ────────────────────────────────────────────────────
        items(state.dayLog.meals, key = { it.id }) { slot ->
            NeonMealSection(
                slot       = slot,
                entries    = state.dayLog.byMeal[slot] ?: emptyList(),
                isExpanded = slot.id in state.expandedMeals,
                onToggle   = { trackingViewModel.toggleMeal(slot.id) },
                onAdd      = { navController.navigate("${Screens.AddFoodScreen.name}/${slot.id}") },
                onRemove   = trackingViewModel::removeFood,
                onEdit     = trackingViewModel::openEditSheet,
                onRename   = { trackingViewModel.showRenameMealDialog(slot.id, slot.name) },
                onDelete   = { trackingViewModel.removeMeal(slot.id) },
                modifier   = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (state.showAddMealDialog) {
        AddMealDialog(
            name = state.addMealName,
            onNameChange = trackingViewModel::onAddMealNameChange,
            onConfirm = trackingViewModel::confirmAddMeal,
            onDismiss = trackingViewModel::dismissAddMealDialog,
        )
    }

    if (state.showRenameMealDialog) {
        RenameMealDialog(
            name         = state.renameMealName,
            onNameChange = trackingViewModel::onRenameMealNameChange,
            onConfirm    = trackingViewModel::confirmRenameMeal,
            onDismiss    = trackingViewModel::dismissRenameMealDialog
        )
    }

    if (showEditMacrosDialog) {
        EditMacrosDialog(
            initialKcal    = state.kcalGoal,
            initialProtein = state.proteinGoal,
            initialCarbs   = state.carbsGoal,
            initialFats    = state.fatsGoal,
            onSave         = { kcal, protein, carbs, fats ->
                trackingViewModel.saveMacroGoals(kcal, protein, carbs, fats)
                showEditMacrosDialog = false
            },
            onDismiss      = { showEditMacrosDialog = false },
        )
    }

    val ef = state.editFoodState
    if (ef != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = trackingViewModel::dismissEditSheet,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLow,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OnSurfaceVariant) }
        ) {
            FoodDetailSheet(
                food            = ef.loggedFood.food,
                selectedServing = ef.selectedServing,
                quantity        = ef.quantity,
                sheetMealSlot   = ef.mealSlot,
                activeMealSlots = state.dayLog.meals,
                onSelectServing = trackingViewModel::editSelectServing,
                onIncrement     = trackingViewModel::editIncrementQuantity,
                onDecrement     = trackingViewModel::editDecrementQuantity,
                onSelectSlot    = trackingViewModel::editSelectSlot,
                onConfirm       = trackingViewModel::confirmEdit,
                confirmLabel    = "Actualizar",
            )
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        if (!picked.isAfter(LocalDate.now())) {
                            trackingViewModel.selectDate(picked)
                        }
                    }
                    showDatePicker = false
                }) { Text("Aceptar", color = Primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Neon components ───────────────────────────────────────────────────────────

@Composable
private fun NeonSurfaceContainerLow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(20.dp))
    ) { content() }
}

@Composable
private fun NeonRing(progress: Float, size: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "neonRingProgress"
    )
    val sw = if (size > 120) 16f else 10f
    val primaryColor = Primary
    Box(
        modifier = Modifier
            .size(size.dp)
            .drawBehind {
                val inset = sw * 3.5f
                val arcSize = Size(this.size.width - inset, this.size.height - inset)
                val topLeft = Offset(inset / 2, inset / 2)

                // Track
                drawArc(
                    color = primaryColor.copy(alpha = 0.08f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                    topLeft = topLeft, size = arcSize,
                )
                if (animatedProgress > 0f) {
                    val sweep = 360f * animatedProgress
                    // Glow layers
                    drawArc(color = primaryColor.copy(alpha = 0.04f), startAngle = -90f, sweepAngle = sweep, useCenter = false,
                        style = Stroke(width = sw * 5f, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                    drawArc(color = primaryColor.copy(alpha = 0.10f), startAngle = -90f, sweepAngle = sweep, useCenter = false,
                        style = Stroke(width = sw * 3f, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                    drawArc(color = primaryColor.copy(alpha = 0.25f), startAngle = -90f, sweepAngle = sweep, useCenter = false,
                        style = Stroke(width = sw * 1.8f, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                    // Solid arc
                    drawArc(color = primaryColor, startAngle = -90f, sweepAngle = sweep, useCenter = false,
                        style = Stroke(width = sw, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                }
            },
        contentAlignment = Alignment.Center
    ) {}
}

@Composable
private fun NeonStatColumn(label: String, value: String, color: Color = OnSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun NeonDividerV() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(SurfaceContainerHigh)
    )
}

@Composable
private fun NeonMacroRow(label: String, current: Int, goal: Int, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = (current.toFloat() / goal).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "macroProgress"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = color)
            Row {
                Text("$current", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(" / ${goal}g", fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))
                    )
            )
        }
    }
}

// ── Week strip ────────────────────────────────────────────────────────────────

@Composable
private fun NeonWeekStrip(
    selectedDate: LocalDate,
    weekSummaryDays: List<DayLog>,
    onDaySelected: (LocalDate) -> Unit,
) {
    val today     = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
            .border(width = 0.dp, color = Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (0..6).forEach { offset ->
            val date       = weekStart.plusDays(offset.toLong())
            val dayLog     = weekSummaryDays.getOrNull(offset)
            val isToday    = date == today
            val isSelected = date == selectedDate
            val hasData    = (dayLog?.entries?.isNotEmpty()) == true
            val isFuture   = date.isAfter(today)

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Primary.copy(alpha = 0.15f)
                    else       -> Color.Transparent
                },
                label = "dayBg"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .then(
                        if (isSelected) Modifier.border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable(enabled = !isFuture) { onDaySelected(date) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    dayLabels[offset],
                    fontSize   = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = when {
                        isSelected -> Primary
                        isFuture   -> OnSurfaceVariant.copy(alpha = 0.4f)
                        else       -> OnSurfaceVariant
                    }
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday  -> Primary
                                hasData  -> Secondary.copy(alpha = 0.6f)
                                isFuture -> Color.Transparent
                                else     -> OnSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                )
                if (hasData && !isFuture) {
                    Text(
                        "${dayLog.totalKcal}",
                        fontSize   = 8.sp,
                        color      = if (isSelected) Primary else OnSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Spacer(Modifier.height(11.dp))
                }
            }
        }
    }
}

// ── Meal section ──────────────────────────────────────────────────────────────

@Composable
private fun NeonMealSection(
    slot: MealSlot,
    entries: List<LoggedFood>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onEdit: (LoggedFood) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalKcal = entries.sumOf { it.kcal }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(slot.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
                if (entries.isNotEmpty()) {
                    Text(
                        "$totalKcal kcal · ${entries.size} alimento${if (entries.size > 1) "s" else ""}",
                        fontSize = 12.sp, color = Secondary
                    )
                } else {
                    Text("Sin registros", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (slot.isCustom) {
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant, modifier = Modifier.size(15.dp))
                    }
                }
                if (slot.isCustom && entries.isEmpty()) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded && entries.isNotEmpty(), enter = expandVertically(), exit = shrinkVertically()) {
            Column {
                HorizontalDivider(color = SurfaceContainerHigh)
                entries.forEach { logged ->
                    NeonLoggedFoodRow(logged = logged, onEdit = { onEdit(logged) }, onRemove = { onRemove(logged.id) })
                }
            }
        }
    }
}

@Composable
private fun NeonLoggedFoodRow(
    logged: LoggedFood,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh)
                .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Restaurant, null, Modifier.size(18.dp), tint = Primary) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                logged.food.name,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                "${logged.serving.label} × ${logged.quantity}",
                fontSize = 12.sp, color = OnSurfaceVariant
            )
        }

        Text("${logged.kcal}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Secondary)

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Tertiary.copy(alpha = 0.12f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Text("×", fontSize = 16.sp, color = Tertiary, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Dialogs (dark-themed) ─────────────────────────────────────────────────────

@Composable
private fun AddMealDialog(name: String, onNameChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = { Text("Nueva comida", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ponle un nombre a esta comida.", fontSize = 14.sp, color = OnSurfaceVariant)
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("Ej: Post-entreno, Snack...", color = OnSurfaceVariant, fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedBorderColor = SurfaceContainerHigh,
                        focusedBorderColor = Primary,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("Añadir", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnSurfaceVariant) }
        },
    )
}

@Composable
private fun RenameMealDialog(name: String, onNameChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceContainerLow,
        title = { Text("Renombrar comida", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                placeholder = { Text("Nombre de la comida", color = OnSurfaceVariant, fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor   = SurfaceContainerLowest,
                    unfocusedBorderColor    = SurfaceContainerHigh,
                    focusedBorderColor      = Primary,
                    focusedTextColor        = OnSurface,
                    unfocusedTextColor      = OnSurface,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("Guardar", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnSurfaceVariant) }
        }
    )
}

@Composable
private fun EditMacrosDialog(
    initialKcal: Int,
    initialProtein: Int,
    initialCarbs: Int,
    initialFats: Int,
    onSave: (kcal: Int, protein: Int, carbs: Int, fats: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var kcal by remember { mutableStateOf(initialKcal.toString()) }
    var protein by remember { mutableStateOf(initialProtein.toString()) }
    var carbs by remember { mutableStateOf(initialCarbs.toString()) }
    var fats by remember { mutableStateOf(initialFats.toString()) }

    val canSave = (kcal.toIntOrNull() ?: 0) >= 1 &&
        protein.toIntOrNull() != null && carbs.toIntOrNull() != null && fats.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = { Text("Editar macros", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Define tu objetivo diario de calorías y macronutrientes.",
                    fontSize = 14.sp, color = OnSurfaceVariant,
                )
                MacroField("Calorías (kcal)", kcal) { kcal = it }
                MacroField("Proteínas (g)", protein) { protein = it }
                MacroField("Carbohidratos (g)", carbs) { carbs = it }
                MacroField("Grasas (g)", fats) { fats = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        kcal.toIntOrNull() ?: 0,
                        protein.toIntOrNull() ?: 0,
                        carbs.toIntOrNull() ?: 0,
                        fats.toIntOrNull() ?: 0,
                    )
                },
                enabled = canSave,
            ) { Text("Guardar", color = Primary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnSurfaceVariant) }
        },
    )
}

@Composable
private fun MacroField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.all { it.isDigit() } && input.length <= 6) onChange(input) },
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLowest,
            focusedContainerColor = SurfaceContainerLowest,
            unfocusedBorderColor = SurfaceContainerHigh,
            focusedBorderColor = Primary,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
