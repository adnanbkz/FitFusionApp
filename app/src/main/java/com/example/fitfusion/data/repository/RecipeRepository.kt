package com.example.fitfusion.data.repository

import android.net.Uri
import com.example.fitfusion.data.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth           = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage     = FirebaseStorage.getInstance(),
) {
    private fun userRecipes() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("recipes")
    }

    private fun communityRecipes() = firestore.collection("recipes")

    fun fetchMine(onSuccess: (List<Recipe>) -> Unit, onError: (Exception) -> Unit) {
        userRecipes()
            ?.orderBy("createdAt", Query.Direction.DESCENDING)
            ?.get()
            ?.addOnSuccessListener { snap ->
                onSuccess(snap.documents.mapNotNull { it.toRecipe() })
            }
            ?.addOnFailureListener(onError)
            ?: onSuccess(emptyList())
    }

    fun fetchCommunity(onSuccess: (List<Recipe>) -> Unit, onError: (Exception) -> Unit) {
        communityRecipes()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snap ->
                onSuccess(snap.documents.mapNotNull { it.toRecipe() })
            }
            .addOnFailureListener(onError)
    }

    fun save(
        recipe: Recipe,
        localPhotoUri: Uri?,
        authorName: String?,
        onSuccess: (Recipe) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError(Exception("No autenticado"))
            return
        }

        val finalize: (String?) -> Unit = { uploadedUrl ->
            val finalRecipe = recipe.copy(
                photoUrl   = uploadedUrl ?: recipe.photoUrl,
                authorId   = uid,
                authorName = authorName,
            )
            persistBothLocations(finalRecipe, onSuccess = { onSuccess(finalRecipe) }, onError = onError)
        }

        if (localPhotoUri != null) {
            val ref = storage.reference.child("recipe_photos/$uid/${recipe.id}.jpg")
            ref.putFile(localPhotoUri)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener { url -> finalize(url.toString()) }
                        .addOnFailureListener(onError)
                }
                .addOnFailureListener(onError)
        } else {
            finalize(null)
        }
    }

    private fun persistBothLocations(
        recipe: Recipe,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val personal = userRecipes()
        if (personal == null) {
            onError(Exception("No autenticado"))
            return
        }
        personal.document(recipe.id).set(recipe.toMap())
            .addOnSuccessListener {
                if (recipe.isPublic) {
                    communityRecipes().document(recipe.id).set(recipe.toMap())
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener(onError)
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener(onError)
    }

    fun saveFromCommunity(recipe: Recipe, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val personal = userRecipes()
        if (personal == null) {
            onError(Exception("No autenticado"))
            return
        }
        val copy = recipe.copy(
            isPublic  = false,
            createdAt = System.currentTimeMillis(),
        )
        personal.document(copy.id).set(copy.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun delete(recipeId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        userRecipes()
            ?.document(recipeId)
            ?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener(onError)
            ?: onError(Exception("No autenticado"))
    }
}

private fun DocumentSnapshot.toRecipe(): Recipe? {
    val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
    return Recipe(
        id           = id,
        name         = name,
        emoji        = getString("emoji") ?: "🍽️",
        photoUrl     = getString("photoUrl"),
        description  = getString("description")  ?: "",
        ingredients  = getString("ingredients")  ?: "",
        instructions = getString("instructions") ?: "",
        cookTimeMin  = getLong("cookTimeMin")?.toInt(),
        kcal         = getLong("kcal")?.toInt(),
        bestMoment   = getString("bestMoment"),
        isPublic     = getBoolean("isPublic") ?: false,
        authorId     = getString("authorId"),
        authorName   = getString("authorName"),
        createdAt    = getLong("createdAt") ?: System.currentTimeMillis(),
    )
}
