package com.example.fitfusion.data.models

import java.util.UUID

data class RecipeIngredient(
    val name: String,
    val brand: String? = null,
    val foodId: String? = null,
    val quantityG: Int = 100,
    val kcalPer100g: Float = 0f,
    val proteinPer100g: Float = 0f,
    val carbsPer100g: Float = 0f,
    val fatsPer100g: Float = 0f,
) {
    val totalKcal: Int    get() = (kcalPer100g    * quantityG / 100f).toInt()
    val totalProtein: Int get() = (proteinPer100g * quantityG / 100f).toInt()
    val totalCarbs: Int   get() = (carbsPer100g   * quantityG / 100f).toInt()
    val totalFat: Int     get() = (fatsPer100g    * quantityG / 100f).toInt()

    fun toMap(): Map<String, Any?> = mapOf(
        "name"           to name,
        "brand"          to brand,
        "foodId"         to foodId,
        "quantityG"      to quantityG,
        "kcalPer100g"    to kcalPer100g.toDouble(),
        "proteinPer100g" to proteinPer100g.toDouble(),
        "carbsPer100g"   to carbsPer100g.toDouble(),
        "fatsPer100g"    to fatsPer100g.toDouble(),
    )

    companion object {
        fun fromMap(map: Map<*, *>): RecipeIngredient? {
            val name = map["name"] as? String ?: return null
            return RecipeIngredient(
                name           = name,
                brand          = map["brand"] as? String,
                foodId         = map["foodId"] as? String,
                quantityG      = (map["quantityG"]      as? Number)?.toInt()   ?: 100,
                kcalPer100g    = (map["kcalPer100g"]    as? Number)?.toFloat() ?: 0f,
                proteinPer100g = (map["proteinPer100g"] as? Number)?.toFloat() ?: 0f,
                carbsPer100g   = (map["carbsPer100g"]   as? Number)?.toFloat() ?: 0f,
                fatsPer100g    = (map["fatsPer100g"]    as? Number)?.toFloat() ?: 0f,
            )
        }
    }
}

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val photoUrl: String? = null,
    val description: String = "",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: String = "",
    val cookTimeMin: Int? = null,
    val kcal: Int? = null,
    val bestMoments: List<String> = emptyList(),
    val isPublic: Boolean = false,
    val isDraft: Boolean = false,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isValid: Boolean
        get() = name.isNotBlank()

    val computedKcal: Int
        get() = ingredients.sumOf { it.totalKcal }

    fun toMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "name"           to name,
        "photoUrl"       to photoUrl,
        "description"    to description,
        "ingredients"    to ingredients.map { it.toMap() },
        "instructions"   to instructions,
        "cookTimeMin"    to cookTimeMin,
        "kcal"           to kcal,
        "bestMoments"    to bestMoments,
        "isPublic"       to isPublic,
        "isDraft"        to isDraft,
        "authorId"       to authorId,
        "authorName"     to authorName,
        "createdAt"      to createdAt,
    )
}
