package com.example.fitfusion.data.models

data class RecipeIngredient(
    val ingredientId: String,
    val name: String,
    val emoji: String,
    val kcalPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatsPer100g: Float,
    val servingLabel: String,
    val servingGrams: Float,
    val quantity: Int = 1,
) {
    val kcal: Int    get() = (kcalPer100g    * servingGrams * quantity / 100f).toInt()
    val protein: Int get() = (proteinPer100g * servingGrams * quantity / 100f).toInt()
    val carbs: Int   get() = (carbsPer100g   * servingGrams * quantity / 100f).toInt()
    val fat: Int     get() = (fatsPer100g    * servingGrams * quantity / 100f).toInt()

    fun toFood() = Food(
        id             = ingredientId,
        name           = name,
        kcalPer100g    = kcalPer100g,
        proteinPer100g = proteinPer100g,
        carbsPer100g   = carbsPer100g,
        fatsPer100g    = fatsPer100g,
        emoji          = emoji,
        servingOptions = listOf(Serving(servingLabel, servingGrams)),
    )

    fun toServing() = Serving(servingLabel, servingGrams)

    fun toMap(): Map<String, Any> = mapOf(
        "ingredientId"    to ingredientId,
        "name"            to name,
        "emoji"           to emoji,
        "kcalPer100g"     to kcalPer100g,
        "proteinPer100g"  to proteinPer100g,
        "carbsPer100g"    to carbsPer100g,
        "fatsPer100g"     to fatsPer100g,
        "servingLabel"    to servingLabel,
        "servingGrams"    to servingGrams,
        "quantity"        to quantity,
    )
}

data class Recipe(
    val id: String      = java.util.UUID.randomUUID().toString(),
    val name: String    = "",
    val emoji: String   = "🍽️",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
) {
    val totalKcal:    Int get() = ingredients.sumOf { it.kcal }
    val totalProtein: Int get() = ingredients.sumOf { it.protein }
    val totalCarbs:   Int get() = ingredients.sumOf { it.carbs }
    val totalFat:     Int get() = ingredients.sumOf { it.fat }
    val isValid: Boolean  get() = name.isNotBlank() && ingredients.isNotEmpty()

    fun toMap(): Map<String, Any> = mapOf(
        "id"          to id,
        "name"        to name,
        "emoji"       to emoji,
        "ingredients" to ingredients.map { it.toMap() },
        "createdAt"   to createdAt,
    )
}
