package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.Exception
import java.util.Locale

data class ExercisePage(
    val exercises: List<ExerciseCatalogItem>,
    val lastDocument: DocumentSnapshot?,
    val hasMore: Boolean,
)

class ExerciseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun fetchExercisePage(
        searchQuery: String,
        pageSize: Int,
        lastDocument: DocumentSnapshot? = null,
        onSuccess: (ExercisePage) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.getDefault())

        var query: Query = if (normalizedQuery.isBlank()) {
            firestore.collection("exercises")
                .orderBy("priority")
                .orderBy("nameLower")
                .limit(pageSize.toLong())
        } else {
            firestore.collection("exercises")
                .orderBy("nameLower")
                .startAt(normalizedQuery)
                .endAt(normalizedQuery + "\uf8ff")
                .limit(pageSize.toLong())
        }

        lastDocument?.let { document ->
            query = query.startAfter(document)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val exercises = snapshot.documents.mapNotNull { document ->
                    document.resolveExerciseItem() ?: return@mapNotNull null
                }
                onSuccess(
                    ExercisePage(
                        exercises = exercises,
                        lastDocument = snapshot.documents.lastOrNull(),
                        hasMore = snapshot.size() >= pageSize,
                    )
                )
            }
            .addOnFailureListener(onError)
    }

    /**
     * Busca todos los ejercicios de una categoría lanzando dos queries en paralelo:
     * una sobre "muscleGroup" y otra sobre "primeMoverMuscle", ya que filterMuscleGroup
     * usa ambos campos como fallback. Los resultados se fusionan y deduplicam por documentId.
     *
     * Los callbacks de Firestore se ejecutan en el hilo principal, por lo que no hay
     * riesgo de concurrencia al modificar collectedDocs.
     */
    fun fetchExercisesForCategory(
        muscleGroupKey: String,
        pageSize: Int,
        onSuccess: (ExercisePage) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val limit = pageSize.toLong()
        val collectedDocs = mutableListOf<DocumentSnapshot>()
        var completedCount = 0
        var firstError: Exception? = null

        fun finish() {
            completedCount++
            if (completedCount < 2) return

            if (collectedDocs.isEmpty() && firstError != null) {
                onError(firstError!!)
                return
            }

            val merged = collectedDocs
                .distinctBy { it.id }
                .sortedBy { (it.get("nameLower") as? String).orEmpty() }
            val page = merged.take(pageSize)
            val exercises = page.mapNotNull { it.resolveExerciseItem() }

            onSuccess(
                ExercisePage(
                    exercises = exercises,
                    lastDocument = page.lastOrNull(),
                    hasMore = merged.size > pageSize,
                )
            )
        }

        listOf("muscleGroup", "primeMoverMuscle").forEach { field ->
            firestore.collection("exercises")
                .whereEqualTo(field, muscleGroupKey)
                .limit(limit)
                .get()
                .addOnSuccessListener { snapshot ->
                    collectedDocs.addAll(snapshot.documents)
                    finish()
                }
                .addOnFailureListener { e ->
                    if (firstError == null) firstError = e
                    finish()
                }
        }
    }

    fun fetchExerciseById(
        documentId: String,
        onSuccess: (ExerciseCatalogItem?) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        firestore.collection("exercises").document(documentId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }
                onSuccess(document.resolveExerciseItem())
            }
            .addOnFailureListener(onError)
    }
}

private fun DocumentSnapshot.resolveExerciseItem(): ExerciseCatalogItem? {
    val documentId = id
    val exerciseId = readText("exerciseId").orEmpty().ifBlank { documentId }
    val slug = readText("slug").orEmpty().ifBlank { documentId }
    val rawName = readText("name").orEmpty()
    val resolvedName = rawName.ifBlank {
        slug.replace("-", " ").split(" ").joinToString(" ") { token ->
            token.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
            }
        }
    }
    if (resolvedName.isBlank()) return null
    return toExerciseCatalogItem(documentId, exerciseId, slug, resolvedName)
}

private fun DocumentSnapshot.readText(field: String): String? {
    val value = get(field) ?: return null
    return when (value) {
        is String -> value.trim().takeIf(String::isNotBlank)
        is Number, is Boolean -> value.toString()
        else -> null
    }
}

private fun DocumentSnapshot.readBool(field: String): Boolean? =
    get(field) as? Boolean

private fun DocumentSnapshot.toExerciseCatalogItem(
    documentId: String,
    exerciseId: String,
    slug: String,
    resolvedName: String,
): ExerciseCatalogItem = ExerciseCatalogItem(
    documentId = documentId,
    exerciseId = exerciseId,
    slug = slug,
    name = resolvedName,
    nameLower = readText("nameLower") ?: resolvedName.lowercase(Locale.getDefault()),
    difficultyLevel = readText("difficultyLevel"),
    bodyRegion = readText("bodyRegion"),
    muscleGroup = readText("muscleGroup"),
    primeMoverMuscle = readText("primeMoverMuscle"),
    secondaryMuscle = readText("secondaryMuscle"),
    tertiaryMuscle = readText("tertiaryMuscle"),
    mechanics = readText("mechanics"),
    forceType = readText("forceType"),
    laterality = readText("laterality"),
    primaryExerciseClassification = readText("primaryExerciseClassification"),
    isCombinationExercise = readBool("isCombinationExercise"),
    primaryEquipment = readText("primaryEquipment"),
    secondaryEquipment = readText("secondaryEquipment"),
    loadPosition = readText("loadPosition"),
    posture = readText("posture"),
    footElevation = readText("footElevation"),
    grip = readText("grip"),
    armMode = readText("armMode"),
    armPattern = readText("armPattern"),
    legPattern = readText("legPattern"),
    shortYoutubeDemoUrl = readText("shortYoutubeDemoUrl"),
    inDepthYoutubeTechniqueUrl = readText("inDepthYoutubeTechniqueUrl"),
)
