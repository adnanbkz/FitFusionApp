package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class WorkoutCategory(val label: String, val emoji: String) {
    STRENGTH("Multi-muscle", "🏋️"),
    CARDIO("Gymnastics", "🤸"),
    YOGA("Flexibility", "🧘"),
    HIIT("Like Nike Run", "⚡")
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
        title = "Explosive Power Circuit",
        durationMin = 45,
        intensity = "High Intensity",
        emoji = "🔥"
    ),
    val recentWorkouts: List<RecentWorkout> = emptyList(),
    val loggedWorkoutIds: Set<Int> = emptySet()
)

class AddWorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddWorkoutUiState(
            recentWorkouts = listOf(
                RecentWorkout(1, "Morning Jog", 22, "Moderate", "4.2 km", "🏃"),
                RecentWorkout(2, "Upper Body Push", 48, "Hard", "12 sets", "🏋️"),
                RecentWorkout(3, "Swim Session", 55, "Low", "1,800m", "🏊")
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