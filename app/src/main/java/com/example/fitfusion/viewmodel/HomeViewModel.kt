package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FeedPostData(
    val author: String,
    val time: String,
    val tag: String,
    val likes: Int,
    val comments: Int,
    val description: String
)

data class HomeUiState(
    val momentumPercent: Int = 82,
    val kcalRemaining: Int = 1450,
    val posts: List<FeedPostData> = listOf(
        FeedPostData(
            author = "Alex Miller",
            time = "2 hours ago",
            tag = "Morning Run",
            likes = 124,
            comments = 18,
            description = "Crushing the morning miles. The air in the valley was crisp today! \uD83C\uDF32\uD83C\uDFC3 #KineticRun #MorningVibes"
        ),
        FeedPostData(
            author = "Sarah Chen",
            time = "4 hours ago",
            tag = "Post-Workout Fuel",
            likes = 89,
            comments = 5,
            description = "High protein, high micronutrients. This salmon bowl is exactly what the body needs after leg day. \uD83C\uDF63\uD83D\uDCAA #NutritionMatters"
        )
    )
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // TODO: Load real data from Firestore
}