package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.IngredientRepository
import com.example.fitfusion.data.repository.OpenFoodFactsRepository
import com.example.fitfusion.data.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.time.LocalDate

enum class FoodTab { ALIMENTOS, RECETAS }
enum class RecipeSubTab { USUARIOS, MIS_RECETAS }

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
    val recipeSubTab: RecipeSubTab = RecipeSubTab.MIS_RECETAS,
    val myRecipes: List<Recipe> = emptyList(),
    val communityRecipes: List<Recipe> = emptyList(),
    val isLoadingMyRecipes: Boolean = false,
    val isLoadingCommunityRecipes: Boolean = false,
    val selectedRecipe: Recipe? = null,
    val savingCommunityRecipeId: String? = null,
    val recipeFeedback: String? = null,
)

@OptIn(FlowPreview::class)
class AddFoodViewModel : ViewModel() {

    private companion object {
        const val FOOD_SEARCH_DEBOUNCE_MS = 800L
    }

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
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(FOOD_SEARCH_DEBOUNCE_MS)
                .collectLatest { rawQuery ->
                    val query = rawQuery.trim()
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isLoadingSearch = false) }
                        return@collectLatest
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
                        val offDeferred = async {
                            try { OpenFoodFactsRepository.search(query) }
                            catch (_: Exception) { emptyList() }
                        }
                        val firestoreResults = firestoreDeferred.await()
                        val offResults       = offDeferred.await()
                        val merged = firestoreResults + offResults.filter { off ->
                            firestoreResults.none { it.name.equals(off.name, ignoreCase = true) }
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

    fun setRecipeSubTab(tab: RecipeSubTab) {
        _uiState.update { it.copy(recipeSubTab = tab) }
    }

    fun setActiveMealSlot(slot: MealSlot) {
        _uiState.update { it.copy(activeMealSlot = slot, sheetMealSlot = slot) }
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

    fun loadMyRecipes() {
        if (_uiState.value.isLoadingMyRecipes) return
        _uiState.update { it.copy(isLoadingMyRecipes = true) }
        recipeRepository.fetchMine(
            onSuccess = { recipes ->
                _uiState.update { it.copy(myRecipes = recipes, isLoadingMyRecipes = false) }
            },
            onError = {
                _uiState.update { it.copy(isLoadingMyRecipes = false) }
            }
        )
    }

    fun loadCommunityRecipes() {
        if (_uiState.value.isLoadingCommunityRecipes) return
        _uiState.update { it.copy(isLoadingCommunityRecipes = true) }
        recipeRepository.fetchCommunity(
            onSuccess = { recipes ->
                _uiState.update { it.copy(communityRecipes = recipes, isLoadingCommunityRecipes = false) }
            },
            onError = {
                _uiState.update { it.copy(isLoadingCommunityRecipes = false) }
            }
        )
    }

    fun saveCommunityRecipeToMine(recipe: Recipe) {
        if (_uiState.value.savingCommunityRecipeId != null) return
        _uiState.update { it.copy(savingCommunityRecipeId = recipe.id) }
        recipeRepository.saveFromCommunity(
            recipe    = recipe,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        savingCommunityRecipeId = null,
                        recipeFeedback          = "Receta guardada en Mis recetas",
                    )
                }
                loadMyRecipes()
            },
            onError = { e ->
                _uiState.update {
                    it.copy(
                        savingCommunityRecipeId = null,
                        recipeFeedback          = e.message ?: "No se pudo guardar",
                    )
                }
            }
        )
    }

    fun clearRecipeFeedback() {
        _uiState.update { it.copy(recipeFeedback = null) }
    }

    fun openRecipeDetail(recipe: Recipe) {
        _uiState.update { it.copy(selectedRecipe = recipe) }
    }

    fun dismissRecipeDetail() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }
}
