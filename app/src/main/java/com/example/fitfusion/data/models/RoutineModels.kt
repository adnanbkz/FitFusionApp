package com.example.fitfusion.data.models

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class RoutineExercise(
    val exerciseId: String,
    val exerciseName: String,
    val emoji: String = "💪",
    val muscleGroup: String = "",
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val targetWeightKg: Float = 0f,
    val restSeconds: Int = 90,
    val notes: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "exerciseId"     to exerciseId,
        "exerciseName"   to exerciseName,
        "emoji"          to emoji,
        "muscleGroup"    to muscleGroup,
        "targetSets"     to targetSets,
        "targetReps"     to targetReps,
        "targetWeightKg" to targetWeightKg,
        "restSeconds"    to restSeconds,
        "notes"          to notes,
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): RoutineExercise? {
            val id   = map["exerciseId"]   as? String ?: return null
            val name = map["exerciseName"] as? String ?: return null
            return RoutineExercise(
                exerciseId     = id,
                exerciseName   = name,
                emoji          = map["emoji"]       as? String ?: "💪",
                muscleGroup    = map["muscleGroup"] as? String ?: "",
                targetSets     = (map["targetSets"]     as? Number)?.toInt()   ?: 3,
                targetReps     = (map["targetReps"]     as? Number)?.toInt()   ?: 10,
                targetWeightKg = (map["targetWeightKg"] as? Number)?.toFloat() ?: 0f,
                restSeconds    = (map["restSeconds"]    as? Number)?.toInt()   ?: 90,
                notes          = map["notes"] as? String ?: "",
            )
        }
    }
}

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "💪",
    val description: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val estimatedDurationMin: Int? = null,
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isValid: Boolean get() = name.isNotBlank() && exercises.isNotEmpty()

    fun toMap(): Map<String, Any?> = mapOf(
        "id"                   to id,
        "name"                 to name,
        "emoji"                to emoji,
        "description"          to description,
        "exercises"            to exercises.map { it.toMap() },
        "estimatedDurationMin" to estimatedDurationMin,
        "isPublic"             to isPublic,
        "authorId"             to authorId,
        "authorName"           to authorName,
        "createdAt"            to createdAt,
    )
}

data class WeeklyRoutinePlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "📅",
    val days: Map<DayOfWeek, String?> = emptyMap(),
    val dayRoutineNames: Map<DayOfWeek, String?> = emptyMap(),
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isValid: Boolean get() = name.isNotBlank() && days.values.any { it != null }
    val activeDays: Int   get() = days.values.count { it != null }

    fun toMap(): Map<String, Any?> = mapOf(
        "id"               to id,
        "name"             to name,
        "emoji"            to emoji,
        "days"             to days.mapKeys { it.key.name },
        "dayRoutineNames"  to dayRoutineNames.mapKeys { it.key.name },
        "isPublic"         to isPublic,
        "authorId"         to authorId,
        "authorName"       to authorName,
        "createdAt"        to createdAt,
    )
}

data class ScheduledRoutine(
    val date: LocalDate,
    val routineId: String,
    val routineName: String,
    val emoji: String = "💪",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "date"         to date.toString(),
        "routineId"    to routineId,
        "routineName"  to routineName,
        "emoji"        to emoji,
    )
}
