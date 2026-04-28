package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.WeeklyRoutinePlan
import com.example.fitfusion.data.repository.RoutineRepository
import com.example.fitfusion.data.repository.WeeklyPlanRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek

data class CreateWeeklyPlanUiState(
    val name: String     = "",
    val emoji: String    = "📅",
    val isPublic: Boolean = false,
    val days: Map<DayOfWeek, String?> = DayOfWeek.entries.associateWith { null },
    val myRoutines: List<Routine> = emptyList(),
    val isLoadingRoutines: Boolean = false,
    val selectingForDay: DayOfWeek? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
) {
    val isValid: Boolean get() = name.isNotBlank() && days.values.any { it != null }
}

class CreateWeeklyPlanViewModel(
    private val routineRepository: RoutineRepository = RoutineRepository(),
    private val planRepository: WeeklyPlanRepository = WeeklyPlanRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWeeklyPlanUiState())
    val uiState: StateFlow<CreateWeeklyPlanUiState> = _uiState.asStateFlow()

    init { loadRoutines() }

    fun loadRoutines() {
        _uiState.update { it.copy(isLoadingRoutines = true) }
        routineRepository.fetchMine(
            onSuccess = { list ->
                _uiState.update { it.copy(myRoutines = list, isLoadingRoutines = false) }
            },
            onError = {
                _uiState.update { it.copy(isLoadingRoutines = false) }
            }
        )
    }

    fun onNameChange(v: String)      = _uiState.update { it.copy(name = v, saveError = null) }
    fun onEmojiChange(v: String)     = _uiState.update { it.copy(emoji = v.takeLast(2).ifBlank { "📅" }) }
    fun onPublicToggle(v: Boolean)   = _uiState.update { it.copy(isPublic = v) }

    fun openDaySelector(day: DayOfWeek) = _uiState.update { it.copy(selectingForDay = day) }
    fun closeDaySelector()              = _uiState.update { it.copy(selectingForDay = null) }

    fun assignRoutineToDay(day: DayOfWeek, routineId: String?) {
        _uiState.update {
            val updated = it.days.toMutableMap().apply { put(day, routineId) }
            it.copy(days = updated, selectingForDay = null)
        }
    }

    fun saveWeeklyPlan(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveError = null) }

        val dayNames = state.days.mapValues { (_, routineId) ->
            state.myRoutines.find { it.id == routineId }?.name
        }

        val plan = WeeklyRoutinePlan(
            name            = state.name.trim(),
            emoji           = state.emoji,
            days            = state.days,
            dayRoutineNames = dayNames,
            isPublic        = state.isPublic,
        )
        val authorName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")

        planRepository.save(
            plan       = plan,
            authorName = authorName,
            onSuccess  = {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            },
            onError    = { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Error al guardar") }
            }
        )
    }
}
