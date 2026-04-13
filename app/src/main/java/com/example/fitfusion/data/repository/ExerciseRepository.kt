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
                    val documentId = document.id
                    val exerciseId = document.readText("exerciseId").orEmpty().ifBlank { documentId }
                    val slug = document.readText("slug").orEmpty().ifBlank { documentId }
                    val rawName = document.readText("name").orEmpty()
                    val resolvedName = rawName.ifBlank {
                        slug.replace("-", " ")
                            .split(" ")
                            .joinToString(" ") { token ->
                                token.replaceFirstChar { char ->
                                    if (char.isLowerCase()) {
                                        char.titlecase(Locale.getDefault())
                                    } else {
                                        char.toString()
                                    }
                                }
                            }
                    }

                    if (resolvedName.isBlank()) {
                        return@mapNotNull null
                    }

                    ExerciseCatalogItem(
                        documentId = documentId,
                        exerciseId = exerciseId,
                        slug = slug,
                        name = resolvedName,
                        nameLower = document.readText("nameLower") ?: resolvedName.lowercase(Locale.getDefault()),
                        difficultyLevel = document.readText("difficultyLevel"),
                        muscleGroup = document.readText("muscleGroup"),
                        primeMoverMuscle = document.readText("primeMoverMuscle"),
                        secondaryMuscle = document.readText("secondaryMuscle"),
                        primaryEquipment = document.readText("primaryEquipment"),
                        secondaryEquipment = document.readText("secondaryEquipment"),
                        posture = document.readText("posture"),
                        shortYoutubeDemoUrl = document.readText("shortYoutubeDemoUrl"),
                        inDepthYoutubeTechniqueUrl = document.readText("inDepthYoutubeTechniqueUrl"),
                    )
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
}

private fun DocumentSnapshot.readText(field: String): String? {
    val value = get(field) ?: return null
    return when (value) {
        is String -> value.trim().takeIf(String::isNotBlank)
        is Number, is Boolean -> value.toString()
        else -> null
    }
}
