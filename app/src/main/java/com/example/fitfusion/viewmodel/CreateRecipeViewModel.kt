package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.RecipeIngredient
import com.example.fitfusion.data.models.Serving
import com.example.fitfusion.data.repository.IngredientRepository
import com.example.fitfusion.data.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateRecipeUiState(
    val name: String  = "",
    val emoji: String = "🍽️",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val isLoadingResults: Boolean = false,
    // Sheet de añadir ingrediente
    val selectedFood: Food? = null,
    val selectedServing: Serving? = null,
    val ingredientQuantity: Int = 1,
    val isSaving: Boolean = false,
    val saveError: String? = null,
) {
    val isValid: Boolean  get() = name.isNotBlank() && ingredients.isNotEmpty()
    val totalKcal: Int    get() = ingredients.sumOf { it.kcal }
    val totalProtein: Int get() = ingredients.sumOf { it.protein }
    val totalCarbs: Int   get() = ingredients.sumOf { it.carbs }
    val totalFat: Int     get() = ingredients.sumOf { it.fat }
}

@OptIn(FlowPreview::class)
class CreateRecipeViewModel : ViewModel() {

    private val ingredientRepository = IngredientRepository()
    private val recipeRepository     = RecipeRepository()

    private val _uiState = MutableStateFlow(CreateRecipeUiState())
    val uiState: StateFlow<CreateRecipeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isLoadingResults = false) }
                        return@collect
                    }
                    _uiState.update { it.copy(isLoadingResults = true) }
                    ingredientRepository.fetchPage(
                        searchQuery = query,
                        pageSize    = 20,
                        onSuccess   = { page ->
                            _uiState.update { it.copy(searchResults = page.ingredients, isLoadingResults = false) }
                        },
                        onError     = {
                            _uiState.update { it.copy(searchResults = emptyList(), isLoadingResults = false) }
                        }
                    )
                }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, saveError = null) }
    }

    fun onEmojiChange(value: String) {
        _uiState.update { it.copy(emoji = value.takeLast(2).ifBlank { "🍽️" }) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun openIngredientSheet(food: Food) {
        _uiState.update {
            it.copy(
                selectedFood     = food,
                selectedServing  = food.servingOptions.first(),
                ingredientQuantity = 1,
            )
        }
    }

    fun dismissIngredientSheet() {
        _uiState.update { it.copy(selectedFood = null) }
    }

    fun selectServing(serving: Serving) {
        _uiState.update { it.copy(selectedServing = serving) }
    }

    fun incrementQuantity() {
        _uiState.update { it.copy(ingredientQuantity = (it.ingredientQuantity + 1).coerceAtMost(20)) }
    }

    fun decrementQuantity() {
        _uiState.update { it.copy(ingredientQuantity = (it.ingredientQuantity - 1).coerceAtLeast(1)) }
    }

    fun confirmAddIngredient() {
        val state   = _uiState.value
        val food    = state.selectedFood    ?: return
        val serving = state.selectedServing ?: return
        val ingredient = RecipeIngredient(
            ingredientId   = food.id,
            name           = food.name,
            emoji          = food.emoji,
            kcalPer100g    = food.kcalPer100g,
            proteinPer100g = food.proteinPer100g,
            carbsPer100g   = food.carbsPer100g,
            fatsPer100g    = food.fatsPer100g,
            servingLabel   = serving.label,
            servingGrams   = serving.grams,
            quantity       = state.ingredientQuantity,
        )
        _uiState.update { it.copy(
            ingredients = it.ingredients + ingredient,
            selectedFood = null,
            searchQuery  = "",
            searchResults = emptyList(),
        ) }
    }

    fun removeIngredient(ingredientId: String) {
        _uiState.update { it.copy(ingredients = it.ingredients.filter { i -> i.ingredientId != ingredientId }) }
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        val recipe = Recipe(
            name        = state.name.trim(),
            emoji       = state.emoji,
            ingredients = state.ingredients,
        )
        recipeRepository.save(
            recipe    = recipe,
            onSuccess = {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            },
            onError   = { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        )
    }
}
