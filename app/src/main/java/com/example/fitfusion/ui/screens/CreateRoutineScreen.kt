package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.RoutineExercise
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.CreateRoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCreateRoutine(
    navController: NavHostController,
    viewModel: CreateRoutineViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Nueva rutina", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveRoutine { navController.popBackStack() } },
                        enabled = state.isValid && !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = Primary,
                            )
                        } else {
                            Text(
                                "Guardar",
                                color      = if (state.isValid) Primary else OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeaderCard(state = state, viewModel = viewModel) }
            item { DetailsSection(state = state, viewModel = viewModel) }
            item { SearchSection(state = state, viewModel = viewModel) }

            if (state.searchQuery.isNotBlank()) {
                when {
                    state.isLoadingResults -> item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp)) }
                    }
                    state.searchResults.isEmpty() -> item {
                        Text(
                            "Sin resultados",
                            color    = OnSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    else -> items(state.searchResults, key = { it.documentId }) { item ->
                        ExerciseSearchRow(item = item, onAdd = { viewModel.addExercise(item) })
                    }
                }
            }

            if (state.exercises.isNotEmpty()) {
                item {
                    Text(
                        "${state.exercises.size} EJERCICIO${if (state.exercises.size != 1) "S" else ""}",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color         = OnSurfaceVariant,
                        modifier      = Modifier.padding(top = 4.dp),
                    )
                }
                itemsIndexed(state.exercises, key = { _, e -> e.exerciseId }) { index, ex ->
                    AddedExerciseRow(
                        exercise = ex,
                        onEdit   = { viewModel.openEditExercise(index) },
                        onRemove = { viewModel.removeExercise(index) },
                    )
                }
            }

            item { PublishRow(isPublic = state.isPublic, onToggle = viewModel::onPublicToggle) }

            if (state.saveError != null) {
                item {
                    Text(
                        state.saveError!!,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    state.editingExerciseIndex?.let { idx ->
        val ex = state.exercises.getOrNull(idx) ?: return@let
        ExerciseEditSheet(
            exercise  = ex,
            onDismiss = viewModel::closeEditExercise,
            onUpdate  = { sets, reps, weight, rest, notes ->
                viewModel.updateExerciseField(idx, sets = sets, reps = reps, weight = weight, rest = rest, notes = notes)
            },
            onDone    = viewModel::closeEditExercise,
        )
    }
}

@Composable
private fun HeaderCard(state: com.example.fitfusion.viewmodel.CreateRoutineUiState, viewModel: CreateRoutineViewModel) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("INFORMACIÓN")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value         = state.emoji,
                    onValueChange = viewModel::onEmojiChange,
                    modifier      = Modifier.width(72.dp),
                    textStyle     = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = fieldColors(),
                )
                OutlinedTextField(
                    value         = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder   = { Text("Nombre de la rutina *", color = OnSurfaceVariant, fontSize = 15.sp) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = fieldColors(),
                )
            }
            OutlinedTextField(
                value         = state.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder   = { Text("Descripción breve (opcional)", color = OnSurfaceVariant, fontSize = 14.sp) },
                minLines      = 2,
                maxLines      = 3,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = fieldColors(),
            )
        }
    }
}

@Composable
private fun DetailsSection(state: com.example.fitfusion.viewmodel.CreateRoutineUiState, viewModel: CreateRoutineViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("DETALLES")
        OutlinedTextField(
            value           = state.duration,
            onValueChange   = viewModel::onDurationChange,
            placeholder     = { Text("Duración estimada (min)", color = OnSurfaceVariant, fontSize = 13.sp) },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            colors          = fieldColors(),
        )
    }
}

@Composable
private fun SearchSection(state: com.example.fitfusion.viewmodel.CreateRoutineUiState, viewModel: CreateRoutineViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("AÑADIR EJERCICIOS")
        OutlinedTextField(
            value         = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder   = { Text("Busca del catálogo...", color = OnSurfaceVariant, fontSize = 14.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            trailingIcon  = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(onClick = viewModel::clearSearch) {
                        Icon(Icons.Default.Clear, "Limpiar", tint = OnSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape      = RoundedCornerShape(14.dp),
            colors     = fieldColors(),
            modifier   = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ExerciseSearchRow(item: ExerciseCatalogItem, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAdd).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) { Text("💪", fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            val group = item.muscleGroup ?: item.primeMoverMuscle ?: "Ejercicio"
            Text(group, fontSize = 12.sp, color = OnSurfaceVariant)
        }
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Primary),
            contentAlignment = Alignment.Center,
        ) { Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun AddedExerciseRow(
    exercise: RoutineExercise,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Text(exercise.emoji, fontSize = 20.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exerciseName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val weightStr = if (exercise.targetWeightKg > 0f) " · ${exercise.targetWeightKg.toInt()} kg" else ""
                Text(
                    "${exercise.targetSets}×${exercise.targetReps}$weightStr · ${exercise.restSeconds}s descanso",
                    fontSize = 12.sp,
                    color    = OnSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(SurfaceContainerHigh).clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) { Text("×", fontSize = 16.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseEditSheet(
    exercise: RoutineExercise,
    onDismiss: () -> Unit,
    onUpdate: (Int?, Int?, Float?, Int?, String?) -> Unit,
    onDone: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var setsStr   by remember(exercise.exerciseId) { mutableStateOf(exercise.targetSets.toString()) }
    var repsStr   by remember(exercise.exerciseId) { mutableStateOf(exercise.targetReps.toString()) }
    var weightStr by remember(exercise.exerciseId) { mutableStateOf(exercise.targetWeightKg.takeIf { it > 0f }?.toInt()?.toString() ?: "") }
    var restStr   by remember(exercise.exerciseId) { mutableStateOf(exercise.restSeconds.toString()) }
    var notesStr  by remember(exercise.exerciseId) { mutableStateOf(exercise.notes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(exercise.exerciseName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
            Text("Configura series, reps y peso objetivo", fontSize = 13.sp, color = OnSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField(
                    label = "SERIES",
                    value = setsStr,
                    onChange = {
                        setsStr = it.filter(Char::isDigit).take(3)
                        setsStr.toIntOrNull()?.let { v -> onUpdate(v, null, null, null, null) }
                    },
                    modifier = Modifier.weight(1f),
                )
                NumField(
                    label = "REPS",
                    value = repsStr,
                    onChange = {
                        repsStr = it.filter(Char::isDigit).take(3)
                        repsStr.toIntOrNull()?.let { v -> onUpdate(null, v, null, null, null) }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField(
                    label = "PESO (kg)",
                    value = weightStr,
                    onChange = {
                        weightStr = it.filter(Char::isDigit).take(4)
                        onUpdate(null, null, (weightStr.toIntOrNull() ?: 0).toFloat(), null, null)
                    },
                    modifier = Modifier.weight(1f),
                )
                NumField(
                    label = "DESCANSO (s)",
                    value = restStr,
                    onChange = {
                        restStr = it.filter(Char::isDigit).take(4)
                        restStr.toIntOrNull()?.let { v -> onUpdate(null, null, null, v, null) }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value         = notesStr,
                onValueChange = {
                    notesStr = it
                    onUpdate(null, null, null, null, it)
                },
                placeholder   = { Text("Notas (opcional)", color = OnSurfaceVariant, fontSize = 13.sp) },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = fieldColors(),
            )

            Button(
                onClick  = onDone,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
            ) { Text("Hecho", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun NumField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
        OutlinedTextField(
            value           = value,
            onValueChange   = onChange,
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape           = RoundedCornerShape(10.dp),
            colors          = fieldColors(),
            modifier        = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PublishRow(isPublic: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Publicar en la comunidad", fontWeight = FontWeight.SemiBold, color = OnSurface, fontSize = 14.sp)
                Text("Otros usuarios podrán verla y guardarla", fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Switch(
                checked         = isPublic,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = SurfaceContainerLow,
    focusedContainerColor   = SurfaceContainerLow,
    unfocusedBorderColor    = Color.Transparent,
    focusedBorderColor      = Primary,
)

