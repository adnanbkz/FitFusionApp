package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FatSecretRepository
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.IngredientRepository
import com.example.fitfusion.data.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.time.LocalDate

enum class FoodTab { ALIMENTOS, RECETAS }

data class AddFoodUiState(
    val activeTab: FoodTab = FoodTab.ALIMENTOS,
    val activeMealSlot: MealSlot = MealSlot.fromCurrentHour(),
    val availableSlots: List<MealSlot> = MealSlot.DEFAULT,
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val isLoadingSearch: Boolean = false,
    val selectedFood: Food? = null,
    val selectedServing: Serving? = null,
    val quantity: Int = 1,
    val sheetMealSlot: MealSlot = MealSlot.fromCurrentHour(),
    val favorites: List<Food> = emptyList(),
    val recents: List<Food> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val isLoadingRecipes: Boolean = false,
    val selectedRecipe: Recipe? = null,
    val recipeSheetMealSlot: MealSlot = MealSlot.fromCurrentHour(),
)

@OptIn(FlowPreview::class)
class AddFoodViewModel : ViewModel() {

    private val ingredientRepository = IngredientRepository()
    private val recipeRepository     = RecipeRepository()

    private val _uiState = MutableStateFlow(
        AddFoodUiState(
            availableSlots = FoodRepository.getDayLog(LocalDate.now()).meals,
        )
    )
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    init {
        // Sync available meal slots with the current day's DayLog
        viewModelScope.launch {
            FoodRepository.dayLogs.collect { logs ->
                val slots = logs[LocalDate.now()]?.meals ?: MealSlot.DEFAULT
                _uiState.update { it.copy(availableSlots = slots) }
            }
        }
        // Sync recents from FoodRepository
        viewModelScope.launch {
            FoodRepository.recents.collect { recents ->
                _uiState.update { it.copy(recents = recents) }
            }
        }
        // Parallel search: Firestore ingredients + FatSecret
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isLoadingSearch = false) }
                        return@collect
                    }
                    _uiState.update { it.copy(isLoadingSearch = true) }
                    try {
                        val firestoreDeferred = async {
                            suspendCoroutine { cont ->
                                ingredientRepository.fetchPage(
                                    searchQuery = query,
                                    pageSize    = 20,
                                    onSuccess   = { page -> cont.resume(page.ingredients) },
                                    onError     = { cont.resume(emptyList()) },
                                )
                            }
                        }
                        val fatSecretDeferred = async {
                            try { FatSecretRepository.searchFoods(query) } catch (_: Exception) { emptyList() }
                        }
                        val firestoreResults  = firestoreDeferred.await()
                        val fatSecretResults  = fatSecretDeferred.await()
                        val merged = firestoreResults + fatSecretResults.filter { fs ->
                            firestoreResults.none { it.name.equals(fs.name, ignoreCase = true) }
                        }
                        _uiState.update { it.copy(searchResults = merged, isLoadingSearch = false) }
                    } catch (_: Exception) {
                        _uiState.update { it.copy(searchResults = emptyList(), isLoadingSearch = false) }
                    }
                }
        }
    }

    fun setActiveTab(tab: FoodTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setActiveMealSlot(slot: MealSlot) {
        _uiState.update { it.copy(activeMealSlot = slot, sheetMealSlot = slot, recipeSheetMealSlot = slot) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isLoadingSearch = false) }
    }

    fun openSheet(food: Food, preselectedSlot: MealSlot) {
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

    fun selectSheetMealSlot(slot: MealSlot) {
        _uiState.update { it.copy(sheetMealSlot = slot) }
    }

    fun confirmAdd() {
        val state   = _uiState.value
        val food    = state.selectedFood    ?: return
        val serving = state.selectedServing ?: return
        dismissSheet()
        viewModelScope.launch {
            FoodRepository.addFood(
                LoggedFood(
                    food     = food,
                    serving  = serving,
                    quantity = state.quantity,
                    mealSlot = state.sheetMealSlot,
                    date     = LocalDate.now(),
                )
            )
        }
    }

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

    fun openRecipeSheet(recipe: Recipe, preselectedSlot: MealSlot) {
        _uiState.update { it.copy(selectedRecipe = recipe, recipeSheetMealSlot = preselectedSlot) }
    }

    fun dismissRecipeSheet() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }

    fun selectRecipeSheetMealSlot(slot: MealSlot) {
        _uiState.update { it.copy(recipeSheetMealSlot = slot) }
    }

    fun confirmAddRecipe() {
        val state  = _uiState.value
        val recipe = state.selectedRecipe ?: return
        val date   = LocalDate.now()
        dismissRecipeSheet()
        viewModelScope.launch {
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
        }
    }
}
