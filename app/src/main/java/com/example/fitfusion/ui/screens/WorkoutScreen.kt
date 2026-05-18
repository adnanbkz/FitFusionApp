package com.example.fitfusion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ExerciseProgress
import com.example.fitfusion.viewmodel.WorkoutUiState
import com.example.fitfusion.viewmodel.WorkoutViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private data class EditableWorkoutState(
    val source: LoggedWorkout,
    val name: String = source.name,
    val durationMinutes: Int = source.durationMinutes.coerceAtLeast(1),
    val exercises: List<WorkoutExercise> = source.exercises,
) {
    fun toLoggedWorkout(): LoggedWorkout {
        val startedAt = source.startedAtMs
        val endedAt = startedAt?.let { it + durationMinutes * 60_000L } ?: source.endedAtMs
        return source.copy(
            name = name.ifBlank { "Entrenamiento" },
            durationMinutes = durationMinutes,
            kcalBurned = (durationMinutes * 6.5f).toInt(),
            endedAtMs = endedAt,
            exercises = exercises,
        )
    }
}

/**
 * Hub de entrenamiento: historial de sesiones registradas y progreso por ejercicio.
 * Se muestra en la pestaña "Ejercicio" del navbar de [PantallaHome].
 */
@Composable
fun PantallaWorkout(
    navController: NavHostController,
    workoutViewModel: WorkoutViewModel = viewModel(),
) {
    val state by workoutViewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var expandedWorkoutId by remember { mutableStateOf<String?>(null) }
    var workoutEditState by remember { mutableStateOf<EditableWorkoutState?>(null) }
    var workoutEditError by remember { mutableStateOf<String?>(null) }
    var isSavingWorkoutEdit by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Entrenamiento",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = OnSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
            )
        }

        item { WorkoutStatsRow(state, modifier = Modifier.padding(horizontal = 20.dp)) }

        item {
            Button(
                onClick = {
                    navController.navigate("${Screens.AddWorkoutScreen.name}?logMode=true")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Icon(Icons.Outlined.FitnessCenter, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Empezar entrenamiento", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        item {
            WorkoutSegmented(
                selected = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        when {
            state.isLoading -> item { WorkoutLoading() }

            selectedTab == 0 -> {
                if (state.workouts.isEmpty()) {
                    item {
                        WorkoutEmptyState(
                            title = "Aún no has registrado entrenamientos",
                            subtitle = "Empieza una sesión y aquí aparecerá tu historial.",
                        )
                    }
                } else {
                    items(state.workouts, key = { it.id }) { workout ->
                        WorkoutHistoryCard(
                            workout = workout,
                            expanded = expandedWorkoutId == workout.id,
                            onToggle = {
                                expandedWorkoutId =
                                    if (expandedWorkoutId == workout.id) null else workout.id
                            },
                            onExerciseClick = { documentId ->
                                navController.navigate("${Screens.ExerciseDetailScreen.name}/$documentId")
                            },
                            onEdit = {
                                workoutEditState = EditableWorkoutState(workout)
                                workoutEditError = null
                            },
                            onRemove = {
                                workoutViewModel.removeWorkoutFromDay(workout.id, workout.date)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }

            else -> {
                if (state.exerciseProgress.isEmpty()) {
                    item {
                        WorkoutEmptyState(
                            title = "Aún no hay progreso que mostrar",
                            subtitle = "Registra ejercicios en tus entrenamientos para ver su evolución.",
                        )
                    }
                } else {
                    items(state.exerciseProgress, key = { it.documentId ?: it.name }) { progress ->
                        ExerciseProgressCard(
                            progress = progress,
                            onClick = {
                                progress.documentId?.let {
                                    navController.navigate("${Screens.ExerciseDetailScreen.name}/$it")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }
    }

    workoutEditState?.let { editState ->
        EditWorkoutSheet(
            state = editState,
            isSaving = isSavingWorkoutEdit,
            errorMessage = workoutEditError,
            onStateChange = { workoutEditState = it; workoutEditError = null },
            onDismiss = {
                if (!isSavingWorkoutEdit) {
                    workoutEditState = null
                    workoutEditError = null
                }
            },
            onSave = {
                isSavingWorkoutEdit = true
                workoutViewModel.updateWorkout(
                    workout = editState.toLoggedWorkout(),
                    onSuccess = {
                        isSavingWorkoutEdit = false
                        workoutEditState = null
                        workoutEditError = null
                    },
                    onError = { message ->
                        isSavingWorkoutEdit = false
                        workoutEditError = message
                    },
                )
            },
        )
    }
}

@Composable
private fun WorkoutStatsRow(state: WorkoutUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        WorkoutStatCard("Entrenos", state.totalWorkouts.toString(), Modifier.weight(1f))
        WorkoutStatCard("Esta semana", state.workoutsThisWeek.toString(), Modifier.weight(1f))
        WorkoutStatCard("Volumen", "${formatVolume(state.totalVolumeKg)} kg", Modifier.weight(1f))
    }
}

@Composable
private fun WorkoutStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLowest)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Primary, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun WorkoutSegmented(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentTab("Historial", selected == 0, Modifier.weight(1f)) { onSelect(0) }
        SegmentTab("Progreso", selected == 1, Modifier.weight(1f)) { onSelect(1) }
    }
}

@Composable
private fun SegmentTab(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Color.White else OnSurfaceVariant,
        )
    }
}

@Composable
private fun WorkoutLoading() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun WorkoutEmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface, textAlign = TextAlign.Center)
        Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: LoggedWorkout,
    expanded: Boolean,
    onToggle: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .clickable(onClick = onToggle)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(workout.date.label(), fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Edit, "Editar", tint = OnSurfaceVariant, modifier = Modifier.size(15.dp))
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, "Eliminar", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = OnSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutChip("${workout.durationMinutes} min")
            WorkoutChip("${workout.exerciseCount} ejercicios")
            WorkoutChip("${workout.totalVolumeKg.toInt()} kg")
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                workout.exercises.forEach { exercise ->
                    val docId = exercise.exerciseDocumentId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (!docId.isNullOrBlank()) {
                                    Modifier.clickable { onExerciseClick(docId) }
                                } else {
                                    Modifier
                                }
                            )
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Primary),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            exercise.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(exercise.summary, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutChip(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = OnSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditWorkoutSheet(
    state: EditableWorkoutState,
    isSaving: Boolean,
    errorMessage: String?,
    onStateChange: (EditableWorkoutState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Editar entrenamiento", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(
                        "${state.source.date.dayOfMonth} ${state.source.date.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("es"))}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                    )
                }
                Text(
                    "${state.exercises.sumOf { it.sets.size }} series",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { onStateChange(state.copy(name = it)) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary,
                ),
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Duración", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        Text("Actualiza tiempo y kcal estimadas", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StepperButton(label = "-", onClick = {
                            onStateChange(state.copy(durationMinutes = (state.durationMinutes - 5).coerceAtLeast(1)))
                        })
                        Text("${state.durationMinutes} min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        StepperButton(label = "+", onClick = {
                            onStateChange(state.copy(durationMinutes = (state.durationMinutes + 5).coerceAtMost(240)))
                        })
                    }
                }
            }

            Text("EJERCICIOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.exercises.forEachIndexed { exerciseIndex, exercise ->
                    EditableExerciseCard(
                        exercise = exercise,
                        onAddSet = {
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets + (current.sets.lastOrNull()
                                                ?: WorkoutSet(reps = 10, weightKg = 0f))
                                        )
                                    }
                                )
                            )
                        },
                        onRemoveSet = { setIndex ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        if (current.sets.size <= 1 || setIndex !in current.sets.indices) {
                                            current
                                        } else {
                                            current.copy(sets = current.sets.filterIndexed { i, _ -> i != setIndex })
                                        }
                                    }
                                )
                            )
                        },
                        onSetRepsChange = { setIndex, reps ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets.updateAt(setIndex) { set ->
                                                set.copy(reps = reps.coerceIn(1, 50))
                                            }
                                        )
                                    }
                                )
                            )
                        },
                        onSetWeightChange = { setIndex, weight ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets.updateAt(setIndex) { set ->
                                                set.copy(weightKg = weight.coerceIn(0, 300).toFloat())
                                            }
                                        )
                                    }
                                )
                            )
                        },
                    )
                }
            }

            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EditableExerciseCard(
    exercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onSetRepsChange: (Int, Int) -> Unit,
    onSetWeightChange: (Int, Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(exercise.muscleGroup, fontSize = 12.sp, color = OnSurfaceVariant)
                }
                AssistChip(
                    onClick = onAddSet,
                    label = { Text("Añadir serie") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerHigh),
                )
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                exercise.sets.forEachIndexed { setIndex, set ->
                    EditableSetRow(
                        setIndex = setIndex,
                        set = set,
                        canRemove = exercise.sets.size > 1,
                        onRemove = { onRemoveSet(setIndex) },
                        onRepsChange = { reps -> onSetRepsChange(setIndex, reps) },
                        onWeightChange = { weight -> onSetWeightChange(setIndex, weight) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableSetRow(
    setIndex: Int,
    set: WorkoutSet,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Serie ${setIndex + 1}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                if (canRemove) {
                    TextButton(onClick = onRemove) { Text("Eliminar", color = Primary) }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                EditableStepper(
                    label = "Reps",
                    value = set.reps,
                    onDecrement = { onRepsChange((set.reps - 1).coerceAtLeast(1)) },
                    onIncrement = { onRepsChange((set.reps + 1).coerceAtMost(50)) },
                )
                EditableStepper(
                    label = "Peso (kg)",
                    value = set.weightKg.toInt(),
                    onDecrement = { onWeightChange((set.weightKg.toInt() - 5).coerceAtLeast(0)) },
                    onIncrement = { onWeightChange((set.weightKg.toInt() + 5).coerceAtMost(300)) },
                )
            }
        }
    }
}

@Composable
private fun EditableStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StepperButton(label = "-", onClick = onDecrement)
            Text("$value", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.widthIn(min = 28.dp))
            StepperButton(label = "+", onClick = onIncrement)
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(SurfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 18.sp, color = OnSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExerciseProgressCard(
    progress: ExerciseProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClickable = progress.documentId != null
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    progress.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${progress.sessionsCount} ${if (progress.sessionsCount == 1) "sesión" else "sesiones"}",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                )
            }
            val delta = progress.latestVolume - progress.firstVolume
            if (progress.points.size >= 2 && delta != 0f) {
                val improved = delta > 0f
                Text(
                    (if (improved) "▲ " else "▼ ") + "${abs(delta).toInt()} kg",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (improved) Primary else Tertiary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (progress.points.size >= 2) {
            VolumeSparkline(
                values = progress.points.map { it.totalVolume },
                lineColor = Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            )
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ProgressMetric("Mejor serie", "${progress.bestSetVolume.toInt()} kg")
            ProgressMetric("Último volumen", "${progress.latestVolume.toInt()} kg")
        }
    }
}

@Composable
private fun ProgressMetric(label: String, value: String) {
    Column {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun VolumeSparkline(
    values: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (values.size < 2) return
    val minValue = values.min()
    val maxValue = values.max()
    val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range) * size.height
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(x, y))
        }
    }
}

private val workoutDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-ES"))

private fun LocalDate.label(): String =
    format(workoutDateFormatter).replaceFirstChar { it.uppercase() }

private fun formatVolume(kg: Float): String = when {
    kg >= 1000f -> "%.1fk".format(kg / 1000f)
    else -> kg.toInt().toString()
}

private fun <T> List<T>.updateAt(index: Int, transform: (T) -> T): List<T> {
    if (index !in indices) return this
    return mapIndexed { i, item -> if (i == index) transform(item) else item }
}
