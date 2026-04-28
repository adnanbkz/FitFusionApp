package com.example.fitfusion.data.models

import java.util.UUID

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "🍽️",
    val photoUrl: String? = null,
    val description: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val cookTimeMin: Int? = null,
    val kcal: Int? = null,
    val bestMoment: String? = null,
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isValid: Boolean
        get() = name.isNotBlank()

    fun toMap(): Map<String, Any?> = mapOf(
        "id"           to id,
        "name"         to name,
        "emoji"        to emoji,
        "photoUrl"     to photoUrl,
        "description"  to description,
        "ingredients"  to ingredients,
        "instructions" to instructions,
        "cookTimeMin"  to cookTimeMin,
        "kcal"         to kcal,
        "bestMoment"   to bestMoment,
        "isPublic"     to isPublic,
        "authorId"     to authorId,
        "authorName"   to authorName,
        "createdAt"    to createdAt,
    )
}
