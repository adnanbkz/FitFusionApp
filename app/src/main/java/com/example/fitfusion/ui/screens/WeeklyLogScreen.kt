package com.example.fitfusion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.DayLog
import com.example.fitfusion.data.models.MealSlotType
import com.example.fitfusion.data.models.WeekSummary
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// ── ViewModel ─────────────────────────────────────────────────────────────────

data class WeeklyLogUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val weekSummary: WeekSummary = WeekSummary(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        emptyList()
    ),
    val expandedDay: LocalDate? = null,
)

class WeeklyLogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        WeeklyLogUiState().let { s ->
            s.copy(weekSummary = FoodRepository.getWeekSummary(s.weekStart))
        }
    )
    val uiState: StateFlow<WeeklyLogUiState> = _uiState.asStateFlow()

    fun previousWeek() {
        _uiState.update {
            val newStart = it.weekStart.minusWeeks(1)
            it.copy(
                weekStart   = newStart,
                weekSummary = FoodRepository.getWeekSummary(newStart),
                expandedDay = null,
            )
        }
    }

    fun nextWeek() {
        val today     = LocalDate.now()
        val currentStart = _uiState.value.weekStart
        val nextStart = currentStart.plusWeeks(1)
        if (nextStart.isAfter(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))) return
        _uiState.update {
            it.copy(
                weekStart   = nextStart,
                weekSummary = FoodRepository.getWeekSummary(nextStart),
                expandedDay = null,
            )
        }
    }

    fun toggleDay(date: LocalDate) {
        _uiState.update {
            it.copy(expandedDay = if (it.expandedDay == date) null else date)
        }
    }
}

// ── Pantalla ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWeeklyLog(
    navController: NavHostController,
    weeklyLogViewModel: WeeklyLogViewModel = viewModel(),
) {
    val state by weeklyLogViewModel.uiState.collectAsState()
    val today = LocalDate.now()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Registro semanal", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 4.dp)
        ) {
            // ── Navegador de semana ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = weeklyLogViewModel::previousWeek) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior", tint = OnSurface)
                    }
                    val endDate = state.weekStart.plusDays(6)
                    val fmt     = DateTimeFormatter.ofPattern("d MMM", Locale("es"))
                    Text(
                        "${state.weekStart.format(fmt)} – ${endDate.format(fmt)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp,
                        color      = OnSurface
                    )
                    IconButton(
                        onClick  = weeklyLogViewModel::nextWeek,
                        enabled  = state.weekStart.plusWeeks(1) <=
                            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Semana siguiente", tint = OnSurface)
                    }
                }
            }

            // ── Gráfico de barras ─────────────────────────────────────────────
            item {
                Card(
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("KCAL POR DÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                        Spacer(Modifier.height(16.dp))
                        WeekBarChart(
                            days       = state.weekSummary.days,
                            kcalGoal   = 2000,
                            today      = today,
                            weekStart  = state.weekStart,
                            onDayTap   = weeklyLogViewModel::toggleDay,
                            expandedDay = state.expandedDay
                        )
                    }
                }
            }

            // ── Tarjeta resumen ───────────────────────────────────────────────
            item {
                Card(
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("RESUMEN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryStatCard("${state.weekSummary.avgKcal}", "MEDIA DIARIA", "kcal", Primary)
                            SummaryStatCard("${state.weekSummary.daysLogged}/7", "DÍAS LOGADOS", "", OnSurface)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryStatCard("${state.weekSummary.daysOnTrack}/7", "EN OBJETIVO", "", Secondary)
                            SummaryStatCard("${state.weekSummary.avgProtein}g", "PROT. MEDIA", "/día", Tertiary)
                        }
                    }
                }
            }

            // ── Desglose por día ──────────────────────────────────────────────
            item {
                Text(
                    "DESGLOSE POR DÍA",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Primary
                )
            }

            val dayLabels = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
            state.weekSummary.days.forEachIndexed { idx, dayLog ->
                item(key = dayLog.date.toString()) {
                    DayLogRow(
                        dayName    = dayLabels[idx],
                        dayLog     = dayLog,
                        isToday    = dayLog.date == today,
                        isExpanded = state.expandedDay == dayLog.date,
                        onToggle   = { weeklyLogViewModel.toggleDay(dayLog.date) }
                    )
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun WeekBarChart(
    days: List<DayLog>,
    kcalGoal: Int,
    today: LocalDate,
    weekStart: LocalDate,
    onDayTap: (LocalDate) -> Unit,
    expandedDay: LocalDate?,
) {
    val maxKcal  = maxOf(days.maxOfOrNull { it.totalKcal } ?: 0, kcalGoal).toFloat()
    val maxBarHeight = 80.dp
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { idx, dayLog ->
            val date       = weekStart.plusDays(idx.toLong())
            val isToday    = date == today
            val isFuture   = date.isAfter(today)
            val isExpanded = date == expandedDay
            val barFraction = if (maxKcal > 0f) dayLog.totalKcal / maxKcal else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(enabled = !isFuture) { onDayTap(date) }
            ) {
                // Kcal label
                if (dayLog.entries.isNotEmpty()) {
                    Text(
                        "${dayLog.totalKcal}",
                        fontSize   = 9.sp,
                        color      = if (isExpanded) Primary else OnSurfaceVariant,
                        fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal,
                        modifier   = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    Spacer(Modifier.height(18.dp))
                }
                // Barra
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(maxBarHeight * barFraction.coerceAtLeast(if (isFuture) 0f else 0.02f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            when {
                                isExpanded -> Primary
                                isToday    -> Primary.copy(alpha = 0.7f)
                                isFuture   -> SurfaceContainerHigh.copy(alpha = 0.4f)
                                dayLog.isOnTrack -> PrimaryContainer.copy(alpha = 0.5f)
                                dayLog.entries.isNotEmpty() -> Secondary.copy(alpha = 0.4f)
                                else -> SurfaceContainerHigh
                            }
                        )
                )
                // Día
                Text(
                    dayLabels[idx],
                    fontSize   = 11.sp,
                    fontWeight = if (isToday || isExpanded) FontWeight.Bold else FontWeight.Normal,
                    color      = when {
                        isExpanded -> Primary
                        isToday    -> OnSurface
                        isFuture   -> OnSurfaceVariant.copy(alpha = 0.4f)
                        else       -> OnSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 6.dp)
                )
                // Indicador objetivo
                if (dayLog.isOnTrack) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                } else {
                    Spacer(Modifier.height(9.dp))
                }
            }
        }
    }

    // Línea de objetivo
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
        Text("Objetivo: $kcalGoal kcal", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SummaryStatCard(value: String, label: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            if (unit.isNotBlank()) {
                Text(" $unit", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun DayLogRow(
    dayName: String,
    dayLog: DayLog,
    isToday: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) Primary.copy(alpha = 0.05f) else SurfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                dayName,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize   = 15.sp,
                                color      = if (isToday) Primary else OnSurface
                            )
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("HOY", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Primary)
                                }
                            }
                            if (dayLog.isOnTrack) {
                                Text("✓", fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (dayLog.entries.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                MacroTag("${dayLog.totalKcal} kcal", OnSurface)
                                MacroTag("P ${dayLog.totalProtein}g", Primary)
                                MacroTag("C ${dayLog.totalCarbs}g", Secondary)
                                MacroTag("G ${dayLog.totalFat}g", Tertiary)
                            }
                        } else {
                            Text("Sin registros", fontSize = 12.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
                if (dayLog.entries.isNotEmpty()) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Detalle expandible
            AnimatedVisibility(
                visible = isExpanded && dayLog.entries.isNotEmpty(),
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                    HorizontalDivider(
                        color    = OutlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    MealSlotType.entries.forEach { slot ->
                        val slotEntries = dayLog.byMeal[slot]
                        if (!slotEntries.isNullOrEmpty()) {
                            Text(
                                "${slot.emoji} ${slot.label}",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = OnSurfaceVariant,
                                modifier   = Modifier.padding(top = 6.dp, bottom = 4.dp)
                            )
                            slotEntries.forEach { logged ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${logged.food.emoji} ${logged.food.name}",
                                        fontSize = 13.sp, color = OnSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${logged.kcal} kcal",
                                        fontSize = 13.sp, color = OnSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroTag(text: String, color: Color) {
    Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
}

