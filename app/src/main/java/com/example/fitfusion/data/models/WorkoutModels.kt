package com.example.fitfusion.data.models

import java.time.LocalDate
import java.util.UUID

data class WorkoutSet(
    val reps: Int,
    val weightKg: Float = 0f,
)

data class WorkoutExercise(
    val name: String,
    val muscleGroup: String,
    val sets: List<WorkoutSet>,
) {
    val totalVolume: Float get() = sets.sumOf { (it.reps * it.weightKg).toDouble() }.toFloat()
    val summary: String get() = "${sets.size}×${sets.firstOrNull()?.reps ?: 0}" +
        if (sets.firstOrNull()?.weightKg ?: 0f > 0f) " · ${sets.first().weightKg.toInt()} kg" else ""
}

data class LoggedWorkout(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val name: String,
    val emoji: String = "🏋️",
    val durationMinutes: Int,
    val kcalBurned: Int,
    val exercises: List<WorkoutExercise> = emptyList(),
)

enum class UserPostType { WORKOUT, NUTRITION }

data class UserPost(
    val id: String            = UUID.randomUUID().toString(),
    val type: UserPostType,
    val caption: String,
    val workoutName: String?  = null,
    val workoutEmoji: String? = null,
    val workoutDurationMinutes: Int? = null,
    val workoutKcal: Int?     = null,
    val workoutVideoUri: String? = null,
    val nutritionPhotoUri: String?       = null,
    val nutritionKcal: Int?              = null,
    val nutritionIngredients: String?    = null,
    val nutritionInstructions: String?   = null,
    val nutritionCookTimeMinutes: Int?   = null,
    val nutritionBestMoment: String?     = null,
    val timestamp: Long       = System.currentTimeMillis(),
)
