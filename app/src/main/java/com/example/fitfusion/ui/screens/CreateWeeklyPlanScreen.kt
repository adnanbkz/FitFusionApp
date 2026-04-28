package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.CreateWeeklyPlanViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCreateWeeklyPlan(
    navController: NavHostController,
    viewModel: CreateWeeklyPlanViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Nuevo plan semanal", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWeeklyPlan { navController.popBackStack() } },
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
            item {
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
                                placeholder   = { Text("Nombre del plan *", color = OnSurfaceVariant, fontSize = 15.sp) },
                                singleLine    = true,
                                modifier      = Modifier.weight(1f),
                                shape         = RoundedCornerShape(12.dp),
                                colors        = fieldColors(),
                            )
                        }
                    }
                }
            }

            item {
                SectionLabel("DÍAS DE LA SEMANA")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Asigna una rutina a cada día o déjalo como descanso",
                    fontSize = 12.sp, color = OnSurfaceVariant,
                )
            }

            items(DayOfWeek.entries.toList()) { day ->
                DayRow(
                    day         = day,
                    routineId   = state.days[day],
                    routineName = state.myRoutines.find { it.id == state.days[day] }?.name,
                    emoji       = state.myRoutines.find { it.id == state.days[day] }?.emoji,
                    onClick     = { viewModel.openDaySelector(day) },
                )
            }

            item { PublishRow(isPublic = state.isPublic, onToggle = viewModel::onPublicToggle) }

            if (state.saveError != null) {
                item {
                    Text(state.saveError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }

    state.selectingForDay?.let { day ->
        RoutinePickerSheet(
            day           = day,
            myRoutines    = state.myRoutines,
            isLoading     = state.isLoadingRoutines,
            onDismiss     = viewModel::closeDaySelector,
            onRest        = { viewModel.assignRoutineToDay(day, null) },
            onSelect      = { routine -> viewModel.assignRoutineToDay(day, routine.id) },
        )
    }
}

@Composable
private fun DayRow(
    day: DayOfWeek,
    routineId: String?,
    routineName: String?,
    emoji: String?,
    onClick: () -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(
                    if (routineId != null) Primary.copy(alpha = 0.15f) else SurfaceContainerHigh
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    day.getDisplayName(TextStyle.SHORT, Locale("es")).take(3).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = if (routineId != null) Primary else OnSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    day.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = OnSurface,
                )
                if (routineName != null) {
                    Text(
                        "${emoji ?: "💪"}  $routineName",
                        fontSize = 13.sp,
                        color    = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text("Descanso", fontSize = 13.sp, color = OnSurfaceVariant)
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutinePickerSheet(
    day: DayOfWeek,
    myRoutines: List<Routine>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRest: () -> Unit,
    onSelect: (Routine) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                day.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface,
            )
            RestRow(onClick = onRest)
            when {
                isLoading -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp)) }
                myRoutines.isEmpty() -> Text(
                    "Aún no tienes rutinas. Crea una primero.",
                    fontSize = 13.sp, color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                )
                else -> myRoutines.forEach { routine -> RoutineOptionRow(routine = routine, onClick = { onSelect(routine) }) }
            }
        }
    }
}

@Composable
private fun RestRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) { Text("🛌", fontSize = 20.sp) }
        Text("Descanso", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
    }
}

@Composable
private fun RoutineOptionRow(routine: Routine, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) { Text(routine.emoji, fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(routine.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${routine.exercises.size} ejercicio${if (routine.exercises.size != 1) "s" else ""}" +
                    (routine.estimatedDurationMin?.let { " · $it min" } ?: ""),
                fontSize = 12.sp, color = OnSurfaceVariant,
            )
        }
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
                Text("Otros usuarios podrán verlo y guardarlo", fontSize = 12.sp, color = OnSurfaceVariant)
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
