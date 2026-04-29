package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseItem(
    val name: String,
    val sets: Int,
    val reps: Int,
)

data class WorkoutPost(
    val id: String,
    val author: String,
    val authorInitials: String,
    val workoutType: String,
    val workoutName: String,
    val durationMin: Int,
    val kcal: Int,
    val totalWeightKg: Double,
    val exercises: List<ExerciseItem>,
    val videoUri: String? = null,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
    val timestamp: Long,
) {
    val timeAgo: String get() = formatTimeAgo(timestamp)
}

data class NutritionPost(
    val id: String,
    val author: String,
    val authorInitials: String,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val kcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val cookTimeMinutes: Int = 0,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
    val timestamp: Long,
) {
    val timeAgo: String get() = formatTimeAgo(timestamp)
}

sealed class FeedItem {
    abstract val timestamp: Long
    data class Workout(val post: WorkoutPost) : FeedItem() {
        override val timestamp: Long get() = post.timestamp
    }
    data class Nutrition(val post: NutritionPost) : FeedItem() {
        override val timestamp: Long get() = post.timestamp
    }
}

enum class FeedFilter { ALL, WORKOUTS, NUTRITION }

fun UserPost.toFeedItem(authorName: String): FeedItem {
    val initials = authorName.split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "Y" }

    return when (type) {
        UserPostType.WORKOUT -> FeedItem.Workout(
            WorkoutPost(
                id             = id,
                author         = authorName,
                authorInitials = initials,
                workoutType    = "Entreno",
                workoutName    = workoutName ?: caption,
                durationMin    = workoutDurationMinutes ?: 0,
                kcal           = workoutKcal ?: 0,
                totalWeightKg  = 0.0,
                exercises      = emptyList(),
                videoUri       = workoutVideoUri,
                likes          = 0,
                comments       = 0,
                timestamp      = timestamp,
            )
        )
        UserPostType.NUTRITION -> FeedItem.Nutrition(
            NutritionPost(
                id             = id,
                author         = authorName,
                authorInitials = initials,
                imageUrl       = nutritionPhotoUri,
                title          = caption,
                description    = nutritionIngredients ?: nutritionInstructions ?: "",
                kcal           = nutritionKcal ?: 0,
                proteinG       = 0,
                carbsG         = 0,
                cookTimeMinutes = nutritionCookTimeMinutes ?: 0,
                likes          = 0,
                comments       = 0,
                timestamp      = timestamp,
            )
        )
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1   -> "Ahora"
        minutes < 60  -> "Hace ${minutes}min"
        hours < 24    -> "Hace ${hours}h"
        days < 7      -> "Hace ${days}d"
        else          -> "Hace ${days / 7}sem"
    }
}

data class FeedUiState(
    val items: List<FeedItem> = emptyList(),
    val filter: FeedFilter = FeedFilter.ALL,
) {
    val filteredItems: List<FeedItem> get() = when (filter) {
        FeedFilter.ALL       -> items
        FeedFilter.WORKOUTS  -> items.filterIsInstance<FeedItem.Workout>()
        FeedFilter.NUTRITION -> items.filterIsInstance<FeedItem.Nutrition>()
    }
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            FeedRepository.items.collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun toggleLike(itemId: String) {
        FeedRepository.toggleLike(itemId)
    }

    fun setFilter(filter: FeedFilter) {
        _uiState.update { it.copy(filter = filter) }
    }
}
