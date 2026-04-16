package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.util.EquipmentTranslations
import com.example.fitfusion.util.MuscleTranslations
import com.example.fitfusion.viewmodel.AddWorkoutViewModel
import com.example.fitfusion.viewmodel.ExerciseConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddWorkout(
    navController: NavHostController,
    isLogMode: Boolean = false,
    addWorkoutViewModel: AddWorkoutViewModel = viewModel()
) {
    val state by addWorkoutViewModel.uiState.collectAsState()

    LaunchedEffect(isLogMode) {
        addWorkoutViewModel.setLogMode(isLogMode)
    }

    Scaffold(
        containerColor = Surface,
        floatingActionButton = {
            if (state.isLogMode) {
                if (state.selectedExercises.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick            = addWorkoutViewModel::showSessionSheet,
                        containerColor     = Primary,
                        contentColor       = Color.White,
                        icon               = { Icon(Icons.Default.Check, contentDescription = null) },
                        text               = { Text("Ver sesión · ${state.selectedExercises.size}", fontWeight = FontWeight.SemiBold) }
                    )
                }
            } else {
                FloatingActionButton(
                    onClick        = { navController.navigate(Screens.CreateExerciseScreen.name) },
                    containerColor = Primary,
                    contentColor   = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear ejercicio")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                TopAppBar(
                    title = {
                        Text(
                            if (state.isLogMode) "Registrar entrenamiento" else "Añadir entrenamiento",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            }

            item {
                Text(
                    if (state.isLogMode)
                        "Selecciona los ejercicios de tu sesión de hoy."
                    else
                        "Explora el catálogo global de Firestore y elige ejercicios reales para el flujo de entrenamientos.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = addWorkoutViewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Buscar por nombre del ejercicio") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = addWorkoutViewModel::clearSearchQuery) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    shape  = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedContainerColor   = SurfaceContainerLowest,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableMuscleGroups) { muscleGroup ->
                        FilterChip(
                            selected = muscleGroup == state.selectedMuscleGroup,
                            onClick  = { addWorkoutViewModel.onMuscleGroupSelected(muscleGroup) },
                            label    = { Text(MuscleTranslations.translate(muscleGroup)) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor     = Primary,
                                containerColor         = SurfaceContainerLowest
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors   = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Catálogo de Firestore", fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                "${state.filteredExercises.size} ejercicios cargados",
                                fontSize = 13.sp, color = OnSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = addWorkoutViewModel::refreshExercises,
                            label   = { Text("Recargar") },
                            colors  = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerLow)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when {
                state.isLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cargando ejercicios desde Firestore...", color = OnSurfaceVariant)
                        }
                    }
                }

                state.errorMessage != null -> item {
                    ErrorStateCard(state.errorMessage!!, onRetry = addWorkoutViewModel::refreshExercises)
                }

                state.filteredExercises.isEmpty() -> item {
                    EmptyExerciseState(
                        hasSearchOrFilter = state.searchQuery.isNotBlank() ||
                            state.selectedMuscleGroup != "Todos"
                    )
                }

                else -> {
                    items(state.filteredExercises, key = { it.documentId }) { exercise ->
                        val isSelected = state.selectedExercises.any { it.documentId == exercise.documentId }
                        ExerciseCatalogRow(
                            exercise   = exercise,
                            isLogMode  = state.isLogMode,
                            isSelected = isSelected,
                            onClick    = {
                                if (state.isLogMode) {
                                    addWorkoutViewModel.toggleExercise(exercise)
                                } else {
                                    navController.navigate(
                                        "${Screens.ExerciseDetailScreen.name}/${exercise.documentId}"
                                    )
                                }
                            }
                        )
                    }
                    item {
                        LoadMoreSection(
                            isLoadingMore = state.isLoadingMore,
                            hasMore       = state.hasMore,
                            onLoadMore    = addWorkoutViewModel::loadMoreExercises
                        )
                    }
                }
            }
        }
    }

    // ── BottomSheet de sesión ─────────────────────────────────────────────────
    if (state.showSessionSheet) {
        LogWorkoutSheet(
            state      = state,
            onDismiss  = addWorkoutViewModel::dismissSessionSheet,
            onNameChange    = addWorkoutViewModel::updateSessionName,
            onIncrDuration  = addWorkoutViewModel::incrementDuration,
            onDecrDuration  = addWorkoutViewModel::decrementDuration,
            onConfigChange  = addWorkoutViewModel::updateExerciseConfig,
            onSave     = {
                addWorkoutViewModel.saveSession {
                    navController.popBackStack()
                }
            }
        )
    }
}

// ── BottomSheet de configuración de sesión ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogWorkoutSheet(
    state: com.example.fitfusion.viewmodel.AddWorkoutUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onIncrDuration: () -> Unit,
    onDecrDuration: () -> Unit,
    onConfigChange: (String, ExerciseConfig) -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = SurfaceContainerLowest,
        dragHandle        = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Tu sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)

            // Nombre del entrenamiento
            OutlinedTextField(
                value         = state.sessionName,
                onValueChange = onNameChange,
                label         = { Text("Nombre del entrenamiento") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor   = SurfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Primary,
                )
            )

            // Duración + estimación kcal
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Duración", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        Text("~${state.kcalEstimate} kcal estimadas", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StepperButton(label = "−", onClick = onDecrDuration)
                        Text(
                            "${state.sessionDurationMinutes} min",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface
                        )
                        StepperButton(label = "+", onClick = onIncrDuration)
                    }
                }
            }

            // Ejercicios
            if (state.selectedExercises.isNotEmpty()) {
                Text(
                    "EJERCICIOS (${state.selectedExercises.size})",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.selectedExercises.forEach { exercise ->
                        val config = state.exerciseConfigs[exercise.documentId] ?: ExerciseConfig()
                        ExerciseConfigCard(
                            exerciseName = exercise.name,
                            muscleLabel  = MuscleTranslations.translate(exercise.displayMuscleGroup),
                            config       = config,
                            onConfigChange = { newConfig ->
                                onConfigChange(exercise.documentId, newConfig)
                            }
                        )
                    }
                }
            }

            // Botón guardar
            Button(
                onClick  = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "Guardar entrenamiento",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ExerciseConfigCard(
    exerciseName: String,
    muscleLabel: String,
    config: ExerciseConfig,
    onConfigChange: (ExerciseConfig) -> Unit,
) {
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exerciseName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(muscleLabel, fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ConfigStepper(
                    label = "Series",
                    value = config.sets,
                    onDecrement = { onConfigChange(config.copy(sets = (config.sets - 1).coerceAtLeast(1))) },
                    onIncrement = { onConfigChange(config.copy(sets = (config.sets + 1).coerceAtMost(10))) }
                )
                ConfigStepper(
                    label = "Reps",
                    value = config.reps,
                    onDecrement = { onConfigChange(config.copy(reps = (config.reps - 1).coerceAtLeast(1))) },
                    onIncrement = { onConfigChange(config.copy(reps = (config.reps + 1).coerceAtMost(50))) }
                )
                ConfigStepper(
                    label = "Peso (kg)",
                    value = config.weightKg,
                    onDecrement = { onConfigChange(config.copy(weightKg = (config.weightKg - 5).coerceAtLeast(0))) },
                    onIncrement = { onConfigChange(config.copy(weightKg = (config.weightKg + 5).coerceAtMost(300))) }
                )
            }
        }
    }
}

@Composable
private fun ConfigStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepperButton(label = "−", onClick = onDecrement)
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
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, color = OnSurface, fontWeight = FontWeight.Bold)
    }
}

// ── Fila de ejercicio del catálogo ────────────────────────────────────────────

@Composable
private fun ExerciseCatalogRow(
    exercise: ExerciseCatalogItem,
    isLogMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.06f) else SurfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exercise.exerciseId, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    if (!isLogMode) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Primary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                exercise.difficultyLevel ?: "Sin nivel",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseMetaPill(label = MuscleTranslations.translate(exercise.displayMuscleGroup))
                    ExerciseMetaPill(label = EquipmentTranslations.translate(exercise.displayEquipment))
                    exercise.posture?.let { ExerciseMetaPill(label = it) }
                }
            }

            // Indicador de selección (solo en log mode)
            if (isLogMode) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Primary else SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetaPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun ErrorStateCard(message: String, onRetry: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors   = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No se pudo cargar el catálogo de ejercicios", fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = OnSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("Reintentar", color = Primary) }
        }
    }
}

@Composable
private fun LoadMoreSection(isLoadingMore: Boolean, hasMore: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoadingMore -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cargando más ejercicios...", color = OnSurfaceVariant)
            }
            hasMore -> TextButton(onClick = onLoadMore) { Text("Cargar más", color = Primary) }
            else    -> Text("Fin de los resultados cargados", color = OnSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyExerciseState(hasSearchOrFilter: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (hasSearchOrFilter) "No hay ejercicios que coincidan con los filtros actuales"
                else "No se encontraron ejercicios en Firestore",
                fontWeight = FontWeight.Bold, color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasSearchOrFilter) "Prueba otra búsqueda o limpia el filtro de grupo muscular."
                else "Comprueba que la colección global exercises está poblada.",
                color = OnSurfaceVariant, fontSize = 14.sp
            )
        }
    }
}
