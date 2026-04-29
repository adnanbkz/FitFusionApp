package com.example.fitfusion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.workout.ActiveExerciseEntry
import com.example.fitfusion.data.workout.ActiveSetEntry
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ActiveWorkoutViewModel
import com.example.fitfusion.viewmodel.formatElapsed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActiveWorkout(
    navController: NavHostController,
    activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(),
) {
    val session by activeWorkoutViewModel.session.collectAsState()
    val elapsed by activeWorkoutViewModel.elapsedSeconds.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (session == null) navController.popBackStack()
    }

    BackHandler { showCancelDialog = true }

    val current = session ?: return

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            current.name.ifBlank { "Entrenamiento" },
                            fontSize = 17.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                        )
                        Text(
                            "${current.exerciseCount} ejercicios · ${current.totalSets} series",
                            fontSize = 12.sp, color = OnSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = { showRenameDialog = true }) {
                        Text("Renombrar", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = {
                    navController.navigate(Screens.WorkoutFinishScreen.name)
                },
                containerColor = Primary,
                contentColor   = Color.White,
                icon           = { Icon(Icons.Default.Check, contentDescription = null) },
                text           = { Text("Finalizar", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TimerCard(
                    elapsedSeconds = elapsed,
                    isPaused       = current.isPaused,
                    onPauseToggle  = {
                        if (current.isPaused) activeWorkoutViewModel.resume()
                        else activeWorkoutViewModel.pause()
                    },
                )
            }

            items(current.exercises, key = { it.exerciseDocumentId }) { exercise ->
                ExerciseSetEditor(
                    exercise          = exercise,
                    onAddSet          = { activeWorkoutViewModel.addSet(exercise.exerciseDocumentId) },
                    onRemoveSet       = { idx -> activeWorkoutViewModel.removeSet(exercise.exerciseDocumentId, idx) },
                    onRepsChange      = { idx, v ->
                        activeWorkoutViewModel.updateSetReps(exercise.exerciseDocumentId, idx, v)
                    },
                    onWeightChange    = { idx, v ->
                        activeWorkoutViewModel.updateSetWeight(exercise.exerciseDocumentId, idx, v)
                    },
                    onToggleCompleted = { idx ->
                        activeWorkoutViewModel.toggleSetCompleted(exercise.exerciseDocumentId, idx)
                    },
                    onRemoveExercise  = { activeWorkoutViewModel.removeExercise(exercise.exerciseDocumentId) },
                )
            }

            item {
                OutlinedButton(
                    onClick  = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Descartar entrenamiento", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title   = { Text("¿Descartar entrenamiento?") },
            text    = { Text("Perderás los ejercicios y el tiempo registrado de esta sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    activeWorkoutViewModel.cancel()
                }) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Seguir") }
            },
            containerColor = SurfaceContainerLowest,
        )
    }

    if (showRenameDialog) {
        var draftName by remember(current.name) { mutableStateOf(current.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Renombrar sesión") },
            text  = {
                OutlinedTextField(
                    value         = draftName,
                    onValueChange = { draftName = it },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    activeWorkoutViewModel.renameSession(draftName.trim().ifBlank { "Entrenamiento" })
                    showRenameDialog = false
                }) {
                    Text("Guardar", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancelar") }
            },
            containerColor = SurfaceContainerLowest,
        )
    }
}

@Composable
private fun TimerCard(
    elapsedSeconds: Long,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
) {
    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Timer, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(
                        if (isPaused) "EN PAUSA" else "TIEMPO ACTIVO",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary,
                    )
                }
                Text(
                    formatElapsed(elapsedSeconds),
                    fontSize = 38.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            FilledIconButton(
                onClick    = onPauseToggle,
                shape      = CircleShape,
                colors     = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPaused) Primary else SurfaceContainerHigh,
                    contentColor   = if (isPaused) Color.White else OnSurface,
                ),
                modifier   = Modifier.size(54.dp),
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Reanudar" else "Pausar",
                )
            }
        }
    }
}

@Composable
private fun ExerciseSetEditor(
    exercise: ActiveExerciseEntry,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onRepsChange: (Int, Int) -> Unit,
    onWeightChange: (Int, Int) -> Unit,
    onToggleCompleted: (Int) -> Unit,
    onRemoveExercise: () -> Unit,
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(exercise.muscleGroup, fontSize = 12.sp, color = OnSurfaceVariant)
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Default.Close, contentDescription = "Quitar ejercicio", tint = OnSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("SERIE", modifier = Modifier.width(40.dp),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
                Text("REPS", modifier = Modifier.weight(1f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
                Text("KG", modifier = Modifier.weight(1f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
                Spacer(Modifier.width(80.dp))
            }
            exercise.sets.forEachIndexed { index, set ->
                SetRow(
                    index           = index,
                    set             = set,
                    canRemove       = exercise.sets.size > 1,
                    onRepsChange    = { onRepsChange(index, it) },
                    onWeightChange  = { onWeightChange(index, it) },
                    onToggleDone    = { onToggleCompleted(index) },
                    onRemove        = { onRemoveSet(index) },
                )
            }
            TextButton(
                onClick  = onAddSet,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(Modifier.width(4.dp))
                Text("Añadir serie", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SetRow(
    index: Int,
    set: ActiveSetEntry,
    canRemove: Boolean,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Int) -> Unit,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${index + 1}",
            modifier = Modifier.width(40.dp),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = if (set.completed) Primary else OnSurface,
        )
        SetStepper(
            value = set.reps,
            onChange = onRepsChange,
            min = 1, max = 50,
            modifier = Modifier.weight(1f),
        )
        SetStepper(
            value = set.weightKg,
            onChange = onWeightChange,
            min = 0, max = 300, step = 5,
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = onToggleDone,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (set.completed) Primary else SurfaceContainerHigh,
                contentColor   = if (set.completed) Color.White else OnSurface,
            ),
            modifier = Modifier.size(36.dp),
        ) {
            Icon(Icons.Default.Check, contentDescription = "Completar", modifier = Modifier.size(18.dp))
        }
        if (canRemove) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Eliminar serie", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        } else {
            Spacer(Modifier.width(36.dp))
        }
    }
}

@Composable
private fun SetStepper(
    value: Int,
    onChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int = 1,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onChange((value - step).coerceAtLeast(min)) },
            enabled = value > min,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        Text(
            "$value",
            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface,
        )
        IconButton(
            onClick = { onChange((value + step).coerceAtMost(max)) },
            enabled = value < max,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}
