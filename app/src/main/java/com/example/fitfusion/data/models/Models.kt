package com.example.fitfusion.data.models

data class ExerciseCatalogItem(
    val documentId: String,
    val exerciseId: String,
    val slug: String,
    val name: String,
    val nameLower: String,
    val difficultyLevel: String?,
    val muscleGroup: String?,
    val primeMoverMuscle: String?,
    val secondaryMuscle: String?,
    val primaryEquipment: String?,
    val secondaryEquipment: String?,
    val posture: String?,
    val shortYoutubeDemoUrl: String?,
    val inDepthYoutubeTechniqueUrl: String?,
) {
    val displayMuscleGroup: String
        get() = sequenceOf(primeMoverMuscle, muscleGroup, secondaryMuscle)
            .mapNotNull { it?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "Other"

    val displayEquipment: String
        get() = sequenceOf(primaryEquipment, secondaryEquipment)
            .mapNotNull { it?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "Bodyweight"
}
