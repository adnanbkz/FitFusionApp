package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Serving
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

object FatSecretRepository {

    private val functions = FirebaseFunctions.getInstance()

    suspend fun searchFoods(query: String, maxResults: Int = 20): List<Food> {
        val result = functions
            .getHttpsCallable("searchFoods")
            .call(mapOf("query" to query, "maxResults" to maxResults))
            .await()

        @Suppress("UNCHECKED_CAST")
        val rawFoods = (result.data as? Map<String, Any>)
            ?.get("foods") as? List<Map<String, Any>>
            ?: return emptyList()

        return rawFoods.mapNotNull { it.toFood() }
    }

    suspend fun getFoodDetail(fatSecretId: String): Food? {
        val result = functions
            .getHttpsCallable("getFoodDetail")
            .call(mapOf("foodId" to fatSecretId))
            .await()

        @Suppress("UNCHECKED_CAST")
        val raw = result.data as? Map<String, Any> ?: return null
        return raw.toFood()
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.toFood(): Food? {
        val name = (this["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
        val fatSecretId   = (this["fatSecretId"] as? String) ?: return null
        val servingLabel  = (this["servingLabel"] as? String) ?: "100g"
        val servingGrams  = (this["servingGrams"] as? Number)?.toFloat() ?: 100f
        return Food(
            id             = "fs_$fatSecretId",
            fatSecretId    = fatSecretId,
            name           = name,
            brand          = this["brand"] as? String,
            kcalPer100g    = (this["kcalPer100g"]    as? Number)?.toFloat() ?: 0f,
            proteinPer100g = (this["proteinPer100g"] as? Number)?.toFloat() ?: 0f,
            carbsPer100g   = (this["carbsPer100g"]   as? Number)?.toFloat() ?: 0f,
            fatsPer100g    = (this["fatsPer100g"]    as? Number)?.toFloat() ?: 0f,
            emoji          = "🍽️",
            servingOptions = listOf(Serving(servingLabel, servingGrams), Serving("100g", 100f))
                .distinctBy { it.label },
        )
    }
}
