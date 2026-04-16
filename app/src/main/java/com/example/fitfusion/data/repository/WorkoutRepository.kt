package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

object WorkoutRepository {

    private val _workouts = MutableStateFlow<Map<LocalDate, List<LoggedWorkout>>>(emptyMap())
    val workouts: StateFlow<Map<LocalDate, List<LoggedWorkout>>> = _workouts.asStateFlow()

    init {
        seedMockData()
    }

    fun getWorkoutsForDate(date: LocalDate): List<LoggedWorkout> =
        _workouts.value[date] ?: emptyList()

    fun addWorkout(workout: LoggedWorkout) {
        _workouts.update { map ->
            val existing = map[workout.date] ?: emptyList()
            map + (workout.date to existing + workout)
        }
    }

    fun removeWorkout(id: String, date: LocalDate) {
        _workouts.update { map ->
            val updated = (map[date] ?: emptyList()).filter { it.id != id }
            if (updated.isEmpty()) map - date else map + (date to updated)
        }
    }

    private fun seedMockData() {
        val today = LocalDate.now()

        val todayWorkout = LoggedWorkout(
            id = "mock_today_1",
            date = today,
            name = "Fuerza — Empuje",
            emoji = "🏋️",
            durationMinutes = 52,
            kcalBurned = 390,
            exercises = listOf(
                WorkoutExercise(
                    name = "Sentadilla",
                    muscleGroup = "Piernas",
                    sets = List(4) { WorkoutSet(reps = 8, weightKg = 80f) }
                ),
                WorkoutExercise(
                    name = "Press de Banca",
                    muscleGroup = "Pecho",
                    sets = List(3) { WorkoutSet(reps = 10, weightKg = 60f) }
                ),
                WorkoutExercise(
                    name = "Peso Muerto",
                    muscleGroup = "Espalda",
                    sets = List(3) { WorkoutSet(reps = 6, weightKg = 100f) }
                ),
            )
        )

        val tuesdayWorkout = LoggedWorkout(
            id = "mock_tue_1",
            date = today.minusDays(2),
            name = "Cardio — HIIT",
            emoji = "🏃",
            durationMinutes = 30,
            kcalBurned = 280,
            exercises = listOf(
                WorkoutExercise("Burpees", "Full body", List(4) { WorkoutSet(reps = 15) }),
                WorkoutExercise("Mountain climbers", "Core", List(3) { WorkoutSet(reps = 20) }),
            )
        )

        _workouts.value = mapOf(
            today to listOf(todayWorkout),
            today.minusDays(2) to listOf(tuesdayWorkout),
        )
    }
}
