package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.IngredientRepository
import com.example.fitfusion.data.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FoodTab { ALIMENTOS, RECETAS }

data class AddFoodUiState(
    val activeTab: FoodTab = FoodTab.ALIMENTOS,
    val activeMealSlot: MealSlotType = MealSlotType.fromCurrentHour(),
    val mealConfig: MealConfig = MealConfig(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    // BottomSheet alimento individual
    val selectedFood: Food? = null,
    val selectedServing: Serving? = null,
    val quantity: Int = 1,
    val sheetMealSlot: MealSlotType = MealSlotType.fromCurrentHour(),
    // Listas rápidas (catálogo local de fallback)
    val favorites: List<Food> = emptyList(),
    val recents: List<Food> = emptyList(),
    // Recetas
    val recipes: List<Recipe> = emptyList(),
    val isLoadingRecipes: Boolean = false,
    val selectedRecipe: Recipe? = null,
    val recipeSheetMealSlot: MealSlotType = MealSlotType.fromCurrentHour(),
)

@OptIn(FlowPreview::class)
class AddFoodViewModel : ViewModel() {

    private val ingredientRepository = IngredientRepository()
    private val recipeRepository     = RecipeRepository()

    private val _uiState = MutableStateFlow(
        AddFoodUiState(
            favorites  = FoodRepository.favorites,
            recents    = FoodRepository.recents,
            mealConfig = FoodRepository.mealConfig.value,
        )
    )
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            FoodRepository.mealConfig.collect { config ->
                _uiState.update { it.copy(mealConfig = config) }
            }
        }
        // Búsqueda con debounce → Firestore
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList()) }
                        return@collect
                    }
                    ingredientRepository.fetchPage(
                        searchQuery = query,
                        pageSize    = 20,
                        onSuccess   = { page ->
                            _uiState.update { it.copy(searchResults = page.ingredients) }
                        },
                        onError     = {
                            _uiState.update { it.copy(searchResults = emptyList()) }
                        }
                    )
                }
        }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    fun setActiveTab(tab: FoodTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ── Alimentos ─────────────────────────────────────────────────────────────

    fun setActiveMealSlot(slot: MealSlotType) {
        _uiState.update { it.copy(activeMealSlot = slot, sheetMealSlot = slot, recipeSheetMealSlot = slot) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun openSheet(food: Food, preselectedSlot: MealSlotType) {
        _uiState.update {
            it.copy(
                selectedFood    = food,
                selectedServing = food.servingOptions.first(),
                quantity        = 1,
                sheetMealSlot   = preselectedSlot,
            )
        }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(selectedFood = null) }
    }

    fun selectServing(serving: Serving) {
        _uiState.update { it.copy(selectedServing = serving) }
    }

    fun incrementQuantity() {
        _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(20)) }
    }

    fun decrementQuantity() {
        _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
    }

    fun selectSheetMealSlot(slot: MealSlotType) {
        _uiState.update { it.copy(sheetMealSlot = slot) }
    }

    fun confirmAdd() {
        val state   = _uiState.value
        val food    = state.selectedFood    ?: return
        val serving = state.selectedServing ?: return
        FoodRepository.addFood(
            LoggedFood(
                food     = food,
                serving  = serving,
                quantity = state.quantity,
                mealSlot = state.sheetMealSlot,
                date     = LocalDate.now(),
            )
        )
        dismissSheet()
    }

    // ── Recetas ───────────────────────────────────────────────────────────────

    fun loadRecipes() {
        if (_uiState.value.isLoadingRecipes) return
        _uiState.update { it.copy(isLoadingRecipes = true) }
        recipeRepository.fetchAll(
            onSuccess = { recipes ->
                _uiState.update { it.copy(recipes = recipes, isLoadingRecipes = false) }
            },
            onError = {
                _uiState.update { it.copy(isLoadingRecipes = false) }
            }
        )
    }

    fun openRecipeSheet(recipe: Recipe, preselectedSlot: MealSlotType) {
        _uiState.update { it.copy(selectedRecipe = recipe, recipeSheetMealSlot = preselectedSlot) }
    }

    fun dismissRecipeSheet() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }

    fun selectRecipeSheetMealSlot(slot: MealSlotType) {
        _uiState.update { it.copy(recipeSheetMealSlot = slot) }
    }

    fun confirmAddRecipe() {
        val state  = _uiState.value
        val recipe = state.selectedRecipe ?: return
        val date   = LocalDate.now()
        recipe.ingredients.forEach { ingredient ->
            FoodRepository.addFood(
                LoggedFood(
                    food     = ingredient.toFood(),
                    serving  = ingredient.toServing(),
                    quantity = ingredient.quantity,
                    mealSlot = state.recipeSheetMealSlot,
                    date     = date,
                )
            )
        }
        dismissRecipeSheet()
    }
}
