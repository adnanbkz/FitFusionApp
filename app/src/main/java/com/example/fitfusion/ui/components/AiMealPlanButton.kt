package com.example.fitfusion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitfusion.data.ai.AiMealPlanRequest
import com.example.fitfusion.data.ai.AiMealPlanResponse
import com.example.fitfusion.data.repository.AiRepository
import kotlinx.coroutines.launch

private val RESTRICTION_OPTIONS = listOf(
    "Vegetariano",
    "Vegano",
    "Sin gluten",
    "Sin lactosa",
    "Bajo en sodio",
    "Sin frutos secos",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMealPlanButton(
    backgroundColor: Color,
    borderColor: Color,
    accent: Color,
    textColor: Color,
    mutedTextColor: Color,
) {
    var showForm by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AiMealPlanResponse?>(null) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { showForm = true }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Text("Plan IA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }

    if (showForm) {
        MealPlanFormSheet(
            accent = accent,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            cardColor = backgroundColor,
            onDismiss = { showForm = false },
            onResult = {
                showForm = false
                result = it
            },
        )
    }

    result?.let { plan ->
        MealPlanResultDialog(plan = plan, onDismiss = { result = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MealPlanFormSheet(
    accent: Color,
    textColor: Color,
    mutedTextColor: Color,
    cardColor: Color,
    onDismiss: () -> Unit,
    onResult: (AiMealPlanResponse) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var targetKcal by remember { mutableStateOf("2000") }
    var mealsPerDay by remember { mutableStateOf(4) }
    var daysCount by remember { mutableStateOf(7) }
    val restrictions = remember { mutableStateListOf<String>() }
    var isGenerating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = cardColor,
        dragHandle       = { BottomSheetDefaults.DragHandle() },
        modifier         = Modifier.imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Plan de comidas con IA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text("Genera un plan semanal según tus calorías y restricciones.", fontSize = 12.sp, color = mutedTextColor)

            OutlinedTextField(
                value         = targetKcal,
                onValueChange = { targetKcal = it.filter(Char::isDigit).take(5) },
                label         = { Text("Objetivo kcal/día", color = mutedTextColor, fontSize = 12.sp) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedBorderColor    = mutedTextColor.copy(alpha = 0.3f),
                    focusedBorderColor      = accent,
                    focusedTextColor        = textColor,
                    unfocusedTextColor      = textColor,
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StepperField(
                    label = "COMIDAS / DÍA", value = mealsPerDay,
                    onChange = { mealsPerDay = it.coerceIn(2, 6) },
                    min = 2, max = 6,
                    accent = accent, textColor = textColor, mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f),
                )
                StepperField(
                    label = "DÍAS", value = daysCount,
                    onChange = { daysCount = it.coerceIn(1, 7) },
                    min = 1, max = 7,
                    accent = accent, textColor = textColor, mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "RESTRICCIONES",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = mutedTextColor,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RESTRICTION_OPTIONS.forEach { option ->
                        val selected = option in restrictions
                        FilterChip(
                            selected = selected,
                            onClick  = {
                                if (selected) restrictions.remove(option) else restrictions.add(option)
                            },
                            label    = { Text(option, fontSize = 12.sp) },
                            shape    = RoundedCornerShape(20.dp),
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.15f),
                                selectedLabelColor     = accent,
                                labelColor             = textColor,
                            ),
                        )
                    }
                }
            }

            error?.let {
                Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val kcal = targetKcal.toIntOrNull() ?: 2000
                    isGenerating = true
                    error = null
                    scope.launch {
                        AiRepository.generateWeeklyMealPlan(
                            AiMealPlanRequest(
                                targetKcal   = kcal,
                                mealsPerDay  = mealsPerDay,
                                daysCount    = daysCount,
                                restrictions = restrictions.toList(),
                            )
                        )
                            .onSuccess {
                                isGenerating = false
                                onResult(it)
                            }
                            .onFailure {
                                isGenerating = false
                                error = it.message ?: "Error consultando la IA"
                            }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Generar plan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun StepperField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    min: Int,
    max: Int,
    accent: Color,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = mutedTextColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(mutedTextColor.copy(alpha = 0.08f))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = { onChange(value - 1) },
                enabled = value > min,
            ) { Text("−", fontSize = 18.sp, color = if (value > min) accent else mutedTextColor, fontWeight = FontWeight.Bold) }
            Text("$value", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
            TextButton(
                onClick = { onChange(value + 1) },
                enabled = value < max,
            ) { Text("+", fontSize = 18.sp, color = if (value < max) accent else mutedTextColor, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun MealPlanResultDialog(
    plan: AiMealPlanResponse,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan generado", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(plan.days.size) { dayIndex ->
                    val day = plan.days[dayIndex]
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            day.dayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        day.meals.forEach { meal ->
                            Text(
                                "${meal.slotName}: " + meal.dishes.joinToString(", ") { "${it.name} (${it.kcal} kcal)" },
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", fontWeight = FontWeight.SemiBold) }
        },
    )
}
