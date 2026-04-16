package com.example.fitfusion.data.models

data class ExerciseCatalogItem(
    val documentId: String,
    val exerciseId: String,
    val slug: String,
    val name: String,
    val nameLower: String,
    // Classification
    val difficultyLevel: String?,
    val bodyRegion: String?,
    val muscleGroup: String?,
    val primeMoverMuscle: String?,
    val secondaryMuscle: String?,
    val tertiaryMuscle: String?,
    val mechanics: String?,
    val forceType: String?,
    val laterality: String?,
    val primaryExerciseClassification: String?,
    val isCombinationExercise: Boolean?,
    // Equipment
    val primaryEquipment: String?,
    val secondaryEquipment: String?,
    val loadPosition: String?,
    // Technique
    val posture: String?,
    val footElevation: String?,
    val grip: String?,
    val armMode: String?,
    val armPattern: String?,
    val legPattern: String?,
    // Media
    val shortYoutubeDemoUrl: String?,
    val inDepthYoutubeTechniqueUrl: String?,
) {
    val displayMuscleGroup: String
        get() = sequenceOf(primeMoverMuscle, muscleGroup, secondaryMuscle)
            .mapNotNull { it?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "Other"

    /** Clave usada para los filtros de categoría. Prioriza el grupo amplio (ej. "arms")
     *  sobre el músculo específico (ej. "biceps brachii") para reducir el número de chips. */
    val filterMuscleGroup: String
        get() = sequenceOf(muscleGroup, primeMoverMuscle, secondaryMuscle)
            .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "other"

    val displayEquipment: String
        get() = sequenceOf(primaryEquipment, secondaryEquipment)
            .mapNotNull { it?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "Bodyweight"
}
