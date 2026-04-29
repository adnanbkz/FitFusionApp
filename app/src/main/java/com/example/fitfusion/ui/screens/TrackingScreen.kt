package com.example.fitfusion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.R
import com.example.fitfusion.data.models.DayLog
import com.example.fitfusion.data.models.LoggedFood
import com.example.fitfusion.data.models.MealSlot
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.fitfusion.ui.components.MacroRow
import com.example.fitfusion.ui.components.MomentumRing
import com.example.fitfusion.ui.components.StatColumn
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.TrackingViewModel
import java.time.DayOfWeek
import java.time.LocalDate
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_dumbbell), null, Modifier.size(18.dp), tint = Color.White)
                    }
                    Text("FitFusion", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                Text(
                    state.selectedDate.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es"))),
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            WeekStrip(
                selectedDate = state.selectedDate,
                weekSummaryDays = state.weekSummary.days,
                onDaySelected = trackingViewModel::selectDate
            )
        }

        item {
            Card(
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "RESUMEN DIARIO",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp, color = Primary
                    )
                    Box(contentAlignment = Alignment.Center) {
                        MomentumRing(progress = state.netProgress, size = 140)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${state.kcalLeft}",
                                fontSize = 40.sp, fontWeight = FontWeight.Bold, color = OnSurface
                            )
                            Text(
                                "KCAL RESTANTES",
                                fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                color = OnSurfaceVariant, letterSpacing = 1.sp
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("INGERIDAS", "${state.kcalEaten}")
                        StatColumn("QUEMADAS",  "${state.kcalBurned}")
                        StatColumn("OBJETIVO",  "${state.kcalGoal}")
                    }
                }
            }
        }

        state.dailySummary?.takeIf { it.workoutCount > 0 }?.let { ds ->
            item {
                Card(
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "ENTRENAMIENTO DE HOY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn("SESIONES", "${ds.workoutCount}")
                            StatColumn("KCAL",     "${ds.kcalBurned}")
                            StatColumn("VOLUMEN",  "${ds.totalVolumeKg.toInt()} kg")
                        }
                    }
                }
            }
        }

        state.healthData?.let { healthData ->
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ACTIVIDAD HEALTH CONNECT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Primary
                            )
                            Text("Sincronizado", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn("PASOS", "${healthData.steps}")
                            StatColumn("KCAL PASOS", "${healthData.stepCaloriesEstimated}")
                            StatColumn("FC MEDIA", healthData.averageHeartRate?.let { "$it" } ?: "—")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Balance de macros", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        TextButton(onClick = { navController.navigate(Screens.WeeklyLogScreen.name) }) {
                            Text("Ver semana →", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroRow("PROTEÍNAS",     state.dayLog.totalProtein, state.proteinGoal, Primary)
                        MacroRow("CARBOHIDRATOS", state.dayLog.totalCarbs,   state.carbsGoal,   Secondary)
                        MacroRow("GRASAS",        state.dayLog.totalFat,     state.fatsGoal,    Tertiary)
                    }
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(state.aiTip, fontSize = 13.sp, color = OnSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MIS COMIDAS HOY", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                TextButton(onClick = trackingViewModel::showAddMealDialog) {
                    Text("+ Comida", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(state.dayLog.meals, key = { it.id }) { slot ->
            MealSection(
                slot       = slot,
                entries    = state.dayLog.byMeal[slot] ?: emptyList(),
                isExpanded = slot.id in state.expandedMeals,
                onToggle   = { trackingViewModel.toggleMeal(slot.id) },
                onAdd      = { navController.navigate("${Screens.AddFoodScreen.name}/${slot.id}") },
                onRemove   = trackingViewModel::removeFood,
                onEdit     = trackingViewModel::openEditSheet,
                onRename   = { trackingViewModel.showRenameMealDialog(slot.id, slot.name) },
                onDelete   = { trackingViewModel.removeMeal(slot.id) },
            )
        }
    }

    if (state.showAddMealDialog) {
        AddMealDialog(
            name         = state.addMealName,
            onNameChange = trackingViewModel::onAddMealNameChange,
            onConfirm    = trackingViewModel::confirmAddMeal,
            onDismiss    = trackingViewModel::dismissAddMealDialog
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

    val ef = state.editFoodState
    if (ef != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = trackingViewModel::dismissEditSheet,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
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
}


@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    weekSummaryDays: List<DayLog>,
    onDaySelected: (LocalDate) -> Unit,
) {
    val today     = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..6).forEach { offset ->
                val date    = weekStart.plusDays(offset.toLong())
                val dayLog  = weekSummaryDays.getOrNull(offset)
                val isToday    = date == today
                val isSelected = date == selectedDate
                val hasData    = (dayLog?.entries?.isNotEmpty()) == true
                val isFuture   = date.isAfter(today)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isFuture) { onDaySelected(date) }
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        dayLabels[offset],
                        fontSize   = 12.sp,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = when {
                            isSelected -> Primary
                            isFuture   -> OnSurfaceVariant.copy(alpha = 0.4f)
                            else       -> OnSurfaceVariant
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isToday  -> Primary
                                    hasData  -> PrimaryContainer.copy(alpha = 0.6f)
                                    isFuture -> Color.Transparent
                                    else     -> SurfaceContainerHigh
                                }
                            )
                    )
                    if (hasData && !isFuture) {
                        Text(
                            "${dayLog.totalKcal}",
                            fontSize = 9.sp,
                            color    = if (isSelected) Primary else OnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSection(
    slot: MealSlot,
    entries: List<LoggedFood>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onEdit: (LoggedFood) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val totalKcal = entries.sumOf { it.kcal }

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
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
                            fontSize = 12.sp, color = OnSurfaceVariant
                        )
                    } else {
                        Text("Sin registros", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Renombrar", tint = OnSurfaceVariant, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar comida", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Primary, modifier = Modifier.size(20.dp))
                    }
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && entries.isNotEmpty(),
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        color = OutlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    entries.forEach { logged ->
                        LoggedFoodRow(
                            logged   = logged,
                            onEdit   = { onEdit(logged) },
                            onRemove = { onRemove(logged.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggedFoodRow(
    logged: LoggedFood,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Text(logged.food.emoji, fontSize = 18.sp) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                logged.food.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                "${logged.serving.label} × ${logged.quantity}",
                fontSize = 12.sp,
                color    = OnSurfaceVariant
            )
        }

        Text(
            "${logged.kcal} kcal",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = OnSurface
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Text("×", fontSize = 16.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddMealDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceContainerLowest,
        title = { Text("Nueva comida", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ponle un nombre a esta comida.",
                    fontSize = 14.sp, color = OnSurfaceVariant
                )
                OutlinedTextField(
                    value         = name,
                    onValueChange = onNameChange,
                    placeholder   = { Text("Ej: Post-entreno, Snack...", color = OnSurfaceVariant, fontSize = 14.sp) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedContainerColor   = SurfaceContainerLow,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick  = onConfirm,
                enabled  = name.isNotBlank()
            ) {
                Text("Añadir", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun RenameMealDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceContainerLowest,
        title = { Text("Renombrar comida", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            OutlinedTextField(
                value         = name,
                onValueChange = onNameChange,
                placeholder   = { Text("Nombre de la comida", color = OnSurfaceVariant, fontSize = 14.sp) },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor   = SurfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Primary,
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
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}
