package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.util.EquipmentTranslations
import com.example.fitfusion.util.MuscleTranslations
import com.example.fitfusion.viewmodel.AddWorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddWorkout(
    navController: NavHostController,
    isLogMode: Boolean = false,
    addWorkoutViewModel: AddWorkoutViewModel = viewModel()
) {
    val state by addWorkoutViewModel.uiState.collectAsState()
    val activeSession by ActiveWorkoutManager.session.collectAsState()

    LaunchedEffect(isLogMode) {
        addWorkoutViewModel.setLogMode(isLogMode)
    }

    LaunchedEffect(activeSession?.id) {
        if (activeSession != null) {
            navController.navigate(Screens.ActiveWorkoutScreen.name) {
                popUpTo(Screens.AddWorkoutScreen.name) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Surface,
        floatingActionButton = {
            if (state.isLogMode) {
                if (state.selectedExercises.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick        = {
                            if (addWorkoutViewModel.startSession()) {
                                navController.navigate(Screens.ActiveWorkoutScreen.name) {
                                    popUpTo(Screens.AddWorkoutScreen.name) { inclusive = true }
                                }
                            }
                        },
                        containerColor = Primary,
                        contentColor   = Color.White,
                        icon           = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text           = {
                            Text(
                                "Iniciar · ${state.selectedExercises.size}",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
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
                            if (state.isLogMode) "Nuevo entrenamiento" else "Catálogo de ejercicios",
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
                        "Selecciona los ejercicios y pulsa iniciar; el cronómetro arranca automáticamente."
                    else
                        "Explora el catálogo global de Firestore.",
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
                            Text(
                                if (state.isRemoteSearchMode) "Resultados de Algolia" else "Catálogo de Firestore",
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                if (state.isRemoteSearchMode) {
                                    "${state.filteredExercises.size} ejercicios en esta búsqueda"
                                } else {
                                    "${state.filteredExercises.size} ejercicios cargados"
                                },
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
}

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
