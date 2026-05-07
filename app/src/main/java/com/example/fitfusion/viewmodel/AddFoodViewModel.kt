package com.example.fitfusion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.*
import com.example.fitfusion.data.repository.FoodRepository
import com.example.fitfusion.data.repository.IngredientRepository
import com.example.fitfusion.data.repository.OpenFoodFactsRepository
import com.example.fitfusion.data.repository.RecipeRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.util.Locale

enum class FoodTab { ALIMENTOS, RECETAS }
enum class RecipeSubTab { USUARIOS, MIS_RECETAS }

data class AddFoodUiState(
    val activeTab: FoodTab = FoodTab.ALIMENTOS,
    val activeMealSlot: MealSlot = MealSlot.fromCurrentHour(),
    val availableSlots: List<MealSlot> = MealSlot.DEFAULT,
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val isLoadingSearch: Boolean = false,
    val isLoadingMoreSearch: Boolean = false,
    val isLoadingExternalSearch: Boolean = false,
    val externalSearchFailed: Boolean = false,
    val canLoadMoreSearch: Boolean = false,
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
    val scannerOpen: Boolean = false,
    val barcodeLoading: Boolean = false,
    val barcodeNotFound: Boolean = false,
)

@OptIn(FlowPreview::class)
class AddFoodViewModel : ViewModel() {

    private companion object {
        const val TAG = "AddFoodViewModel"
        const val FOOD_SEARCH_DEBOUNCE_MS = 400L
        const val FOOD_SEARCH_PAGE_SIZE = 20
        const val MIN_EXTERNAL_QUERY_LENGTH = 3
        const val MIN_LOCAL_RESULTS_BEFORE_EXTERNAL = 12
        const val EXTERNAL_SEARCH_TIMEOUT_MS = 4_000L
    }

    private val ingredientRepository = IngredientRepository()
    private val recipeRepository     = RecipeRepository()
    private var searchLastDocument: DocumentSnapshot? = null
    private var hasMoreLocalSearch = false
    private var hasMoreExternalSearch = false
    private var externalSearchAttempted = false
    private var externalSearchPage = 0

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
                    resetSearchPaging()
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                searchResults            = emptyList(),
                                isLoadingSearch          = false,
                                isLoadingMoreSearch      = false,
                                isLoadingExternalSearch  = false,
                                externalSearchFailed     = false,
                                canLoadMoreSearch        = false,
                            )
                        }
                        return@collectLatest
                    }
                    _uiState.update {
                        it.copy(
                            isLoadingSearch         = true,
                            isLoadingMoreSearch     = false,
                            isLoadingExternalSearch = false,
                            externalSearchFailed    = false,
                            canLoadMoreSearch       = false,
                        )
                    }
                    try {
                        val page = ingredientRepository.fetchPage(
                            searchQuery = query,
                            pageSize    = FOOD_SEARCH_PAGE_SIZE,
                        )
                        if (!isCurrentSearch(query)) return@collectLatest
                        searchLastDocument = page.lastDocument
                        hasMoreLocalSearch = page.hasMore
                        _uiState.update {
                            it.copy(
                                searchResults       = page.ingredients,
                                isLoadingSearch     = false,
                                canLoadMoreSearch   = canLoadMoreSearch(query),
                            )
                        }
                        if (page.ingredients.size < MIN_LOCAL_RESULTS_BEFORE_EXTERNAL) {
                            fetchExternalSearch(query = query, page = 1, loadingMore = false)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Firestore ingredient search failed for query='$query'", e)
                        if (!isCurrentSearch(query)) return@collectLatest
                        _uiState.update {
                            it.copy(
                                searchResults       = emptyList(),
                                isLoadingSearch     = false,
                                canLoadMoreSearch   = canLoadMoreSearch(query),
                            )
                        }
                        fetchExternalSearch(query = query, page = 1, loadingMore = false)
                    }
                }
        }
    }

    private fun resetSearchPaging() {
        searchLastDocument = null
        hasMoreLocalSearch = false
        hasMoreExternalSearch = false
        externalSearchAttempted = false
        externalSearchPage = 0
    }

    private fun isCurrentSearch(query: String): Boolean =
        _uiState.value.searchQuery.trim() == query

    private fun canLoadMoreSearch(query: String = _uiState.value.searchQuery.trim()): Boolean =
        hasMoreLocalSearch || hasMoreExternalSearch ||
            _uiState.value.externalSearchFailed ||
            (!externalSearchAttempted && query.length >= MIN_EXTERNAL_QUERY_LENGTH)

    private suspend fun fetchExternalSearch(query: String, page: Int, loadingMore: Boolean) {
        if (!isCurrentSearch(query) || query.length < MIN_EXTERNAL_QUERY_LENGTH) return
        externalSearchAttempted = true
        _uiState.update {
            it.copy(
                isLoadingExternalSearch = !loadingMore,
                isLoadingMoreSearch     = loadingMore,
                externalSearchFailed    = false,
            )
        }
        val result = withTimeoutOrNull(EXTERNAL_SEARCH_TIMEOUT_MS) {
            OpenFoodFactsRepository.search(
                query    = query,
                pageSize = FOOD_SEARCH_PAGE_SIZE,
                page     = page,
            )
        }
        if (!isCurrentSearch(query)) return

        val failed = result == null || result.failed
        if (!failed) {
            externalSearchPage = page
            hasMoreExternalSearch = result.hasMore
        }
        val merged = mergeFoodResults(
            existing = _uiState.value.searchResults,
            incoming = result?.foods.orEmpty(),
        )
        _uiState.update {
            it.copy(
                searchResults            = merged,
                isLoadingExternalSearch  = false,
                isLoadingMoreSearch      = false,
                externalSearchFailed     = failed,
                canLoadMoreSearch        = failed || canLoadMoreSearch(query),
            )
        }
    }

    private fun mergeFoodResults(existing: List<Food>, incoming: List<Food>): List<Food> {
        val seenIds = existing.map { it.id }.toMutableSet()
        val seenNames = existing.map { it.searchKey() }.toMutableSet()
        val additions = incoming.filter { food ->
            seenIds.add(food.id) && seenNames.add(food.searchKey())
        }
        return existing + additions
    }

    private fun Food.searchKey(): String =
        listOf(name, brand.orEmpty())
            .joinToString("|") { it.trim().lowercase(Locale.ROOT) }

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
        resetSearchPaging()
        _uiState.update {
            it.copy(
                searchQuery             = query,
                searchResults           = emptyList(),
                isLoadingSearch         = query.isNotBlank(),
                isLoadingMoreSearch     = false,
                isLoadingExternalSearch = false,
                externalSearchFailed    = false,
                canLoadMoreSearch       = false,
            )
        }
    }

    fun clearSearch() {
        resetSearchPaging()
        _uiState.update {
            it.copy(
                searchQuery              = "",
                searchResults            = emptyList(),
                isLoadingSearch          = false,
                isLoadingMoreSearch      = false,
                isLoadingExternalSearch  = false,
                externalSearchFailed     = false,
                canLoadMoreSearch        = false,
            )
        }
    }

    fun loadMoreSearchResults() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        if (query.isBlank() || state.isLoadingSearch || state.isLoadingMoreSearch || state.isLoadingExternalSearch) return

        viewModelScope.launch {
            if (hasMoreLocalSearch) {
                _uiState.update { it.copy(isLoadingMoreSearch = true, externalSearchFailed = false) }
                try {
                    val page = ingredientRepository.fetchPage(
                        searchQuery  = query,
                        pageSize     = FOOD_SEARCH_PAGE_SIZE,
                        lastDocument = searchLastDocument,
                    )
                    if (!isCurrentSearch(query)) return@launch
                    searchLastDocument = page.lastDocument
                    hasMoreLocalSearch = page.hasMore
                    _uiState.update {
                        it.copy(
                            searchResults       = mergeFoodResults(it.searchResults, page.ingredients),
                            isLoadingMoreSearch = false,
                            canLoadMoreSearch   = canLoadMoreSearch(query),
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore ingredient pagination failed for query='$query'", e)
                    hasMoreLocalSearch = false
                    _uiState.update { it.copy(isLoadingMoreSearch = false, canLoadMoreSearch = canLoadMoreSearch(query)) }
                }
                return@launch
            }

            val nextExternalPage = if (externalSearchPage == 0) 1 else externalSearchPage + 1
            fetchExternalSearch(query = query, page = nextExternalPage, loadingMore = true)
        }
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

    fun openScanner() = _uiState.update { it.copy(scannerOpen = true) }

    fun closeScanner() = _uiState.update { it.copy(scannerOpen = false, barcodeNotFound = false) }

    fun clearBarcodeNotFound() = _uiState.update { it.copy(barcodeNotFound = false) }

    fun lookupBarcode(code: String) {
        _uiState.update { it.copy(scannerOpen = false, barcodeLoading = true) }
        viewModelScope.launch {
            val food = OpenFoodFactsRepository.lookupByBarcode(code)
            if (food != null) {
                _uiState.update { it.copy(barcodeLoading = false) }
                openSheet(food, _uiState.value.activeMealSlot)
            } else {
                _uiState.update { it.copy(barcodeLoading = false, barcodeNotFound = true) }
            }
        }
    }

    fun openRecipeDetail(recipe: Recipe) {
        _uiState.update { it.copy(selectedRecipe = recipe) }
    }

    fun dismissRecipeDetail() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }
}
