package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.health.DailyHealthData
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.HealthRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class EditFoodState(
    val loggedFood: LoggedFood,
    val selectedServing: Serving,
    val quantity: Int,
    val mealSlot: MealSlot,
)

data class TrackingUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dayLog: DayLog = DayLog(LocalDate.now()),
    val weekSummary: WeekSummary = WeekSummary(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        emptyList()
    ),
    val expandedMeals: Set<String> = setOf(MealSlot.fromCurrentHour().id),
    val showAddMealDialog: Boolean = false,
    val addMealName: String = "",
    val showRenameMealDialog: Boolean = false,
    val renameMealId: String = "",
    val renameMealName: String = "",
    val workouts: List<LoggedWorkout> = emptyList(),
    val healthData: DailyHealthData? = null,
    val editFoodState: EditFoodState? = null,
    val proteinGoal: Int = 160,
    val carbsGoal:   Int = 210,
    val fatsGoal:    Int = 65,
    val aiTip: String = "Llevas el 60% del objetivo proteico. Un batido post-entreno te ayudaria a cerrarlo.",
) {
    val kcalGoal:    Int   get() = dayLog.kcalGoal
    val kcalEaten:   Int   get() = dayLog.totalKcal
    val workoutKcalBurned: Int get() = workouts.sumOf { it.kcalBurned }
    val stepKcalBurned: Int get() = healthData?.stepCaloriesEstimated ?: 0
    val kcalBurned:  Int   get() = workoutKcalBurned + stepKcalBurned
    val kcalNet:     Int   get() = (kcalEaten - kcalBurned).coerceAtLeast(0)
    val kcalLeft:    Int   get() = (kcalGoal - kcalNet).coerceAtLeast(0)
    val netProgress: Float get() = (kcalNet.toFloat() / kcalGoal.toFloat()).coerceIn(0f, 1f)
}

class TrackingViewModel : ViewModel() {

    private val healthRepository = HealthRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance(),
    )
    private var healthListenerRegistration: ListenerRegistration? = null
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                FoodRepository.dayLogs,
                WorkoutRepository.workouts,
            ) { logs, workoutMap -> Pair(logs, workoutMap) }
                .collect { (logs, workoutMap) ->
                    val date      = _uiState.value.selectedDate
                    val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    _uiState.update { current ->
                        current.copy(
                            dayLog      = logs[date] ?: DayLog(date),
                            weekSummary = FoodRepository.getWeekSummary(weekStart),
                            workouts    = workoutMap[date] ?: emptyList(),
                        )
                    }
                }
        }
        attachHealthDataListener(LocalDate.now())
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
        attachHealthDataListener(date)
    }

    private fun attachHealthDataListener(date: LocalDate) {
        healthListenerRegistration?.remove()
        healthListenerRegistration = healthRepository.listenDailyHealthData(date) { healthData ->
            _uiState.update { current ->
                if (current.selectedDate == date) current.copy(healthData = healthData) else current
            }
        }
    }

    fun toggleMeal(mealId: String) {
        _uiState.update {
            val expanded = it.expandedMeals.toMutableSet()
            if (mealId in expanded) expanded.remove(mealId) else expanded.add(mealId)
            it.copy(expandedMeals = expanded)
        }
    }

    fun removeFood(id: String) {
        val date = _uiState.value.selectedDate
        viewModelScope.launch { FoodRepository.removeFood(id, date) }
    }

    fun showAddMealDialog() {
        _uiState.update { it.copy(showAddMealDialog = true, addMealName = "") }
    }

    fun dismissAddMealDialog() {
        _uiState.update { it.copy(showAddMealDialog = false, addMealName = "") }
    }

    fun onAddMealNameChange(name: String) {
        _uiState.update { it.copy(addMealName = name) }
    }

    fun confirmAddMeal() {
        val name = _uiState.value.addMealName.trim()
        if (name.isBlank()) return
        val meal = MealSlot(
            id       = java.util.UUID.randomUUID().toString(),
            name     = name,
            isCustom = true
        )
        FoodRepository.addMealToDay(_uiState.value.selectedDate, meal)
        _uiState.update { it.copy(showAddMealDialog = false, addMealName = "") }
    }

    fun removeMeal(mealId: String) {
        FoodRepository.removeMealFromDay(_uiState.value.selectedDate, mealId)
    }

    fun showRenameMealDialog(mealId: String, currentName: String) {
        _uiState.update { it.copy(showRenameMealDialog = true, renameMealId = mealId, renameMealName = currentName) }
    }

    fun dismissRenameMealDialog() {
        _uiState.update { it.copy(showRenameMealDialog = false, renameMealId = "", renameMealName = "") }
    }

    fun onRenameMealNameChange(name: String) {
        _uiState.update { it.copy(renameMealName = name) }
    }

    fun confirmRenameMeal() {
        val name = _uiState.value.renameMealName.trim()
        if (name.isBlank()) return
        FoodRepository.renameMealInDay(_uiState.value.selectedDate, _uiState.value.renameMealId, name)
        dismissRenameMealDialog()
    }

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

    fun editSelectSlot(slot: MealSlot) {
        _uiState.update { cur ->
            val ef = cur.editFoodState ?: return@update cur
            cur.copy(editFoodState = ef.copy(mealSlot = slot))
        }
    }

    fun confirmEdit() {
        val ef   = _uiState.value.editFoodState ?: return
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(editFoodState = null) }
        viewModelScope.launch {
            FoodRepository.updateFood(
                id          = ef.loggedFood.id,
                date        = date,
                newServing  = ef.selectedServing,
                newQuantity = ef.quantity,
                newSlot     = ef.mealSlot,
            )
        }
    }

    override fun onCleared() {
        healthListenerRegistration?.remove()
        healthListenerRegistration = null
        super.onCleared()
    }
}
