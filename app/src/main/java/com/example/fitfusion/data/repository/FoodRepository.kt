package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object FoodRepository {

    val catalog: List<Food> = listOf(
        Food("1",  "Ensalada de aguacate", null,     160f,  2f,  9f, 15f,  "aguacate",
            listOf(Serving("1 racion (200g)", 200f), Serving("100g", 100f))),
        Food("2",  "Pasta al pesto",       null,     131f,  5f, 25f,  3.5f, "pasta",
            listOf(Serving("1 plato (300g)", 300f),  Serving("100g", 100f))),
        Food("3",  "Yogur griego",         "Fage",    97f, 10f,  4f,  5f,  "yogur",
            listOf(Serving("1 unidad (200g)", 200f), Serving("100g", 100f)), isFavorite = true),
        Food("4",  "Avena con frutas",     null,      71f,  2.5f, 12f, 1.5f, "avena",
            listOf(Serving("1 bol (350g)", 350f),    Serving("100g", 100f)), isFavorite = true),
        Food("5",  "Pechuga de pollo",     null,     165f, 31f,  0f,  3.6f, "pollo",
            listOf(Serving("1 filete (150g)", 150f), Serving("100g", 100f)), isFavorite = true),
        Food("6",  "Huevos revueltos",     null,     148f, 10f,  1f, 11f,  "huevo",
            listOf(Serving("2 huevos (120g)", 120f), Serving("100g", 100f))),
        Food("7",  "Batido de proteinas",  "MyProtein", 120f, 22f, 4f, 1.5f, "batido",
            listOf(Serving("1 batido (330ml)", 200f), Serving("100ml", 60f))),
        Food("8",  "Platano",              null,      89f,  1.1f, 23f, 0.3f, "platano",
            listOf(Serving("1 unidad (120g)", 120f), Serving("100g", 100f))),
        Food("9",  "Brocoli al vapor",     null,      34f,  2.8f,  7f, 0.4f, "brocoli",
            listOf(Serving("1 racion (200g)", 200f), Serving("100g", 100f))),
        Food("10", "Arroz integral",       null,     111f,  2.6f, 23f, 0.9f, "arroz",
            listOf(Serving("1 racion (200g)", 200f), Serving("100g", 100f))),
        Food("11", "Tostada integral",     null,     247f,  8f,  41f, 3.4f, "tostada",
            listOf(Serving("1 rebanada (40g)", 40f), Serving("100g", 100f))),
        Food("12", "Manzana",             null,       52f,  0.3f, 14f, 0.2f, "manzana",
            listOf(Serving("1 unidad (180g)", 180f), Serving("100g", 100f)))
    )

    val favorites: List<Food> get() = catalog.filter { it.isFavorite }
    val recents:   List<Food> get() = catalog.take(4)

    fun search(query: String): List<Food> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return catalog.filter {
            it.name.lowercase().contains(q) || it.brand?.lowercase()?.contains(q) == true
        }
    }

    private val _dayLogs = MutableStateFlow<Map<LocalDate, DayLog>>(emptyMap())
    val dayLogs: StateFlow<Map<LocalDate, DayLog>> = _dayLogs.asStateFlow()

    init { seedMockData() }

    private fun seedMockData() {
        val today     = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val logs      = mutableMapOf<LocalDate, DayLog>()

        fun entry(foodIdx: Int, slot: MealSlot, date: LocalDate) = LoggedFood(
            food     = catalog[foodIdx],
            serving  = catalog[foodIdx].servingOptions[0],
            quantity = 1,
            mealSlot = slot,
            date     = date
        )

        val mon = weekStart
        if (mon.isBefore(today)) {
            logs[mon] = DayLog(mon, MealSlot.DEFAULT, listOf(
                entry(3, MealSlot.BREAKFAST, mon),
                entry(2, MealSlot.BREAKFAST, mon),
                entry(4, MealSlot.LUNCH,     mon),
                entry(8, MealSlot.LUNCH,     mon),
                entry(1, MealSlot.DINNER,    mon)
            ))
        }
        val tue = weekStart.plusDays(1)
        if (tue.isBefore(today)) {
            logs[tue] = DayLog(tue, MealSlot.DEFAULT, listOf(
                entry(5, MealSlot.BREAKFAST, tue),
                entry(0, MealSlot.LUNCH,     tue),
                entry(9, MealSlot.LUNCH,     tue),
                entry(6, MealSlot.DINNER,    tue)
            ))
        }
        val wed = weekStart.plusDays(2)
        if (wed.isBefore(today)) {
            logs[wed] = DayLog(wed, MealSlot.DEFAULT, listOf(
                entry(3,  MealSlot.BREAKFAST, wed),
                entry(10, MealSlot.BREAKFAST, wed),
                entry(4,  MealSlot.LUNCH,     wed),
                entry(8,  MealSlot.LUNCH,     wed),
                entry(0,  MealSlot.DINNER,    wed),
                entry(1,  MealSlot.DINNER,    wed)
            ))
        }
        _dayLogs.value = logs
    }

    fun getDayLog(date: LocalDate): DayLog = _dayLogs.value[date] ?: DayLog(date = date, meals = emptyList())

    fun addFood(loggedFood: LoggedFood) {
        val current = getDayLog(loggedFood.date)
        val meals   = if (current.meals.any { it.id == loggedFood.mealSlot.id }) current.meals
                      else current.meals + loggedFood.mealSlot
        val updated = current.copy(meals = meals, entries = current.entries + loggedFood)
        _dayLogs.value = _dayLogs.value + (loggedFood.date to updated)
    }

    fun removeFood(id: String, date: LocalDate) {
        val current = getDayLog(date)
        val updated = current.copy(entries = current.entries.filter { it.id != id })
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun updateFood(id: String, date: LocalDate, newServing: Serving, newQuantity: Int, newSlot: MealSlot) {
        val current = getDayLog(date)
        val meals   = if (current.meals.any { it.id == newSlot.id }) current.meals
                      else current.meals + newSlot
        val updated = current.copy(
            meals   = meals,
            entries = current.entries.map { entry ->
                if (entry.id == id) entry.copy(serving = newServing, quantity = newQuantity, mealSlot = newSlot)
                else entry
            }
        )
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun addMealToDay(date: LocalDate, meal: MealSlot) {
        val current = getDayLog(date)
        if (current.meals.any { it.id == meal.id }) return
        _dayLogs.value = _dayLogs.value + (date to current.copy(meals = current.meals + meal))
    }

    fun renameMealInDay(date: LocalDate, mealId: String, newName: String) {
        val current = getDayLog(date)
        val updated = current.copy(
            meals   = current.meals.map { if (it.id == mealId) it.copy(name = newName) else it },
            entries = current.entries.map { if (it.mealSlot.id == mealId) it.copy(mealSlot = it.mealSlot.copy(name = newName)) else it }
        )
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun removeMealFromDay(date: LocalDate, mealId: String) {
        val current = getDayLog(date)
        val updated = current.copy(
            meals   = current.meals.filter { it.id != mealId },
            entries = current.entries.filter { it.mealSlot.id != mealId }
        )
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun getWeekSummary(weekStart: LocalDate): WeekSummary {
        val days = (0L..6L).map { getDayLog(weekStart.plusDays(it)) }
        return WeekSummary(weekStart, days)
    }
}
