package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.ScheduledRoutine
import com.example.fitfusion.data.models.WeeklyRoutinePlan
import com.example.fitfusion.data.repository.RoutineRepository
import com.example.fitfusion.data.repository.ScheduleRepository
import com.example.fitfusion.data.repository.WeeklyPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class PlannerTab { ESTA_SEMANA, RUTINAS, PLANES }
enum class RoutineSource { MINE, COMMUNITY }

data class PlannerUiState(
    val activeTab: PlannerTab = PlannerTab.ESTA_SEMANA,
    val weekStart: LocalDate  = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val schedule: Map<LocalDate, ScheduledRoutine> = emptyMap(),
    val routineSource: RoutineSource = RoutineSource.MINE,
    val myRoutines: List<Routine> = emptyList(),
    val communityRoutines: List<Routine> = emptyList(),
    val isLoadingMyRoutines: Boolean = false,
    val isLoadingCommunityRoutines: Boolean = false,
    val planSource: RoutineSource = RoutineSource.MINE,
    val myPlans: List<WeeklyRoutinePlan> = emptyList(),
    val communityPlans: List<WeeklyRoutinePlan> = emptyList(),
    val isLoadingMyPlans: Boolean = false,
    val isLoadingCommunityPlans: Boolean = false,
    val savingCommunityItemId: String? = null,
    val selectedRoutine: Routine? = null,
    val selectedPlan: WeeklyRoutinePlan? = null,
    val dayPickerDate: LocalDate? = null,
    val applyPlanId: String? = null,
    val feedback: String? = null,
)

class PlannerViewModel(
    private val routineRepository: RoutineRepository = RoutineRepository(),
    private val planRepository: WeeklyPlanRepository = WeeklyPlanRepository(),
    private val scheduleRepository: ScheduleRepository = ScheduleRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
        loadMyRoutines()
    }

    fun setActiveTab(tab: PlannerTab) {
        _uiState.update { it.copy(activeTab = tab) }
        when (tab) {
            PlannerTab.ESTA_SEMANA -> loadSchedule()
            PlannerTab.RUTINAS     -> if (_uiState.value.routineSource == RoutineSource.MINE) loadMyRoutines() else loadCommunityRoutines()
            PlannerTab.PLANES      -> if (_uiState.value.planSource == RoutineSource.MINE) loadMyPlans() else loadCommunityPlans()
        }
    }

    fun setRoutineSource(source: RoutineSource) {
        _uiState.update { it.copy(routineSource = source) }
        if (source == RoutineSource.MINE) loadMyRoutines() else loadCommunityRoutines()
    }

    fun setPlanSource(source: RoutineSource) {
        _uiState.update { it.copy(planSource = source) }
        if (source == RoutineSource.MINE) loadMyPlans() else loadCommunityPlans()
    }

    fun loadSchedule() {
        val weekStart = _uiState.value.weekStart
        scheduleRepository.fetchForWeek(
            weekStart = weekStart,
            onSuccess = { map -> _uiState.update { it.copy(schedule = map) } },
            onError   = { }
        )
    }

    fun loadMyRoutines() {
        if (_uiState.value.isLoadingMyRoutines) return
        _uiState.update { it.copy(isLoadingMyRoutines = true) }
        routineRepository.fetchMine(
            onSuccess = { list ->
                _uiState.update { it.copy(myRoutines = list, isLoadingMyRoutines = false) }
            },
            onError = { _uiState.update { it.copy(isLoadingMyRoutines = false) } }
        )
    }

    fun loadCommunityRoutines() {
        if (_uiState.value.isLoadingCommunityRoutines) return
        _uiState.update { it.copy(isLoadingCommunityRoutines = true) }
        routineRepository.fetchCommunity(
            onSuccess = { list ->
                _uiState.update { it.copy(communityRoutines = list, isLoadingCommunityRoutines = false) }
            },
            onError = { _uiState.update { it.copy(isLoadingCommunityRoutines = false) } }
        )
    }

    fun loadMyPlans() {
        if (_uiState.value.isLoadingMyPlans) return
        _uiState.update { it.copy(isLoadingMyPlans = true) }
        planRepository.fetchMine(
            onSuccess = { list ->
                _uiState.update { it.copy(myPlans = list, isLoadingMyPlans = false) }
            },
            onError = { _uiState.update { it.copy(isLoadingMyPlans = false) } }
        )
    }

    fun loadCommunityPlans() {
        if (_uiState.value.isLoadingCommunityPlans) return
        _uiState.update { it.copy(isLoadingCommunityPlans = true) }
        planRepository.fetchCommunity(
            onSuccess = { list ->
                _uiState.update { it.copy(communityPlans = list, isLoadingCommunityPlans = false) }
            },
            onError = { _uiState.update { it.copy(isLoadingCommunityPlans = false) } }
        )
    }

    fun saveCommunityRoutineToMine(routine: Routine) {
        if (_uiState.value.savingCommunityItemId != null) return
        _uiState.update { it.copy(savingCommunityItemId = routine.id) }
        routineRepository.saveFromCommunity(
            routine   = routine,
            onSuccess = {
                _uiState.update { it.copy(savingCommunityItemId = null, feedback = "Rutina guardada en mis rutinas") }
                loadMyRoutines()
            },
            onError = { e ->
                _uiState.update { it.copy(savingCommunityItemId = null, feedback = e.message ?: "No se pudo guardar") }
            }
        )
    }

    fun saveCommunityPlanToMine(plan: WeeklyRoutinePlan) {
        if (_uiState.value.savingCommunityItemId != null) return
        _uiState.update { it.copy(savingCommunityItemId = plan.id) }
        planRepository.saveFromCommunity(
            plan      = plan,
            onSuccess = {
                _uiState.update { it.copy(savingCommunityItemId = null, feedback = "Plan guardado en mis planes") }
                loadMyPlans()
            },
            onError = { e ->
                _uiState.update { it.copy(savingCommunityItemId = null, feedback = e.message ?: "No se pudo guardar") }
            }
        )
    }

    fun deleteRoutine(routineId: String) {
        routineRepository.delete(
            routineId = routineId,
            onSuccess = { loadMyRoutines() },
            onError   = { e -> _uiState.update { it.copy(feedback = e.message ?: "No se pudo eliminar") } }
        )
    }

    fun deletePlan(planId: String) {
        planRepository.delete(
            planId    = planId,
            onSuccess = { loadMyPlans() },
            onError   = { e -> _uiState.update { it.copy(feedback = e.message ?: "No se pudo eliminar") } }
        )
    }

    fun openRoutineDetail(routine: Routine)   = _uiState.update { it.copy(selectedRoutine = routine) }
    fun dismissRoutineDetail()                = _uiState.update { it.copy(selectedRoutine = null) }
    fun openPlanDetail(plan: WeeklyRoutinePlan) = _uiState.update { it.copy(selectedPlan = plan) }
    fun dismissPlanDetail()                     = _uiState.update { it.copy(selectedPlan = null) }

    fun openDayPicker(date: LocalDate) {
        loadMyRoutines()
        _uiState.update { it.copy(dayPickerDate = date) }
    }
    fun closeDayPicker() = _uiState.update { it.copy(dayPickerDate = null) }

    fun assignRoutineToDate(date: LocalDate, routine: Routine?) {
        if (routine == null) {
            scheduleRepository.clearDate(
                date      = date,
                onSuccess = {
                    _uiState.update { it.copy(dayPickerDate = null, feedback = "Día marcado como descanso") }
                    loadSchedule()
                },
                onError = { e -> _uiState.update { it.copy(feedback = e.message ?: "No se pudo actualizar") } }
            )
            return
        }
        scheduleRepository.assignRoutine(
            date      = date,
            routine   = routine,
            onSuccess = {
                _uiState.update { it.copy(dayPickerDate = null, feedback = "Rutina asignada") }
                loadSchedule()
            },
            onError = { e -> _uiState.update { it.copy(feedback = e.message ?: "No se pudo asignar") } }
        )
    }

    fun openApplyPlan(planId: String) = _uiState.update { it.copy(applyPlanId = planId) }
    fun closeApplyPlan()              = _uiState.update { it.copy(applyPlanId = null) }

    fun applyPlanToWeek(plan: WeeklyRoutinePlan, weekStart: LocalDate) {
        val lookup = (_uiState.value.myRoutines + _uiState.value.communityRoutines).associateBy { it.id }
        scheduleRepository.applyWeeklyPlan(
            plan          = plan,
            weekStart     = weekStart,
            routineLookup = lookup,
            onSuccess     = {
                _uiState.update { it.copy(applyPlanId = null, feedback = "Plan aplicado a la semana") }
                loadSchedule()
            },
            onError       = { e -> _uiState.update { it.copy(feedback = e.message ?: "No se pudo aplicar") } }
        )
    }

    fun clearFeedback() = _uiState.update { it.copy(feedback = null) }
}
