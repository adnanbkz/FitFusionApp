package com.example.fitfusion.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IntensityZone(
    val label: String,
    val duration: String,
    val bpmRange: String,
    val percentage: String,
    val color: Color,
    val progress: Float
)

data class WorkoutSummaryUiState(
    val sessionType: String = "CARDIO SESSION",
    val title: String = "Morning Run",
    val loggedDate: String = "Logged on June 12, 2024 at 7:15 AM",
    val totalKcal: String = "482",
    val duration: String = "45:00",
    val pace: String = "8'12\"",
    val mapLocation: String = "Central Park Loop",
    val mapCity: String = "New York, NY",
    val zones: List<IntensityZone> = listOf(
        IntensityZone("Peak", "08:24", "165 - 180 BPM", "18.6% OF WORKOUT", Color(0xFFB02F00), 0.186f),
        IntensityZone("Cardio", "26:15", "140 - 164 BPM", "58.3% OF WORKOUT", Color(0xFF006E0A), 0.583f),
        IntensityZone("Fat Burn", "10:21", "115 - 139 BPM", "23.1% OF WORKOUT", Color(0xFF476083), 0.231f)
    ),
    val goalReached: Boolean = true,
    val goalMessage: String = "This run completed your cardio goal for the week. You're 12% ahead of your schedule."
)

class WorkoutSummaryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSummaryUiState())
    val uiState: StateFlow<WorkoutSummaryUiState> = _uiState.asStateFlow()

    // TODO: Load workout from Firestore by workoutId
}