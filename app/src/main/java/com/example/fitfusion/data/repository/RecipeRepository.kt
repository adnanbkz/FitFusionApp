package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.RecipeIngredient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private fun col() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("recipes")
    }

    fun fetchAll(onSuccess: (List<Recipe>) -> Unit, onError: (Exception) -> Unit) {
        col()
            ?.orderBy("createdAt", Query.Direction.DESCENDING)
            ?.get()
            ?.addOnSuccessListener { snap ->
                onSuccess(snap.documents.mapNotNull { it.toRecipe() })
            }
            ?.addOnFailureListener(onError)
            ?: onSuccess(emptyList())
    }

    fun save(recipe: Recipe, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        col()
            ?.document(recipe.id)
            ?.set(recipe.toMap())
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener(onError)
            ?: onError(Exception("Not authenticated"))
    }

    fun delete(recipeId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        col()
            ?.document(recipeId)
            ?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener(onError)
            ?: onError(Exception("Not authenticated"))
    }
}

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toRecipe(): Recipe? {
    val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
    val rawIngredients = get("ingredients") as? List<Map<String, Any>> ?: emptyList()
    val ingredients = rawIngredients.mapNotNull { map ->
        val ingredientId = map["ingredientId"] as? String ?: return@mapNotNull null
        val ingName      = map["name"]          as? String ?: return@mapNotNull null
        RecipeIngredient(
            ingredientId   = ingredientId,
            name           = ingName,
            emoji          = map["emoji"]           as? String ?: "🥗",
            kcalPer100g    = (map["kcalPer100g"]    as? Number)?.toFloat() ?: 0f,
            proteinPer100g = (map["proteinPer100g"] as? Number)?.toFloat() ?: 0f,
            carbsPer100g   = (map["carbsPer100g"]   as? Number)?.toFloat() ?: 0f,
            fatsPer100g    = (map["fatsPer100g"]    as? Number)?.toFloat() ?: 0f,
            servingLabel   = map["servingLabel"]    as? String ?: "100g",
            servingGrams   = (map["servingGrams"]   as? Number)?.toFloat() ?: 100f,
            quantity       = (map["quantity"]       as? Number)?.toInt()   ?: 1,
        )
    }
    return Recipe(
        id          = id,
        name        = name,
        emoji       = getString("emoji") ?: "🍽️",
        ingredients = ingredients,
        createdAt   = getLong("createdAt") ?: System.currentTimeMillis(),
    )
}
