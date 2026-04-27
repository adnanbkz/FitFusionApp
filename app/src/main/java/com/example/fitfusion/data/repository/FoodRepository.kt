package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

private const val FOOD_LOGS_COLLECTION = "foodLogs"
private const val USERS_COLLECTION     = "users"

object FoodRepository {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _dayLogs = MutableStateFlow<Map<LocalDate, DayLog>>(emptyMap())
    val dayLogs: StateFlow<Map<LocalDate, DayLog>> = _dayLogs.asStateFlow()

    private val _recents = MutableStateFlow<List<Food>>(emptyList())
    val recents: StateFlow<List<Food>> = _recents.asStateFlow()

    val favorites: List<Food> get() = emptyList()

    // Custom meal slots added via UI but with no food entries yet (in-memory, lost on restart)
    private val customMealsByDate = mutableMapOf<LocalDate, MutableList<MealSlot>>()

    // Last snapshot of raw entries used to rebuild DayLogs when custom meals change
    private var rawEntries: List<LoggedFood> = emptyList()

    private var listenerRegistration: ListenerRegistration? = null
    private var currentUid: String? = null
    private var authListenerRegistered = false

    private val authListener = FirebaseAuth.AuthStateListener { fbAuth ->
        attachFoodLogListener(fbAuth.currentUser?.uid)
    }

    init { ensureInitialized() }

    fun ensureInitialized() {
        if (authListenerRegistered) return
        auth.addAuthStateListener(authListener)
        authListenerRegistered = true
        attachFoodLogListener(auth.currentUser?.uid)
    }

    private fun attachFoodLogListener(uid: String?) {
        if (uid == currentUid && listenerRegistration != null) return
        listenerRegistration?.remove()
        listenerRegistration = null
        currentUid = uid

        if (uid.isNullOrBlank()) {
            rawEntries = emptyList()
            customMealsByDate.clear()
            _dayLogs.value = emptyMap()
            _recents.value = emptyList()
            return
        }

        listenerRegistration = firestore.collection(USERS_COLLECTION).document(uid)
            .collection(FOOD_LOGS_COLLECTION)
            .orderBy("createdAtMs", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                rawEntries = snapshot.documents.mapNotNull { it.toLoggedFoodOrNull() }
                rebuildDayLogs()
            }
    }

    private fun rebuildDayLogs() {
        val byDate = rawEntries.groupBy { it.date }
        val allDates = (byDate.keys + customMealsByDate.keys).toSet()
        _dayLogs.value = allDates.associateWith { date ->
            val entries    = byDate[date] ?: emptyList()
            val entrySlots = entries.map { it.mealSlot }.distinctBy { it.id }
            val customSlots = customMealsByDate[date] ?: emptyList()
            val meals      = (entrySlots + customSlots).distinctBy { it.id }
                .ifEmpty { MealSlot.DEFAULT }
            DayLog(date = date, meals = meals, entries = entries)
        }
        val seen = mutableSetOf<String>()
        _recents.value = rawEntries.mapNotNull { lf ->
            if (seen.add(lf.food.name.lowercase())) lf.food else null
        }.take(4)
    }

    fun getDayLog(date: LocalDate): DayLog =
        _dayLogs.value[date] ?: DayLog(date = date, meals = MealSlot.DEFAULT)

    suspend fun addFood(loggedFood: LoggedFood) {
        ensureInitialized()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION).document(uid)
            .collection(FOOD_LOGS_COLLECTION)
            .document(loggedFood.id)
            .set(loggedFood.toFirestoreMap())
            .await()
        pushFoodSummary(loggedFood.date)
    }

    suspend fun removeFood(id: String, date: LocalDate) {
        ensureInitialized()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION).document(uid)
            .collection(FOOD_LOGS_COLLECTION)
            .document(id)
            .delete()
            .await()
        pushFoodSummary(date)
    }

    suspend fun updateFood(
        id: String,
        date: LocalDate,
        newServing: Serving,
        newQuantity: Int,
        newSlot: MealSlot,
    ) {
        ensureInitialized()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION).document(uid)
            .collection(FOOD_LOGS_COLLECTION)
            .document(id)
            .update(mapOf(
                "servingLabel"    to newServing.label,
                "servingGrams"    to newServing.grams.toDouble(),
                "quantity"        to newQuantity,
                "mealSlotId"      to newSlot.id,
                "mealSlotName"    to newSlot.name,
                "mealSlotIsCustom" to newSlot.isCustom,
            ))
            .await()
        pushFoodSummary(date)
    }

    fun addMealToDay(date: LocalDate, meal: MealSlot) {
        val meals = customMealsByDate.getOrPut(date) { mutableListOf() }
        if (meals.none { it.id == meal.id }) {
            meals.add(meal)
            rebuildDayLogs()
        }
    }

    fun removeMealFromDay(date: LocalDate, mealId: String) {
        customMealsByDate[date]?.removeAll { it.id == mealId }
        rebuildDayLogs()
    }

    fun renameMealInDay(date: LocalDate, mealId: String, newName: String) {
        val meals = customMealsByDate[date] ?: return
        val idx   = meals.indexOfFirst { it.id == mealId }
        if (idx >= 0) meals[idx] = meals[idx].copy(name = newName)
        rebuildDayLogs()
    }

    private fun pushFoodSummary(date: LocalDate) {
        val dayLog = getDayLog(date)
        CoroutineScope(Dispatchers.IO).launch {
            DailySummaryRepository.mergeFoodSummary(
                date         = date,
                kcalConsumed = dayLog.totalKcal,
                proteinG     = dayLog.totalProtein,
                carbsG       = dayLog.totalCarbs,
                fatG         = dayLog.totalFat,
            )
        }
    }

    fun getWeekSummary(weekStart: LocalDate): WeekSummary {
        val days = (0L..6L).map { getDayLog(weekStart.plusDays(it)) }
        return WeekSummary(weekStart, days)
    }

    // ─── Firestore serialization ─────────────────────────────────────────────

    private fun LoggedFood.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"              to id,
        "date"            to date.toString(),
        "mealSlotId"      to mealSlot.id,
        "mealSlotName"    to mealSlot.name,
        "mealSlotIsCustom" to mealSlot.isCustom,
        "quantity"        to quantity,
        "servingLabel"    to serving.label,
        "servingGrams"    to serving.grams.toDouble(),
        "foodId"          to food.id,
        "fatSecretId"     to food.fatSecretId,
        "foodName"        to food.name,
        "foodBrand"       to food.brand,
        "emoji"           to food.emoji,
        "kcalPer100g"     to food.kcalPer100g.toDouble(),
        "proteinPer100g"  to food.proteinPer100g.toDouble(),
        "carbsPer100g"    to food.carbsPer100g.toDouble(),
        "fatsPer100g"     to food.fatsPer100g.toDouble(),
        "createdAtMs"     to System.currentTimeMillis(),
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toLoggedFoodOrNull(): LoggedFood? {
        val foodName  = getString("foodName")?.takeIf { it.isNotBlank() } ?: return null
        val dateStr   = getString("date") ?: return null
        val date      = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
        val mealSlotId   = getString("mealSlotId") ?: return null
        val mealSlotName = getString("mealSlotName") ?: return null
        val mealSlot  = MealSlot.predefinedById(mealSlotId)
            ?: MealSlot(mealSlotId, mealSlotName, getBoolean("mealSlotIsCustom") ?: false)
        val serving   = Serving(
            label = getString("servingLabel") ?: "100g",
            grams = (getDouble("servingGrams") ?: 100.0).toFloat(),
        )
        val food = Food(
            id             = getString("foodId") ?: id,
            fatSecretId    = getString("fatSecretId"),
            name           = foodName,
            brand          = getString("foodBrand"),
            emoji          = getString("emoji") ?: "🍽️",
            kcalPer100g    = (getDouble("kcalPer100g")    ?: 0.0).toFloat(),
            proteinPer100g = (getDouble("proteinPer100g") ?: 0.0).toFloat(),
            carbsPer100g   = (getDouble("carbsPer100g")   ?: 0.0).toFloat(),
            fatsPer100g    = (getDouble("fatsPer100g")    ?: 0.0).toFloat(),
            servingOptions = listOf(serving, Serving("100g", 100f)).distinctBy { it.label },
        )
        return LoggedFood(
            id       = id,
            food     = food,
            serving  = serving,
            quantity = (getLong("quantity") ?: 1L).toInt(),
            mealSlot = mealSlot,
            date     = date,
        )
    }
}
