package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LogEntry(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val calories: String,
    val unit: String = "KCAL"
)

data class TrackingUiState(
    val kcalLeft: Int = 1420,
    val eaten: Int = 980,
    val burned: Int = 340,
    val protein: Int = 112,
    val proteinGoal: Int = 160,
    val carbs: Int = 145,
    val carbsGoal: Int = 210,
    val fats: Int = 42,
    val fatsGoal: Int = 65,
    val aiTip: String = "You're slightly under your protein goal. Consider a Greek yogurt snack.",
    val recentLogs: List<LogEntry> = listOf(
        LogEntry("\uD83C\uDF5C", "Quinoa Buddha Bowl", "Lunch • 12:45 PM", "450"),
        LogEntry("⚡", "High Intensity Inter...", "Workout • 08:30 AM", "-340"),
        LogEntry("☕", "Iced Oat Milk Latte", "Breakfast • 07:15 AM", "180")
    )
)

class TrackingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    // TODO: Load food logs, daily summary from Firestore
    // TODO: Recalculate net calories
}