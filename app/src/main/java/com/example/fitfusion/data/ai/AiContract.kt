package com.example.fitfusion.data.ai

import kotlinx.serialization.Serializable

/*
 * Contrato API entre el cliente Android y el backend .NET (ASP.NET Core).
 *
 * Auth: todas las peticiones incluyen `Authorization: Bearer <FirebaseIdToken>`.
 * El backend debe validar el token contra el proyecto Firebase de FitFusion
 * (mismo project_id que las apps Android) y rechazar 401 si no es válido.
 *
 * Content-Type: application/json. UTF-8.
 *
 * Endpoints:
 *
 *   POST {AI_API_BASE_URL}/api/ai/recipe/kcal
 *     Request:  AiRefineRecipeRequest
 *     Response: AiRefineRecipeResponse
 *     Semántica: refina/verifica la suma kcal/macros de una receta dada por sus
 *                ingredientes (cada uno con kcalPer100g y cantidad). El backend
 *                puede consultar Gemini para corregir errores comunes (recetas
 *                cocinadas, agua que se evapora, etc.) y devolver una estimación
 *                más realista.
 *
 *   POST {AI_API_BASE_URL}/api/ai/plate/estimate
 *     Request:  AiEstimatePlateRequest
 *     Response: AiEstimatePlateResponse
 *     Semántica: el usuario describe un plato en texto libre. El backend
 *                devuelve los macros por 100 g + un tamaño de porción razonable.
 *
 *   POST {AI_API_BASE_URL}/api/ai/routine/generate
 *     Request:  AiGenerateRoutineRequest
 *     Response: AiGenerateRoutineResponse
 *     Semántica: genera una rutina con N ejercicios siguiendo objetivos del
 *                usuario. Los nombres de ejercicio deben ser estándar y los
 *                grupos musculares en español.
 *
 *   POST {AI_API_BASE_URL}/api/ai/meal-plan/generate
 *     Request:  AiMealPlanRequest
 *     Response: AiMealPlanResponse
 *     Semántica: genera un plan semanal (1..7 días) con N comidas/día.
 *
 * Errores: el backend devuelve {"error": "mensaje"} con 4xx/5xx. El cliente
 * Android los expone como excepciones via Result.failure(IOException(message)).
 */

object AiEndpoints {
    const val RECIPE_KCAL    = "/api/ai/recipe/kcal"
    const val PLATE_ESTIMATE = "/api/ai/plate/estimate"
    const val ROUTINE        = "/api/ai/routine/generate"
    const val MEAL_PLAN      = "/api/ai/meal-plan/generate"
}

// ---------- Recipe kcal refinement ----------

@Serializable
data class AiRefineRecipeRequest(
    val name: String,
    val ingredients: List<AiIngredientInput>,
    val cookingMethod: String? = null,
)

@Serializable
data class AiIngredientInput(
    val name: String,
    val quantityG: Int,
    val kcalPer100g: Float,
    val proteinPer100g: Float = 0f,
    val carbsPer100g: Float = 0f,
    val fatsPer100g: Float = 0f,
    val brand: String? = null,
)

@Serializable
data class AiRefineRecipeResponse(
    val totalKcal: Int,
    val totalProteinG: Int,
    val totalCarbsG: Int,
    val totalFatG: Int,
    val notes: String? = null,
    val confidence: Float = 0.7f,
)

// ---------- Plate estimation ----------

@Serializable
data class AiEstimatePlateRequest(
    val description: String,
    val locale: String = "es",
)

@Serializable
data class AiEstimatePlateResponse(
    val name: String,
    val kcalPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatsPer100g: Float,
    val defaultServingLabel: String,
    val defaultServingGrams: Float,
    val notes: String? = null,
)

// ---------- Routine generation ----------

@Serializable
data class AiGenerateRoutineRequest(
    val goalType: String,
    val level: String,
    val daysPerWeek: Int,
    val sessionMinutes: Int,
    val equipment: List<String> = emptyList(),
    val focusMuscleGroups: List<String> = emptyList(),
)

@Serializable
data class AiGenerateRoutineResponse(
    val name: String,
    val description: String,
    val estimatedDurationMin: Int,
    val exercises: List<AiRoutineExerciseDto>,
)

@Serializable
data class AiRoutineExerciseDto(
    val exerciseName: String,
    val muscleGroup: String,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val targetWeightKg: Float? = null,
    val restSeconds: Int = 60,
    val notes: String? = null,
)

// ---------- Weekly meal plan ----------

@Serializable
data class AiMealPlanRequest(
    val targetKcal: Int,
    val mealsPerDay: Int,
    val daysCount: Int = 7,
    val restrictions: List<String> = emptyList(),
    val macroPreference: String? = null,
)

@Serializable
data class AiMealPlanResponse(
    val days: List<AiMealPlanDay>,
)

@Serializable
data class AiMealPlanDay(
    val dayName: String,
    val meals: List<AiMealPlanMeal>,
)

@Serializable
data class AiMealPlanMeal(
    val slotName: String,
    val dishes: List<AiMealPlanDish>,
)

@Serializable
data class AiMealPlanDish(
    val name: String,
    val kcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val descriptionShort: String? = null,
)

// ---------- Error envelope ----------

@Serializable
data class AiErrorResponse(
    val error: String,
)
