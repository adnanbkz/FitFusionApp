package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Serving
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

data class IngredientPage(
    val ingredients: List<Food>,
    val lastDocument: DocumentSnapshot?,
    val hasMore: Boolean,
)

class IngredientRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun fetchPage(
        searchQuery: String,
        pageSize: Int,
        lastDocument: DocumentSnapshot? = null,
        onSuccess: (IngredientPage) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val normalized = searchQuery.trim().lowercase(Locale.getDefault())

        var query: Query = if (normalized.isBlank()) {
            firestore.collection("ingredients")
                .orderBy("nameLower")
                .limit(pageSize.toLong())
        } else {
            firestore.collection("ingredients")
                .orderBy("nameLower")
                .startAt(normalized)
                .endAt(normalized + "\uf8ff")
                .limit(pageSize.toLong())
        }

        lastDocument?.let { query = query.startAfter(it) }

        query.get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { it.toFood() }
                onSuccess(
                    IngredientPage(
                        ingredients  = items,
                        lastDocument = snapshot.documents.lastOrNull(),
                        hasMore      = snapshot.size() >= pageSize,
                    )
                )
            }
            .addOnFailureListener(onError)
    }
}

internal fun DocumentSnapshot.toFood(): Food? {
    val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
    return Food(
        id             = id,
        name           = name,
        brand          = getString("brand"),
        kcalPer100g    = (getDouble("kcalPer100g")    ?: 0.0).toFloat(),
        proteinPer100g = (getDouble("proteinPer100g") ?: 0.0).toFloat(),
        carbsPer100g   = (getDouble("carbsPer100g")   ?: 0.0).toFloat(),
        fatsPer100g    = (getDouble("fatsPer100g")    ?: 0.0).toFloat(),
        emoji          = getString("emoji") ?: "🥗",
        servingOptions = parseServingOptions(get("servingOptions")),
    )
}

@Suppress("UNCHECKED_CAST")
private fun parseServingOptions(raw: Any?): List<Serving> {
    val list = raw as? List<Map<String, Any>> ?: return listOf(Serving("100g", 100f))
    val parsed = list.mapNotNull { map ->
        val label = map["label"] as? String ?: return@mapNotNull null
        val grams = (map["grams"] as? Number)?.toFloat() ?: return@mapNotNull null
        Serving(label, grams)
    }
    return parsed.ifEmpty { listOf(Serving("100g", 100f)) }
}
