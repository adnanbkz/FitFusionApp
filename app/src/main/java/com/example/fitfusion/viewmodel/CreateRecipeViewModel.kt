package com.example.fitfusion.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.RecipeIngredient
import com.example.fitfusion.data.repository.OpenFoodFactsRepository
import com.example.fitfusion.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private val BEST_MOMENTS = listOf("Desayuno", "Almuerzo", "Cena", "Snack", "Pre-entreno", "Post-entreno")

data class CreateRecipeUiState(
    val name: String         = "",
    val description: String  = "",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: String = "",
    val cookTime: String     = "",
    val bestMoments: Set<String>  = emptySet(),
    val isPublic: Boolean    = false,
    val photoUri: Uri?       = null,
    val showPhotoOptions: Boolean = false,
    val showCamera: Boolean  = false,
    val showIngredientPicker: Boolean = false,
    val showDraftDialog: Boolean = false,
    val ingredientQuery: String = "",
    val ingredientResults: List<Food> = emptyList(),
    val isSearchingIngredients: Boolean = false,
    val isSaving: Boolean    = false,
    val saveError: String?   = null,
) {
    val isValid: Boolean get() = name.isNotBlank()
    val totalKcal: Int get() = ingredients.sumOf { it.totalKcal }
    val isDirty: Boolean get() =
        name.isNotBlank() || description.isNotBlank() || ingredients.isNotEmpty() ||
        instructions.isNotBlank() || cookTime.isNotBlank() || bestMoments.isNotEmpty() || photoUri != null
}

class CreateRecipeViewModel(
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val foodRepository = OpenFoodFactsRepository

    private val _uiState = MutableStateFlow(CreateRecipeUiState())
    val uiState: StateFlow<CreateRecipeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    val bestMomentOptions: List<String> = BEST_MOMENTS

    fun onNameChange(value: String)         = _uiState.update { it.copy(name = value, saveError = null) }
    fun onDescriptionChange(value: String)  = _uiState.update { it.copy(description = value) }
    fun onInstructionsChange(value: String) = _uiState.update { it.copy(instructions = value) }
    fun onCookTimeChange(value: String)     = _uiState.update { it.copy(cookTime = value.filter(Char::isDigit).take(4)) }
    fun onPublicToggle(value: Boolean)      = _uiState.update { it.copy(isPublic = value) }

    fun toggleBestMoment(value: String) {
        _uiState.update { state ->
            val updated = state.bestMoments.toMutableSet().apply {
                if (contains(value)) remove(value) else add(value)
            }
            state.copy(bestMoments = updated)
        }
    }

    fun openPhotoOptions()  = _uiState.update { it.copy(showPhotoOptions = true) }
    fun dismissPhotoOptions() = _uiState.update { it.copy(showPhotoOptions = false) }

    fun openCamera() = _uiState.update { it.copy(showPhotoOptions = false, showCamera = true) }
    fun closeCamera() = _uiState.update { it.copy(showCamera = false) }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(photoUri = uri, showPhotoOptions = false, showCamera = false) }
    }

    fun clearPhoto() = _uiState.update { it.copy(photoUri = null) }

    fun openIngredientPicker() = _uiState.update {
        it.copy(showIngredientPicker = true, ingredientQuery = "", ingredientResults = emptyList())
    }
    fun dismissIngredientPicker() = _uiState.update {
        it.copy(showIngredientPicker = false, ingredientQuery = "", ingredientResults = emptyList())
    }

    fun onIngredientQueryChange(query: String) {
        _uiState.update { it.copy(ingredientQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(ingredientResults = emptyList(), isSearchingIngredients = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isSearchingIngredients = true) }
            val result = runCatching { foodRepository.search(query, pageSize = 20) }
            result.onSuccess { searchResult ->
                _uiState.update {
                    it.copy(
                        ingredientResults = searchResult.foods,
                        isSearchingIngredients = false,
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isSearchingIngredients = false) }
            }
        }
    }

    fun addIngredient(food: Food, quantityG: Int) {
        val ingredient = RecipeIngredient(
            name           = food.name,
            brand          = food.brand,
            foodId         = food.id,
            quantityG      = quantityG.coerceAtLeast(1),
            kcalPer100g    = food.kcalPer100g,
            proteinPer100g = food.proteinPer100g,
            carbsPer100g   = food.carbsPer100g,
            fatsPer100g    = food.fatsPer100g,
        )
        _uiState.update {
            it.copy(
                ingredients = it.ingredients + ingredient,
                showIngredientPicker = false,
                ingredientQuery = "",
                ingredientResults = emptyList(),
            )
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            val list = it.ingredients.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(ingredients = list)
        }
    }

    fun updateIngredientQuantity(index: Int, quantityG: Int) {
        _uiState.update {
            val list = it.ingredients.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(quantityG = quantityG.coerceAtLeast(1))
            }
            it.copy(ingredients = list)
        }
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        persist(isDraft = false, onSuccess = onSuccess)
    }

    fun saveAsDraft(onSuccess: () -> Unit) {
        persist(isDraft = true, onSuccess = onSuccess)
    }

    fun requestExitWithDirtyCheck(onCleanExit: () -> Unit) {
        if (_uiState.value.isDirty) {
            _uiState.update { it.copy(showDraftDialog = true) }
        } else {
            onCleanExit()
        }
    }

    fun dismissDraftDialog() = _uiState.update { it.copy(showDraftDialog = false) }

    private fun persist(isDraft: Boolean, onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveError = null, showDraftDialog = false) }

        val recipe = Recipe(
            name         = state.name.trim(),
            description  = state.description.trim(),
            ingredients  = state.ingredients,
            instructions = state.instructions.trim(),
            cookTimeMin  = state.cookTime.toIntOrNull(),
            kcal         = state.totalKcal.takeIf { it > 0 },
            bestMoments  = state.bestMoments.toList(),
            isPublic     = state.isPublic && !isDraft,
            isDraft      = isDraft,
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
