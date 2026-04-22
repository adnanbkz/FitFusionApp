package com.example.fitfusion.data.repository

import com.example.fitfusion.viewmodel.ExerciseItem
import com.example.fitfusion.viewmodel.FeedItem
import com.example.fitfusion.viewmodel.NutritionPost
import com.example.fitfusion.viewmodel.WorkoutPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object FeedRepository {

    private val now = System.currentTimeMillis()
    private const val MINUTE = 60_000L
    private const val HOUR   = 60 * MINUTE
    private const val DAY    = 24 * HOUR

    private val seed: List<FeedItem> = listOf(
        FeedItem.Workout(
            WorkoutPost(
                id = "w1",
                author = "Alex Rivera",
                authorInitials = "AR",
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
                timestamp = now - 2 * HOUR,
            )
        ),
        FeedItem.Nutrition(
            NutritionPost(
                id = "n1",
                author = "Marco K.",
                authorInitials = "MK",
                title = "Caprichos nocturnos",
                description = "El combustible perfecto antes de dormir. Dátiles medjool con mantequilla de cacahuete, una pizca de sal marina y 85% de cacao. Alto en fibra, rico en magnesio y energía pura.",
                kcal = 240,
                proteinG = 4,
                carbsG = 32,
                likes = 89,
                comments = 4,
                timestamp = now - 5 * HOUR,
            )
        ),
        FeedItem.Workout(
            WorkoutPost(
                id = "w2",
                author = "Sara Chen",
                authorInitials = "SC",
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
                timestamp = now - 6 * HOUR,
            )
        ),
        FeedItem.Nutrition(
            NutritionPost(
                id = "n2",
                author = "Laura M.",
                authorInitials = "LM",
                title = "Bowl post-entreno",
                description = "Quinoa con pollo a la parrilla, aguacate, brócoli y semillas de sésamo. Recuperación completa en 30 min.",
                kcal = 520,
                proteinG = 38,
                carbsG = 45,
                likes = 142,
                comments = 12,
                timestamp = now - 1 * DAY,
            )
        ),
        FeedItem.Workout(
            WorkoutPost(
                id = "w3",
                author = "David P.",
                authorInitials = "DP",
                workoutType = "Fuerza",
                workoutName = "Push Day",
                durationMin = 52,
                totalWeightKg = 8.420,
                exercises = listOf(
                    ExerciseItem("Press banca", 4, 8),
                    ExerciseItem("Press militar", 4, 10),
                    ExerciseItem("Fondos", 3, 12),
                ),
                likes = 95,
                comments = 7,
                timestamp = now - 2 * DAY,
            )
        ),
    )

    private val _items = MutableStateFlow(seed.sortedByDescending { it.timestamp })
    val items: StateFlow<List<FeedItem>> = _items.asStateFlow()

    fun addItem(item: FeedItem) {
        _items.update { current ->
            (listOf(item) + current).sortedByDescending { it.timestamp }
        }
    }

    fun toggleLike(itemId: String) {
        _items.update { current ->
            current.map { item ->
                when {
                    item is FeedItem.Workout && item.post.id == itemId -> {
                        val p = item.post
                        item.copy(post = p.copy(
                            isLiked = !p.isLiked,
                            likes   = if (p.isLiked) p.likes - 1 else p.likes + 1,
                        ))
                    }
                    item is FeedItem.Nutrition && item.post.id == itemId -> {
                        val p = item.post
                        item.copy(post = p.copy(
                            isLiked = !p.isLiked,
                            likes   = if (p.isLiked) p.likes - 1 else p.likes + 1,
                        ))
                    }
                    else -> item
                }
            }
        }
    }
}
