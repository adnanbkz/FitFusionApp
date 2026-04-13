package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.PrimaryContainer
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.viewmodel.AddWorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddWorkout(
    navController: NavHostController,
    addWorkoutViewModel: AddWorkoutViewModel = viewModel()
) {
    val state by addWorkoutViewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            TopAppBar(
                title = { Text("Añadir entrenamiento", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                "Explora el catálogo global de Firestore y elige ejercicios reales para el flujo de entrenamientos.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = addWorkoutViewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text("Buscar por nombre del ejercicio")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = addWorkoutViewModel::clearSearchQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary
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
                        onClick = { addWorkoutViewModel.onMuscleGroupSelected(muscleGroup) },
                        label = { Text(muscleGroup) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = Primary,
                            containerColor = SurfaceContainerLowest
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = SurfaceContainerLowest
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Catálogo de Firestore", fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(
                            "${state.filteredExercises.size} ejercicios cargados",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = addWorkoutViewModel::refreshExercises,
                        label = { Text("Recargar") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SurfaceContainerLow
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            state.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cargando ejercicios desde Firestore...", color = OnSurfaceVariant)
                        }
                    }
                }
            }

            errorMessage != null -> {
                item {
                    ErrorStateCard(
                        message = errorMessage,
                        onRetry = addWorkoutViewModel::refreshExercises
                    )
                }
            }

            state.filteredExercises.isEmpty() -> {
                item {
                    EmptyExerciseState(
                        hasSearchOrFilter = state.searchQuery.isNotBlank() ||
                            state.selectedMuscleGroup != "Todos"
                    )
                }
            }

            else -> {
                items(state.filteredExercises, key = { it.documentId }) { exercise ->
                    ExerciseCatalogRow(exercise = exercise)
                }
                item {
                    LoadMoreSection(
                        isLoadingMore = state.isLoadingMore,
                        hasMore = state.hasMore,
                        onLoadMore = addWorkoutViewModel::loadMoreExercises
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCatalogRow(exercise: ExerciseCatalogItem) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = SurfaceContainerLowest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        exercise.exerciseId,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        exercise.difficultyLevel ?: "Sin nivel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExerciseMetaPill(label = exercise.displayMuscleGroup)
                ExerciseMetaPill(label = exercise.displayEquipment)
                exercise.posture?.let { posture ->
                    ExerciseMetaPill(label = posture)
                }
            }

            if (exercise.shortYoutubeDemoUrl != null || exercise.inDepthYoutubeTechniqueUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Enlaces de demostración disponibles",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
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
        Text(
            label,
            fontSize = 12.sp,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun ErrorStateCard(
    message: String,
    onRetry: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = SurfaceContainerLowest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No se pudo cargar el catálogo de ejercicios", fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = OnSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = Primary)
            }
        }
    }
}

@Composable
private fun LoadMoreSection(
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoadingMore -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando más ejercicios...", color = OnSurfaceVariant)
                }
            }

            hasMore -> {
                TextButton(onClick = onLoadMore) {
                    Text("Cargar más", color = Primary)
                }
            }

            else -> {
                Text(
                    "Fin de los resultados cargados",
                    color = OnSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyExerciseState(hasSearchOrFilter: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (hasSearchOrFilter) {
                    "No hay ejercicios que coincidan con los filtros actuales"
                } else {
                    "No se encontraron ejercicios en Firestore"
                },
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasSearchOrFilter) {
                    "Prueba otra búsqueda o limpia el filtro de grupo muscular."
                } else {
                    "Comprueba que la colección global exercises está poblada."
                },
                color = OnSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}
