package com.example.fitfusion.data.models

import java.time.LocalDate
import java.util.UUID

data class WorkoutSet(
    val reps: Int,
    val weightKg: Float = 0f,
)

data class WorkoutExercise(
    val exerciseDocumentId: String? = null,
    val exerciseSlug: String? = null,
    val name: String,
    val muscleGroup: String,
    val sets: List<WorkoutSet>,
) {
    val totalVolume: Float get() = sets.sumOf { (it.reps * it.weightKg).toDouble() }.toFloat()
    val totalReps: Int get() = sets.sumOf { it.reps }
    val summary: String
        get() {
            val firstSet = sets.firstOrNull() ?: return "Sin series"
            val allSameReps = sets.all { it.reps == firstSet.reps }
            val allSameWeight = sets.all { it.weightKg == firstSet.weightKg }
            return if (allSameReps && allSameWeight) {
                "${sets.size}×${firstSet.reps}" +
                    if (firstSet.weightKg > 0f) " · ${firstSet.weightKg.toInt()} kg" else ""
            } else {
                "${sets.size} series"
            }
        }
    val setBreakdown: String
        get() = sets.joinToString(" · ") { set ->
            "${set.reps} reps" + if (set.weightKg > 0f) " @ ${set.weightKg.toInt()} kg" else ""
        }
}

data class LoggedWorkout(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val name: String,
    val emoji: String = "🏋️",
    val durationMinutes: Int,
    val kcalBurned: Int,
    val startedAtMs: Long? = null,
    val endedAtMs: Long? = null,
    val createdAtMs: Long? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
) {
    val exerciseCount: Int get() = exercises.size
    val totalSets: Int get() = exercises.sumOf { it.sets.size }
    val totalVolumeKg: Float get() = exercises.sumOf { it.totalVolume.toDouble() }.toFloat()
}

enum class UserPostType { WORKOUT, NUTRITION }

data class UserPost(
    val id: String = UUID.randomUUID().toString(),
    val type: UserPostType,
    val caption: String,
    val workoutName: String? = null,
    val workoutEmoji: String? = null,
    val workoutDurationMinutes: Int? = null,
    val workoutKcal: Int? = null,
    val workoutVideoUri: String? = null,
    val workoutTotalWeightKg: Float? = null,
    val workoutExercises: List<WorkoutExercise> = emptyList(),
    val nutritionPhotoUri: String? = null,
    val nutritionKcal: Int? = null,
    val nutritionIngredients: String? = null,
    val nutritionInstructions: String? = null,
    val nutritionCookTimeMinutes: Int? = null,
    val nutritionBestMoment: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
