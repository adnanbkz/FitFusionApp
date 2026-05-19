package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.ai.AiCalorieGoalRequest
import com.example.fitfusion.data.ai.AiMealPlanDish
import com.example.fitfusion.data.ai.AiMealPlanResponse
import com.example.fitfusion.data.health.DailyHealthData
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.AiRepository
import com.example.fitfusion.data.repository.DailySummary
import com.example.fitfusion.data.repository.DailySummaryRepository
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.HealthRepository
import com.example.fitfusion.data.repository.UserRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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
    val dailySummary: DailySummary? = null,
    val editFoodState: EditFoodState? = null,
    val kcalGoal:    Int = 2000,
    val proteinGoal: Int = 160,
    val carbsGoal:   Int = 210,
    val fatsGoal:    Int = 65,
    val aiTip: String = "Llevas el 60% del objetivo proteico. Un batido post-entreno te ayudaria a cerrarlo.",
) {
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
    private val userRepository = UserRepository()
    private var healthListenerRegistration: ListenerRegistration? = null
    private var summaryListenerRegistration: ListenerRegistration? = null
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
        attachDailySummaryListener(LocalDate.now())
        loadCalorieGoal()
    }

    fun selectDate(date: LocalDate) {
        val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _uiState.update {
            it.copy(
                selectedDate = date,
                dayLog       = FoodRepository.getDayLog(date),
                weekSummary  = FoodRepository.getWeekSummary(weekStart),
                workouts     = WorkoutRepository.getWorkoutsForDate(date),
                dailySummary = null,
            )
        }
        attachHealthDataListener(date)
        attachDailySummaryListener(date)
    }

    private fun attachHealthDataListener(date: LocalDate) {
        healthListenerRegistration?.remove()
        healthListenerRegistration = healthRepository.listenDailyHealthData(date) { healthData ->
            _uiState.update { current ->
                if (current.selectedDate == date) current.copy(healthData = healthData) else current
            }
        }
    }

    private fun attachDailySummaryListener(date: LocalDate) {
        summaryListenerRegistration?.remove()
        summaryListenerRegistration = DailySummaryRepository.listenDay(date) { summary ->
            _uiState.update { current ->
                if (current.selectedDate == date) current.copy(dailySummary = summary) else current
            }
        }
    }

    /**
     * Pide al backend C# el objetivo calórico/macros del usuario según sus
     * métricas (altura, peso, edad, actividad, objetivo). Si falla (sin perfil
     * completo, sin red o backend caído) se mantienen los valores por defecto.
     */
    private fun loadCalorieGoal() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = runCatching { userRepository.getUserProfile(uid) }.getOrNull() ?: return@launch
            val heightCm = profile.heightCm ?: return@launch
            val weightKg = profile.weightKg ?: return@launch
            val age = ageFromBirthDate(profile.birthDate) ?: return@launch
            val goal = AiRepository.calculateCalorieGoal(
                AiCalorieGoalRequest(
                    heightCm      = heightCm,
                    weightKg      = weightKg,
                    age           = age,
                    activityLevel = profile.activityLevel.orEmpty(),
                    goalType      = profile.goalType.orEmpty(),
                )
            ).getOrNull() ?: return@launch
            _uiState.update {
                it.copy(
                    kcalGoal    = goal.targetKcal,
                    proteinGoal = goal.proteinG,
                    carbsGoal   = goal.carbsG,
                    fatsGoal    = goal.fatG,
                )
            }
        }
    }

    /** Edad en años a partir de la fecha "DD/MM/AAAA"; null si no es válida. */
    private fun ageFromBirthDate(birthDate: String?): Int? {
        if (birthDate.isNullOrBlank()) return null
        return runCatching {
            val date = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd/MM/uuuu"))
            Period.between(date, LocalDate.now()).years
        }.getOrNull()?.takeIf { it in 5..120 }
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
        val date = _uiState.value.selectedDate
        val meal = MealSlot(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            isCustom = true,
        )
        dismissAddMealDialog()
        viewModelScope.launch { FoodRepository.addMealToDay(date, meal) }
    }

    /**
     * Aplica un plan de comidas generado por IA al día seleccionado. La dieta IA
     * anterior del día se borra (la reemplaza esta); las comidas registradas a
     * mano se conservan.
     */
    fun applyAiMealPlan(plan: AiMealPlanResponse) {
        val date = _uiState.value.selectedDate
        val day = plan.days.firstOrNull() ?: return
        viewModelScope.launch {
            val foods = day.meals.flatMap { meal ->
                val slot = resolveSlot(meal.slotName)
                meal.dishes.map { dish -> dish.toLoggedFood(slot, date) }
            }
            FoodRepository.replaceAiMealPlan(date, foods)
        }
    }

    private fun resolveSlot(slotName: String): MealSlot {
        val key = slotName.trim().lowercase()
        return when {
            "desayuno"      in key                                   -> MealSlot.BREAKFAST
            "media"         in key || "media mañana" in key           -> MealSlot.MORNING_SNACK
            "almuerzo"      in key || "comida" in key                 -> MealSlot.LUNCH
            "merienda"      in key || key == "snack"                  -> MealSlot.AFTERNOON_SNACK
            "cena"          in key                                   -> MealSlot.DINNER
            "noche"         in key                                   -> MealSlot.EVENING_SNACK
            else                                                     -> MealSlot.LUNCH
        }
    }

    private fun AiMealPlanDish.toLoggedFood(slot: MealSlot, date: LocalDate): LoggedFood {
        val serving = Serving("1 porción", 100f)
        val food = Food(
            id              = "ai-${java.util.UUID.randomUUID()}",
            name            = name,
            kcalPer100g     = kcal.toFloat(),
            proteinPer100g  = proteinG.toFloat(),
            carbsPer100g    = carbsG.toFloat(),
            fatsPer100g     = fatG.toFloat(),
            servingOptions  = listOf(serving),
        )
        return LoggedFood(
            food     = food,
            serving  = serving,
            quantity = 1,
            mealSlot = slot,
            date     = date,
        )
    }

    fun removeMeal(mealId: String) {
        val date = _uiState.value.selectedDate
        viewModelScope.launch { FoodRepository.removeMealFromDay(date, mealId) }
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
        val date = _uiState.value.selectedDate
        val mealId = _uiState.value.renameMealId
        dismissRenameMealDialog()
        viewModelScope.launch { FoodRepository.renameMealInDay(date, mealId, name) }
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
        summaryListenerRegistration?.remove()
        summaryListenerRegistration = null
        super.onCleared()
    }
}
