package com.example.fitfusion.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.ScheduledRoutine
import com.example.fitfusion.data.models.WeeklyRoutinePlan
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.PlannerTab
import com.example.fitfusion.viewmodel.PlannerViewModel
import com.example.fitfusion.viewmodel.RoutineSource
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPlanner(
    navController: NavHostController,
    viewModel: PlannerViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Planificador", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
        floatingActionButton = {
            when (state.activeTab) {
                PlannerTab.RUTINAS -> if (state.routineSource == RoutineSource.MINE) {
                    FloatingActionButton(
                        onClick        = { navController.navigate(Screens.CreateRoutineScreen.name) },
                        containerColor = Primary,
                        contentColor   = Color.White,
                    ) { Icon(Icons.Default.Add, "Nueva rutina") }
                }
                PlannerTab.PLANES -> if (state.planSource == RoutineSource.MINE) {
                    FloatingActionButton(
                        onClick        = { navController.navigate(Screens.CreateWeeklyPlanScreen.name) },
                        containerColor = Primary,
                        contentColor   = Color.White,
                    ) { Icon(Icons.Default.Add, "Nuevo plan") }
                }
                else -> Unit
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PlannerTabRow(active = state.activeTab, onSelect = viewModel::setActiveTab)

            when (state.activeTab) {
                PlannerTab.ESTA_SEMANA -> EstaSemanaTab(state = state, viewModel = viewModel)
                PlannerTab.RUTINAS     -> RutinasTab(state = state, viewModel = viewModel)
                PlannerTab.PLANES      -> PlanesTab(state = state, viewModel = viewModel)
            }
        }
    }

    state.selectedRoutine?.let { routine ->
        RoutineDetailSheet(routine = routine, onDismiss = viewModel::dismissRoutineDetail)
    }
    state.selectedPlan?.let { plan ->
        PlanDetailSheet(plan = plan, onDismiss = viewModel::dismissPlanDetail)
    }
    state.dayPickerDate?.let { date ->
        DayRoutinePickerSheet(
            date         = date,
            routines     = state.myRoutines,
            isLoading    = state.isLoadingMyRoutines,
            onDismiss    = viewModel::closeDayPicker,
            onSelect     = { viewModel.assignRoutineToDate(date, it) },
            onRest       = { viewModel.assignRoutineToDate(date, null) },
        )
    }
    state.applyPlanId?.let { planId ->
        val plan = state.myPlans.find { it.id == planId } ?: state.communityPlans.find { it.id == planId }
        if (plan != null) {
            ApplyPlanConfirmSheet(
                plan      = plan,
                weekStart = state.weekStart,
                onDismiss = viewModel::closeApplyPlan,
                onApply   = { viewModel.applyPlanToWeek(plan, state.weekStart) },
            )
        }
    }
}

@Composable
private fun PlannerTabRow(active: PlannerTab, onSelect: (PlannerTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(
            PlannerTab.ESTA_SEMANA to "Esta semana",
            PlannerTab.RUTINAS     to "Rutinas",
            PlannerTab.PLANES      to "Planes",
        ).forEach { (tab, label) ->
            val isSelected = active == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) SurfaceContainerLowest else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) OnSurface else OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EstaSemanaTab(state: com.example.fitfusion.viewmodel.PlannerUiState, viewModel: PlannerViewModel) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Semana del ${state.weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))}",
                fontSize = 13.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium,
            )
        }
        items(DayOfWeek.entries.toList()) { day ->
            val date = state.weekStart.plusDays((day.value - 1).toLong())
            val scheduled = state.schedule[date]
            ScheduleDayRow(
                date      = date,
                scheduled = scheduled,
                isToday   = date == LocalDate.now(),
                onClick   = { viewModel.openDayPicker(date) },
            )
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun ScheduleDayRow(
    date: LocalDate,
    scheduled: ScheduledRoutine?,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val day = date.dayOfWeek
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isToday) Primary.copy(alpha = 0.08f) else SurfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(
                    if (scheduled != null) Primary.copy(alpha = 0.15f) else SurfaceContainerHigh
                ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        day.getDisplayName(TextStyle.SHORT, Locale("es")).take(3).uppercase(),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (scheduled != null) Primary else OnSurfaceVariant,
                    )
                    Text(
                        "${date.dayOfMonth}",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (scheduled != null) Primary else OnSurface,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    day.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface,
                )
                if (scheduled != null) {
                    Text(
                        "${scheduled.emoji}  ${scheduled.routineName}",
                        fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text("Descanso — toca para asignar", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RutinasTab(state: com.example.fitfusion.viewmodel.PlannerUiState, viewModel: PlannerViewModel) {
    Column {
        SourceTabRow(
            active = state.routineSource,
            onSelect = viewModel::setRoutineSource,
            mineLabel = "Mis rutinas",
        )
        val routines = if (state.routineSource == RoutineSource.MINE) state.myRoutines else state.communityRoutines
        val isLoading = if (state.routineSource == RoutineSource.MINE) state.isLoadingMyRoutines else state.isLoadingCommunityRoutines

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                isLoading && routines.isEmpty() -> item { LoadingBox() }
                routines.isEmpty() -> item {
                    EmptyMessage(
                        title = if (state.routineSource == RoutineSource.MINE) "Aún no tienes rutinas" else "Sin rutinas de la comunidad",
                        subtitle = if (state.routineSource == RoutineSource.MINE) "Pulsa + para crear tu primera rutina"
                                   else "Sé el primero en publicar una",
                    )
                }
                else -> {
                    items(routines, key = { it.id }) { routine ->
                        RoutineCard(
                            routine   = routine,
                            isMine    = state.routineSource == RoutineSource.MINE,
                            isSaving  = state.savingCommunityItemId == routine.id,
                            onTap     = { viewModel.openRoutineDetail(routine) },
                            onSave    = { viewModel.saveCommunityRoutineToMine(routine) },
                            onDelete  = { viewModel.deleteRoutine(routine.id) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PlanesTab(state: com.example.fitfusion.viewmodel.PlannerUiState, viewModel: PlannerViewModel) {
    Column {
        SourceTabRow(
            active = state.planSource,
            onSelect = viewModel::setPlanSource,
            mineLabel = "Mis planes",
        )
        val plans = if (state.planSource == RoutineSource.MINE) state.myPlans else state.communityPlans
        val isLoading = if (state.planSource == RoutineSource.MINE) state.isLoadingMyPlans else state.isLoadingCommunityPlans

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                isLoading && plans.isEmpty() -> item { LoadingBox() }
                plans.isEmpty() -> item {
                    EmptyMessage(
                        title = if (state.planSource == RoutineSource.MINE) "Aún no tienes planes" else "Sin planes de la comunidad",
                        subtitle = if (state.planSource == RoutineSource.MINE) "Pulsa + para crear tu primer plan"
                                   else "Sé el primero en publicar uno",
                    )
                }
                else -> {
                    items(plans, key = { it.id }) { plan ->
                        PlanCard(
                            plan     = plan,
                            isMine   = state.planSource == RoutineSource.MINE,
                            isSaving = state.savingCommunityItemId == plan.id,
                            onTap    = { viewModel.openPlanDetail(plan) },
                            onApply  = { viewModel.openApplyPlan(plan.id) },
                            onSave   = { viewModel.saveCommunityPlanToMine(plan) },
                            onDelete = { viewModel.deletePlan(plan.id) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SourceTabRow(active: RoutineSource, onSelect: (RoutineSource) -> Unit, mineLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        listOf(
            RoutineSource.COMMUNITY to "Comunidad",
            RoutineSource.MINE      to mineLabel,
        ).forEach { (src, label) ->
            val isSelected = active == src
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) SurfaceContainerLowest else Color.Transparent)
                    .clickable { onSelect(src) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) Primary else OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    isMine: Boolean,
    isSaving: Boolean,
    onTap: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Text(routine.emoji, fontSize = 24.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(routine.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val subtitle = buildString {
                    append("${routine.exercises.size} ejercicio${if (routine.exercises.size != 1) "s" else ""}")
                    routine.estimatedDurationMin?.let { append(" · $it min") }
                    routine.authorName?.takeIf { !isMine }?.let { append(" · por $it") }
                }
                Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isMine) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable(enabled = !isSaving, onClick = onSave),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Add, "Guardar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: WeeklyRoutinePlan,
    isMine: Boolean,
    isSaving: Boolean,
    onTap: () -> Unit,
    onApply: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onTap),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) { Text(plan.emoji, fontSize = 24.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val subtitle = buildString {
                        append("${plan.activeDays}/7 días activos")
                        plan.authorName?.takeIf { !isMine }?.let { append(" · por $it") }
                    }
                    Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
                }
                if (isMine) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable(enabled = !isSaving, onClick = onSave),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Add, "Guardar", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            if (isMine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.10f))
                        .clickable(onClick = onApply)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Primary, modifier = Modifier.size(16.dp))
                        Text("Aplicar a esta semana", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineDetailSheet(routine: Routine, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) { Text(routine.emoji, fontSize = 28.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(routine.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                    routine.authorName?.let { Text("por $it", fontSize = 13.sp, color = OnSurfaceVariant) }
                }
                routine.estimatedDurationMin?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$it", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("MIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.sp)
                    }
                }
            }
            if (routine.description.isNotBlank()) {
                Text(routine.description, fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp)
            }
            Text("EJERCICIOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            routine.exercises.forEach { ex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) { Text(ex.emoji, fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ex.exerciseName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val w = if (ex.targetWeightKg > 0f) " · ${ex.targetWeightKg.toInt()} kg" else ""
                        Text("${ex.targetSets}×${ex.targetReps}$w", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Text("${ex.restSeconds}s", fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDetailSheet(plan: WeeklyRoutinePlan, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) { Text(plan.emoji, fontSize = 28.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                    plan.authorName?.let { Text("por $it", fontSize = 13.sp, color = OnSurfaceVariant) }
                }
            }
            DayOfWeek.entries.forEach { day ->
                val routineName = plan.dayRoutineNames[day]
                val hasRoutine  = plan.days[day] != null
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(
                            if (hasRoutine) Primary.copy(alpha = 0.15f) else SurfaceContainerHigh
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.getDisplayName(TextStyle.SHORT, Locale("es")).take(3).uppercase(),
                            fontWeight = FontWeight.Bold, fontSize = 11.sp,
                            color = if (hasRoutine) Primary else OnSurfaceVariant,
                        )
                    }
                    Text(
                        routineName ?: "Descanso",
                        fontSize   = 14.sp,
                        fontWeight = if (hasRoutine) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (hasRoutine) OnSurface else OnSurfaceVariant,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayRoutinePickerSheet(
    date: LocalDate,
    routines: List<Routine>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Routine) -> Unit,
    onRest: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Asignar rutina a ${date.format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale("es")))}",
                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
                    .clickable(onClick = onRest)
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
            when {
                isLoading -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp)) }
                routines.isEmpty() -> Text(
                    "Aún no tienes rutinas. Crea una primero.",
                    fontSize = 13.sp, color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                )
                else -> routines.forEach { routine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow)
                            .clickable { onSelect(routine) }
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyPlanConfirmSheet(
    plan: WeeklyRoutinePlan,
    weekStart: LocalDate,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Aplicar plan semanal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
            Text(
                "Vas a sobrescribir las rutinas ya asignadas de la semana del " +
                    weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("es"))) +
                    " con las de \"${plan.name}\".",
                fontSize = 14.sp, color = OnSurfaceVariant, lineHeight = 20.sp,
            )
            Button(
                onClick  = onApply,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
            ) { Text("Aplicar", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", color = OnSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp)) }
}

@Composable
private fun EmptyMessage(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
        Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 18.sp)
    }
}
