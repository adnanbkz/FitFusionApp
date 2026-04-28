package com.example.fitfusion.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreateRecipeUiState(
    val name: String         = "",
    val emoji: String        = "🍽️",
    val description: String  = "",
    val ingredients: String  = "",
    val instructions: String = "",
    val cookTime: String     = "",
    val kcal: String         = "",
    val bestMoment: String?  = null,
    val isPublic: Boolean    = false,
    val photoUri: Uri?       = null,
    val showPhotoOptions: Boolean = false,
    val showCamera: Boolean  = false,
    val isSaving: Boolean    = false,
    val saveError: String?   = null,
) {
    val isValid: Boolean get() = name.isNotBlank()
}

class CreateRecipeViewModel(
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRecipeUiState())
    val uiState: StateFlow<CreateRecipeUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String)         = _uiState.update { it.copy(name = value, saveError = null) }
    fun onEmojiChange(value: String)        = _uiState.update { it.copy(emoji = value.takeLast(2).ifBlank { "🍽️" }) }
    fun onDescriptionChange(value: String)  = _uiState.update { it.copy(description = value) }
    fun onIngredientsChange(value: String)  = _uiState.update { it.copy(ingredients = value) }
    fun onInstructionsChange(value: String) = _uiState.update { it.copy(instructions = value) }
    fun onCookTimeChange(value: String)     = _uiState.update { it.copy(cookTime = value.filter(Char::isDigit).take(4)) }
    fun onKcalChange(value: String)         = _uiState.update { it.copy(kcal = value.filter(Char::isDigit).take(5)) }
    fun onBestMomentChange(value: String?)  = _uiState.update { it.copy(bestMoment = value) }
    fun onPublicToggle(value: Boolean)      = _uiState.update { it.copy(isPublic = value) }

    fun openPhotoOptions()  = _uiState.update { it.copy(showPhotoOptions = true) }
    fun dismissPhotoOptions() = _uiState.update { it.copy(showPhotoOptions = false) }

    fun openCamera() = _uiState.update { it.copy(showPhotoOptions = false, showCamera = true) }
    fun closeCamera() = _uiState.update { it.copy(showCamera = false) }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(photoUri = uri, showPhotoOptions = false, showCamera = false) }
    }

    fun clearPhoto() = _uiState.update { it.copy(photoUri = null) }

    fun saveRecipe(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveError = null) }

        val recipe = Recipe(
            name         = state.name.trim(),
            emoji        = state.emoji,
            description  = state.description.trim(),
            ingredients  = state.ingredients.trim(),
            instructions = state.instructions.trim(),
            cookTimeMin  = state.cookTime.toIntOrNull(),
            kcal         = state.kcal.toIntOrNull(),
            bestMoment   = state.bestMoment,
            isPublic     = state.isPublic,
        )

        val authorName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")

        recipeRepository.save(
            recipe         = recipe,
            localPhotoUri  = state.photoUri,
            authorName     = authorName,
            onSuccess      = {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            },
            onError        = { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Error al guardar") }
            }
        )
    }
}
