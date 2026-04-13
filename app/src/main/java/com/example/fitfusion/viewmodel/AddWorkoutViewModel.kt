package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class WorkoutCategory(val label: String, val emoji: String, val displayName: String) {
    STRENGTH("Multimúsculo", "🏋️", "Fuerza"),
    CARDIO("Gimnasia", "🤸", "Cardio"),
    YOGA("Flexibilidad", "🧘", "Yoga"),
    HIIT("Cardio libre", "⚡", "HIIT")
}

data class FeaturedWorkout(
    val title: String,
    val durationMin: Int,
    val intensity: String,
    val emoji: String = "💪"
)

data class RecentWorkout(
    val id: Int,
    val name: String,
    val durationMin: Int,
    val intensity: String,
    val metric: String,
    val emoji: String
)

data class AddWorkoutUiState(
    val featured: FeaturedWorkout = FeaturedWorkout(
        title = "Circuito de potencia explosiva",
        durationMin = 45,
        intensity = "Alta intensidad",
        emoji = "🔥"
    ),
    val recentWorkouts: List<RecentWorkout> = emptyList(),
    val loggedWorkoutIds: Set<Int> = emptySet()
)

class AddWorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddWorkoutUiState(
            recentWorkouts = listOf(
                RecentWorkout(1, "Carrera matutina", 22, "Moderado", "4.2 km", "🏃"),
                RecentWorkout(2, "Empuje tren superior", 48, "Fuerte", "12 series", "🏋️"),
                RecentWorkout(3, "Sesión de natación", 55, "Suave", "1.800 m", "🏊")
            )
        )
    )
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    fun logRecentWorkout(workoutId: Int) {
        _uiState.update { it.copy(loggedWorkoutIds = it.loggedWorkoutIds + workoutId) }
    }

    fun startFeaturedWorkout() {
        // Navigation / start workout logic goes here
    }

    fun startCategory(category: WorkoutCategory) {
        // Navigate to category workout list
    }
}