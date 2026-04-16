package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class EditFoodState(
    val loggedFood: LoggedFood,
    val selectedServing: Serving,
    val quantity: Int,
    val mealSlot: MealSlotType,
)

data class TrackingUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dayLog: DayLog = DayLog(LocalDate.now()),
    val weekSummary: WeekSummary = WeekSummary(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        emptyList()
    ),
    val mealConfig: MealConfig = MealConfig(),
    val expandedMeals: Set<MealSlotType> = setOf(MealSlotType.fromCurrentHour()),
    val showMealConfigDialog: Boolean = false,
    val workouts: List<LoggedWorkout> = emptyList(),
    val editFoodState: EditFoodState? = null,
    // Objetivos nutricionales (estáticos por ahora)
    val proteinGoal: Int = 160,
    val carbsGoal:   Int = 210,
    val fatsGoal:    Int = 65,
    val aiTip: String = "Llevas el 60% del objetivo proteico. Un batido post-entreno te ayudaría a cerrarlo.",
) {
    val kcalGoal:   Int   get() = dayLog.kcalGoal
    val kcalEaten:  Int   get() = dayLog.totalKcal
    val kcalBurned: Int   get() = workouts.sumOf { it.kcalBurned }
    val kcalNet:    Int   get() = (kcalEaten - kcalBurned).coerceAtLeast(0)
    val kcalLeft:   Int   get() = (kcalGoal - kcalNet).coerceAtLeast(0)
    val netProgress: Float get() = (kcalNet.toFloat() / kcalGoal.toFloat()).coerceIn(0f, 1f)
}

class TrackingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                FoodRepository.dayLogs,
                FoodRepository.mealConfig,
                WorkoutRepository.workouts,
            ) { logs, config, workoutMap -> Triple(logs, config, workoutMap) }
                .collect { (logs, config, workoutMap) ->
                    val date      = _uiState.value.selectedDate
                    val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    _uiState.update { current ->
                        current.copy(
                            dayLog      = logs[date] ?: DayLog(date),
                            weekSummary = FoodRepository.getWeekSummary(weekStart),
                            mealConfig  = config,
                            workouts    = workoutMap[date] ?: emptyList(),
                        )
                    }
                }
        }
    }

    fun selectDate(date: LocalDate) {
        val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _uiState.update {
            it.copy(
                selectedDate = date,
                dayLog       = FoodRepository.getDayLog(date),
                weekSummary  = FoodRepository.getWeekSummary(weekStart),
                workouts     = WorkoutRepository.getWorkoutsForDate(date),
            )
        }
    }

    fun toggleMeal(slot: MealSlotType) {
        _uiState.update {
            val expanded = it.expandedMeals.toMutableSet()
            if (slot in expanded) expanded.remove(slot) else expanded.add(slot)
            it.copy(expandedMeals = expanded)
        }
    }

    fun removeFood(id: String) {
        FoodRepository.removeFood(id, _uiState.value.selectedDate)
    }

    fun removeWorkout(id: String) {
        WorkoutRepository.removeWorkout(id, _uiState.value.selectedDate)
    }

    fun showMealConfigDialog() {
        _uiState.update { it.copy(showMealConfigDialog = true) }
    }

    fun dismissMealConfigDialog() {
        _uiState.update { it.copy(showMealConfigDialog = false) }
    }

    fun setMealCount(count: Int) {
        FoodRepository.updateMealConfig(MealConfig.forCount(count))
        _uiState.update { it.copy(showMealConfigDialog = false) }
    }

    // ── Edit sheet ────────────────────────────────────────────────────────────

    fun openEditSheet(logged: LoggedFood) {
        _uiState.update {
            it.copy(
                editFoodState = EditFoodState(
                    loggedFood      = logged,
                    selectedServing = logged.serving,
                    quantity        = logged.quantity,
                    mealSlot        = logged.mealSlot,
                )
            )
        }
    }

    fun dismissEditSheet() {
        _uiState.update { it.copy(editFoodState = null) }
    }

    fun editSelectServing(serving: Serving) {
        _uiState.update { cur ->
            val ef = cur.editFoodState ?: return@update cur
            cur.copy(editFoodState = ef.copy(selectedServing = serving))
        }
    }

    fun editIncrementQuantity() {
        _uiState.update { cur ->
            val ef = cur.editFoodState ?: return@update cur
            cur.copy(editFoodState = ef.copy(quantity = (ef.quantity + 1).coerceAtMost(20)))
        }
    }

    fun editDecrementQuantity() {
        _uiState.update { cur ->
            val ef = cur.editFoodState ?: return@update cur
            cur.copy(editFoodState = ef.copy(quantity = (ef.quantity - 1).coerceAtLeast(1)))
        }
    }

    fun editSelectSlot(slot: MealSlotType) {
        _uiState.update { cur ->
            val ef = cur.editFoodState ?: return@update cur
            cur.copy(editFoodState = ef.copy(mealSlot = slot))
        }
    }

    fun confirmEdit() {
        val ef = _uiState.value.editFoodState ?: return
        FoodRepository.updateFood(
            id         = ef.loggedFood.id,
            date       = _uiState.value.selectedDate,
            newServing  = ef.selectedServing,
            newQuantity = ef.quantity,
            newSlot     = ef.mealSlot,
        )
        _uiState.update { it.copy(editFoodState = null) }
    }
}
