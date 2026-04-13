package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FoodItem(
    val id: Int,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val emoji: String = "🥗",
    val tags: List<String> = emptyList()
)

data class SuggestedFoodItem(
    val id: Int,
    val name: String,
    val calories: Int,
    val tag: String,
    val emoji: String,
    val isFeatured: Boolean = false
)

enum class MealType { BREAKFAST, LUNCH, DINNER }

data class AddFoodUiState(
    val searchQuery: String = "",
    val selectedMeal: MealType? = null,
    val recentlyLogged: List<FoodItem> = emptyList(),
    val suggested: List<SuggestedFoodItem> = emptyList(),
    val addedItemIds: Set<Int> = emptySet()
)

class AddFoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddFoodUiState(
            recentlyLogged = listOf(
                FoodItem(1, "Ensalada de aguacate", 342, protein = 8, carbs = 20, fats = 26, emoji = "🥗"),
                FoodItem(2, "Bol de pasta al pesto", 520, protein = 18, carbs = 72, fats = 18, emoji = "🍝")
            ),
            suggested = listOf(
                SuggestedFoodItem(1, "Avena con frutos rojos", 390, "Alto en fibra", "🫐", isFeatured = true),
                SuggestedFoodItem(2, "Batido de proteínas", 210, "Alto en proteínas", "🥤"),
                SuggestedFoodItem(3, "Yogur griego", 150, "Probiótico", "🥛")
            )
        )
    )
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onMealSelected(meal: MealType) {
        _uiState.update {
            it.copy(selectedMeal = if (it.selectedMeal == meal) null else meal)
        }
    }

    fun addFood(foodId: Int) {
        _uiState.update { it.copy(addedItemIds = it.addedItemIds + foodId) }
    }

    fun addSuggested(suggestedId: Int) {
        _uiState.update { it.copy(addedItemIds = it.addedItemIds + (suggestedId + 1000)) }
    }
}