package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Un punto en la curva de progreso de un ejercicio (volumen acumulado por fecha de entrenamiento). */
data class ExerciseProgressPoint(
    val date: LocalDate,
    val totalVolume: Float,
    val topSetVolume: Float,
)

/** Progreso histórico de un ejercicio concreto a lo largo de las sesiones registradas. */
data class ExerciseProgress(
    val name: String,
    val documentId: String?,
    val sessionsCount: Int,
    val points: List<ExerciseProgressPoint>,
) {
    val bestSetVolume: Float get() = points.maxOfOrNull { it.topSetVolume } ?: 0f
    val latestVolume: Float get() = points.lastOrNull()?.totalVolume ?: 0f
    val firstVolume: Float get() = points.firstOrNull()?.totalVolume ?: 0f
}

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val workouts: List<LoggedWorkout> = emptyList(),
    val exerciseProgress: List<ExerciseProgress> = emptyList(),
    val totalWorkouts: Int = 0,
    val totalVolumeKg: Float = 0f,
    val workoutsThisWeek: Int = 0,
)

/**
 * Alimenta la pantalla de entrenamiento: historial de sesiones y progreso por ejercicio.
 * Solo lee — toda la persistencia vive en [WorkoutRepository].
 */
class WorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        WorkoutRepository.ensureInitialized()
        viewModelScope.launch {
            WorkoutRepository.workouts.collect { byDate ->
                _uiState.value = buildState(byDate)
            }
        }
    }

    private fun buildState(byDate: Map<LocalDate, List<LoggedWorkout>>): WorkoutUiState {
        val all = byDate.values.flatten()
            .sortedWith(
                compareByDescending<LoggedWorkout> { it.date }
                    .thenByDescending { it.createdAtMs ?: 0L }
            )
        val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return WorkoutUiState(
            isLoading = false,
            workouts = all,
            exerciseProgress = buildExerciseProgress(all),
            totalWorkouts = all.size,
            totalVolumeKg = all.sumOf { it.totalVolumeKg.toDouble() }.toFloat(),
            workoutsThisWeek = all.count { !it.date.isBefore(weekStart) },
        )
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

    private fun buildExerciseProgress(workouts: List<LoggedWorkout>): List<ExerciseProgress> {
        val grouped = LinkedHashMap<String, MutableList<Pair<LocalDate, WorkoutExercise>>>()
        for (workout in workouts) {
            for (exercise in workout.exercises) {
                val key = exercise.exerciseDocumentId?.takeIf { it.isNotBlank() }
                    ?: exercise.name.trim().lowercase()
                grouped.getOrPut(key) { mutableListOf() }.add(workout.date to exercise)
            }
        }
        return grouped.values.map { pairs ->
            val points = pairs.groupBy { it.first }
                .map { (date, dayPairs) ->
                    ExerciseProgressPoint(
                        date = date,
                        totalVolume = dayPairs.sumOf { it.second.totalVolume.toDouble() }.toFloat(),
                        topSetVolume = dayPairs.flatMap { it.second.sets }
                            .maxOfOrNull { it.reps * it.weightKg } ?: 0f,
                    )
                }
                .sortedBy { it.date }
            val latestName = pairs.maxByOrNull { it.first }!!.second.name
            ExerciseProgress(
                name = latestName,
                documentId = pairs.firstNotNullOfOrNull { pair ->
                    pair.second.exerciseDocumentId?.takeIf { it.isNotBlank() }
                },
                sessionsCount = points.size,
                points = points,
            )
        }.sortedByDescending { it.points.lastOrNull()?.date }
    }
}
