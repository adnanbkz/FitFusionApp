package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExerciseItem(
    val name: String,
    val sets: Int,
    val reps: Int,
)

data class WorkoutPost(
    val id: String,
    val author: String,
    val authorInitials: String,
    val timeAgo: String,
    val workoutType: String,
    val workoutName: String,
    val durationMin: Int,
    val totalWeightKg: Double,
    val exercises: List<ExerciseItem>,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
)

data class NutritionPost(
    val id: String,
    val author: String,
    val authorInitials: String,
    val timeAgo: String,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val kcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
)

sealed class FeedItem {
    data class Workout(val post: WorkoutPost) : FeedItem()
    data class Nutrition(val post: NutritionPost) : FeedItem()
}

data class FeedUiState(
    val items: List<FeedItem> = defaultFeed,
)

private val defaultFeed: List<FeedItem> = listOf(
    FeedItem.Workout(
        WorkoutPost(
            id = "w1",
            author = "Alex Rivera",
            authorInitials = "AR",
            timeAgo = "Hace 2 horas",
            workoutType = "Fuerza",
            workoutName = "Lower A",
            durationMin = 44,
            totalWeightKg = 10.735,
            exercises = listOf(
                ExerciseItem("Sentadilla", 4, 8),
                ExerciseItem("Press de Piernas", 3, 12),
                ExerciseItem("Extensión de cuádriceps", 3, 15),
            ),
            likes = 124,
            comments = 18,
        )
    ),
    FeedItem.Nutrition(
        NutritionPost(
            id = "n1",
            author = "Marco K.",
            authorInitials = "MK",
            timeAgo = "Hace 5 horas",
            title = "Caprichos nocturnos",
            description = "El combustible perfecto antes de dormir. Dátiles medjool con mantequilla de cacahuete, una pizca de sal marina y 85% de cacao. Alto en fibra, rico en magnesio y energía pura.",
            kcal = 240,
            proteinG = 4,
            carbsG = 32,
            likes = 89,
            comments = 4,
        )
    ),
    FeedItem.Workout(
        WorkoutPost(
            id = "w2",
            author = "Sara Chen",
            authorInitials = "SC",
            timeAgo = "Hace 6 horas",
            workoutType = "Cardio",
            workoutName = "Morning Run",
            durationMin = 38,
            totalWeightKg = 0.0,
            exercises = listOf(
                ExerciseItem("Carrera continua", 1, 0),
                ExerciseItem("Sprint 200m", 6, 0),
            ),
            likes = 67,
            comments = 9,
        )
    ),
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun toggleLike(itemId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    when {
                        item is FeedItem.Workout && item.post.id == itemId -> {
                            val p = item.post
                            item.copy(post = p.copy(
                                isLiked = !p.isLiked,
                                likes = if (p.isLiked) p.likes - 1 else p.likes + 1,
                            ))
                        }
                        item is FeedItem.Nutrition && item.post.id == itemId -> {
                            val p = item.post
                            item.copy(post = p.copy(
                                isLiked = !p.isLiked,
                                likes = if (p.isLiked) p.likes - 1 else p.likes + 1,
                            ))
                        }
                        else -> item
                    }
                }
            )
        }
    }
}
