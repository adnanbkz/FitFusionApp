package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object FoodRepository {

    // ── Catálogo ─────────────────────────────────────────────────────────────

    val catalog: List<Food> = listOf(
        Food("1", "Ensalada de aguacate", null, 160f, 2f, 9f, 15f, "🥗",
            listOf(Serving("1 ración (200g)", 200f), Serving("100g", 100f))),
        Food("2", "Pasta al pesto", null, 131f, 5f, 25f, 3.5f, "🍝",
            listOf(Serving("1 plato (300g)", 300f), Serving("100g", 100f))),
        Food("3", "Yogur griego", "Fage", 97f, 10f, 4f, 5f, "🥛",
            listOf(Serving("1 unidad (200g)", 200f), Serving("100g", 100f)), isFavorite = true),
        Food("4", "Avena con frutas", null, 71f, 2.5f, 12f, 1.5f, "🫐",
            listOf(Serving("1 bol (350g)", 350f), Serving("100g", 100f)), isFavorite = true),
        Food("5", "Pechuga de pollo", null, 165f, 31f, 0f, 3.6f, "🍗",
            listOf(Serving("1 filete (150g)", 150f), Serving("100g", 100f)), isFavorite = true),
        Food("6", "Huevos revueltos", null, 148f, 10f, 1f, 11f, "🥚",
            listOf(Serving("2 huevos (120g)", 120f), Serving("100g", 100f))),
        Food("7", "Batido de proteínas", "MyProtein", 120f, 22f, 4f, 1.5f, "🥤",
            listOf(Serving("1 batido (330ml)", 200f), Serving("100ml", 60f))),
        Food("8", "Plátano", null, 89f, 1.1f, 23f, 0.3f, "🍌",
            listOf(Serving("1 unidad (120g)", 120f), Serving("100g", 100f))),
        Food("9", "Brócoli al vapor", null, 34f, 2.8f, 7f, 0.4f, "🥦",
            listOf(Serving("1 ración (200g)", 200f), Serving("100g", 100f))),
        Food("10", "Arroz integral", null, 111f, 2.6f, 23f, 0.9f, "🍚",
            listOf(Serving("1 ración (200g)", 200f), Serving("100g", 100f))),
        Food("11", "Tostada integral", null, 247f, 8f, 41f, 3.4f, "🍞",
            listOf(Serving("1 rebanada (40g)", 40f), Serving("100g", 100f))),
        Food("12", "Manzana", null, 52f, 0.3f, 14f, 0.2f, "🍎",
            listOf(Serving("1 unidad (180g)", 180f), Serving("100g", 100f)))
    )

    val favorites: List<Food> get() = catalog.filter { it.isFavorite }
    val recents: List<Food>   get() = catalog.take(4)

    fun search(query: String): List<Food> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return catalog.filter {
            it.name.lowercase().contains(q) ||
                it.brand?.lowercase()?.contains(q) == true
        }
    }

    // ── Estado compartido ─────────────────────────────────────────────────────

    private val _dayLogs = MutableStateFlow<Map<LocalDate, DayLog>>(emptyMap())
    val dayLogs: StateFlow<Map<LocalDate, DayLog>> = _dayLogs.asStateFlow()

    private val _mealConfig = MutableStateFlow(MealConfig())
    val mealConfig: StateFlow<MealConfig> = _mealConfig.asStateFlow()

    init { seedMockData() }

    private fun seedMockData() {
        val today     = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val logs      = mutableMapOf<LocalDate, DayLog>()

        fun entry(foodIdx: Int, slot: MealSlotType, date: LocalDate) = LoggedFood(
            food     = catalog[foodIdx],
            serving  = catalog[foodIdx].servingOptions[0],
            quantity = 1,
            mealSlot = slot,
            date     = date
        )

        // Lunes
        val mon = weekStart
        if (mon.isBefore(today)) {
            logs[mon] = DayLog(mon, listOf(
                entry(3, MealSlotType.BREAKFAST, mon),
                entry(2, MealSlotType.BREAKFAST, mon),
                entry(4, MealSlotType.LUNCH,     mon),
                entry(8, MealSlotType.LUNCH,     mon),
                entry(1, MealSlotType.DINNER,    mon)
            ))
        }
        // Martes
        val tue = weekStart.plusDays(1)
        if (tue.isBefore(today)) {
            logs[tue] = DayLog(tue, listOf(
                entry(5, MealSlotType.BREAKFAST, tue),
                entry(0, MealSlotType.LUNCH,     tue),
                entry(9, MealSlotType.LUNCH,     tue),
                entry(6, MealSlotType.DINNER,    tue)
            ))
        }
        // Miércoles
        val wed = weekStart.plusDays(2)
        if (wed.isBefore(today)) {
            logs[wed] = DayLog(wed, listOf(
                entry(3, MealSlotType.BREAKFAST, wed),
                entry(10, MealSlotType.BREAKFAST, wed),
                entry(4, MealSlotType.LUNCH,     wed),
                entry(8, MealSlotType.LUNCH,     wed),
                entry(0, MealSlotType.DINNER,    wed),
                entry(1, MealSlotType.DINNER,    wed)
            ))
        }
        // Hoy: algunos registros para que no esté vacío
        logs[today] = DayLog(today, listOf(
            entry(3, MealSlotType.BREAKFAST, today),
            entry(2, MealSlotType.BREAKFAST, today)
        ))

        _dayLogs.value = logs
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    fun getDayLog(date: LocalDate): DayLog =
        _dayLogs.value[date] ?: DayLog(date = date)

    fun addFood(loggedFood: LoggedFood) {
        val current = getDayLog(loggedFood.date)
        val updated = current.copy(entries = current.entries + loggedFood)
        _dayLogs.value = _dayLogs.value + (loggedFood.date to updated)
    }

    fun removeFood(id: String, date: LocalDate) {
        val current = getDayLog(date)
        val updated = current.copy(entries = current.entries.filter { it.id != id })
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun updateFood(id: String, date: LocalDate, newServing: Serving, newQuantity: Int, newSlot: MealSlotType) {
        val current = getDayLog(date)
        val updated = current.copy(
            entries = current.entries.map { entry ->
                if (entry.id == id) entry.copy(serving = newServing, quantity = newQuantity, mealSlot = newSlot)
                else entry
            }
        )
        _dayLogs.value = _dayLogs.value + (date to updated)
    }

    fun updateMealConfig(config: MealConfig) {
        _mealConfig.value = config
    }

    fun getWeekSummary(weekStart: LocalDate): WeekSummary {
        val days = (0L..6L).map { getDayLog(weekStart.plusDays(it)) }
        return WeekSummary(weekStart, days)
    }
}
