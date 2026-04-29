package com.example.fitfusion.data.models

import java.time.LocalDate

data class Food(
    val id: String,
    val name: String,
    val brand: String? = null,
    val kcalPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatsPer100g: Float,
    val emoji: String = "🍽️",
    val servingOptions: List<Serving> = listOf(Serving("100g", 100f)),
    val isFavorite: Boolean = false,
    val fatSecretId: String? = null,
)

data class Serving(
    val label: String,
    val grams: Float
)

data class MealSlot(
    val id: String,
    val name: String,
    val isCustom: Boolean = false
) {
    companion object {
        val BREAKFAST       = MealSlot("breakfast",       "Desayuno")
        val MORNING_SNACK   = MealSlot("morning_snack",   "Media mañana")
        val LUNCH           = MealSlot("lunch",           "Almuerzo")
        val AFTERNOON_SNACK = MealSlot("afternoon_snack", "Merienda")
        val DINNER          = MealSlot("dinner",          "Cena")
        val EVENING_SNACK   = MealSlot("evening_snack",   "Snack noche")

        val PREDEFINED = listOf(BREAKFAST, MORNING_SNACK, LUNCH, AFTERNOON_SNACK, DINNER, EVENING_SNACK)
        val DEFAULT    = listOf(BREAKFAST, LUNCH, DINNER)

        fun predefinedById(id: String): MealSlot? = PREDEFINED.find { it.id == id }

        fun fromCurrentHour(): MealSlot {
            val hour = java.time.LocalTime.now().hour
            return when (hour) {
                in 6..9   -> BREAKFAST
                in 10..11 -> MORNING_SNACK
                in 12..15 -> LUNCH
                in 16..18 -> AFTERNOON_SNACK
                in 19..22 -> DINNER
                else      -> EVENING_SNACK
            }
        }
    }
}

data class LoggedFood(
    val id: String = java.util.UUID.randomUUID().toString(),
    val food: Food,
    val serving: Serving,
    val quantity: Int,
    val mealSlot: MealSlot,
    val date: LocalDate = LocalDate.now()
) {
    val kcal:    Int get() = (food.kcalPer100g    * serving.grams * quantity / 100f).toInt()
    val protein: Int get() = (food.proteinPer100g * serving.grams * quantity / 100f).toInt()
    val carbs:   Int get() = (food.carbsPer100g   * serving.grams * quantity / 100f).toInt()
    val fat:     Int get() = (food.fatsPer100g    * serving.grams * quantity / 100f).toInt()
}

data class DayLog(
    val date: LocalDate,
    val meals: List<MealSlot> = MealSlot.DEFAULT,
    val entries: List<LoggedFood> = emptyList(),
    val kcalGoal: Int = 2000
) {
    val totalKcal:    Int get() = entries.sumOf { it.kcal }
    val totalProtein: Int get() = entries.sumOf { it.protein }
    val totalCarbs:   Int get() = entries.sumOf { it.carbs }
    val totalFat:     Int get() = entries.sumOf { it.fat }
    val byMeal: Map<MealSlot, List<LoggedFood>> get() = entries.groupBy { it.mealSlot }
    val progress: Float get() = (totalKcal.toFloat() / kcalGoal).coerceIn(0f, 1f)
    val isOnTrack: Boolean
        get() = entries.isNotEmpty() && totalKcal in (kcalGoal * 0.85).toInt()..(kcalGoal * 1.1).toInt()
}

data class WeekSummary(
    val weekStart: LocalDate,
    val days: List<DayLog>
) {
    val avgKcal: Int
        get() = days.filter { it.entries.isNotEmpty() }
            .map { it.totalKcal }.average()
            .takeIf { !it.isNaN() }?.toInt() ?: 0

    val daysLogged:  Int get() = days.count { it.entries.isNotEmpty() }
    val daysOnTrack: Int get() = days.count { it.isOnTrack }
    val bestDay: DayLog? get() = days.maxByOrNull { it.totalKcal }

    val avgProtein: Int
        get() = days.filter { it.entries.isNotEmpty() }
            .map { it.totalProtein }.average()
            .takeIf { !it.isNaN() }?.toInt() ?: 0
}
