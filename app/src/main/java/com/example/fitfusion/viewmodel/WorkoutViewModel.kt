package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class WorkoutUiState(
    val selectedWorkoutDay: LocalDate = LocalDate.now(),
    val selectedDayWorkouts: List<LoggedWorkout> = emptyList(),
    val currentWeekMinutes: List<Int> = List(7) { 0 },
    val previousWeekMinutes: List<Int> = List(7) { 0 },
    val totalSessionsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val totalKcalThisWeek: Int = 0,
)

class WorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        WorkoutUiState(
            selectedDayWorkouts = WorkoutRepository.getWorkoutsForDate(LocalDate.now())
        )
    )
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var workoutsByDay: Map<LocalDate, List<LoggedWorkout>> = emptyMap()

    init {
        viewModelScope.launch {
            WorkoutRepository.workouts.collect { workoutMap ->
                val today = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val prevStart = weekStart.minusWeeks(1)

                val curMins = minutesPerDay(workoutMap, weekStart)
                val prevMins = minutesPerDay(workoutMap, prevStart)
                val thisWeek = (0L..6L).flatMap { offset ->
                    workoutMap[weekStart.plusDays(offset)] ?: emptyList()
                }

                workoutsByDay = workoutMap

                _uiState.update { s ->
                    s.copy(
                        currentWeekMinutes = curMins,
                        previousWeekMinutes = prevMins,
                        totalSessionsThisWeek = thisWeek.size,
                        totalMinutesThisWeek = thisWeek.sumOf { it.durationMinutes },
                        totalKcalThisWeek = thisWeek.sumOf { it.kcalBurned },
                        selectedDayWorkouts = workoutMap[s.selectedWorkoutDay] ?: emptyList(),
                    )
                }
            }
        }
    }

    fun selectWorkoutDay(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedWorkoutDay = date,
                selectedDayWorkouts = workoutsByDay[date] ?: emptyList(),
            )
        }
    }

    fun removeWorkoutFromDay(id: String, date: LocalDate) {
        WorkoutRepository.removeWorkout(id, date)
    }

    fun updateWorkout(
        workout: LoggedWorkout,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                WorkoutRepository.updateWorkout(workout)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "No se pudo actualizar el entrenamiento")
            }
        }
    }

    private fun minutesPerDay(
        map: Map<LocalDate, List<LoggedWorkout>>,
        weekStart: LocalDate,
    ): List<Int> = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        map[date]?.sumOf { it.durationMinutes } ?: 0
    }
}
