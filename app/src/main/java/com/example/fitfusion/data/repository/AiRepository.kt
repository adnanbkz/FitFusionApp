package com.example.fitfusion.data.repository

import com.example.fitfusion.BuildConfig
import com.example.fitfusion.data.ai.AiEndpoints
import com.example.fitfusion.data.ai.AiEstimatePlateRequest
import com.example.fitfusion.data.ai.AiEstimatePlateResponse
import com.example.fitfusion.data.ai.AiGenerateRoutineRequest
import com.example.fitfusion.data.ai.AiGenerateRoutineResponse
import com.example.fitfusion.data.ai.AiMealPlanRequest
import com.example.fitfusion.data.ai.AiMealPlanResponse
import com.example.fitfusion.data.ai.AiRefineRecipeRequest
import com.example.fitfusion.data.ai.AiRefineRecipeResponse
import com.example.fitfusion.data.ai.AiWorkoutEstimateRequest
import com.example.fitfusion.data.ai.AiWorkoutEstimateResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object AiRepository {

    private val baseUrl: String = BuildConfig.AI_API_BASE_URL.trimEnd('/')

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults    = true
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun refineRecipeKcal(request: AiRefineRecipeRequest): Result<AiRefineRecipeResponse> =
        post(AiEndpoints.RECIPE_KCAL, request, AiRefineRecipeRequest.serializer(), AiRefineRecipeResponse.serializer())

    suspend fun estimatePlate(request: AiEstimatePlateRequest): Result<AiEstimatePlateResponse> =
        post(AiEndpoints.PLATE_ESTIMATE, request, AiEstimatePlateRequest.serializer(), AiEstimatePlateResponse.serializer())

    suspend fun generateRoutine(request: AiGenerateRoutineRequest): Result<AiGenerateRoutineResponse> =
        post(AiEndpoints.ROUTINE, request, AiGenerateRoutineRequest.serializer(), AiGenerateRoutineResponse.serializer())

    suspend fun generateWeeklyMealPlan(request: AiMealPlanRequest): Result<AiMealPlanResponse> =
        post(AiEndpoints.MEAL_PLAN, request, AiMealPlanRequest.serializer(), AiMealPlanResponse.serializer())

    suspend fun estimateWorkoutKcal(request: AiWorkoutEstimateRequest): Result<AiWorkoutEstimateResponse> =
        post(AiEndpoints.WORKOUT_ESTIMATE, request, AiWorkoutEstimateRequest.serializer(), AiWorkoutEstimateResponse.serializer())

    private suspend fun <Req, Res> post(
        path: String,
        body: Req,
        reqSerializer: kotlinx.serialization.KSerializer<Req>,
        resSerializer: kotlinx.serialization.KSerializer<Res>,
    ): Result<Res> = withContext(Dispatchers.IO) {
        runCatching {
            val token = currentIdToken()
                ?: throw IOException("No autenticado: inicia sesión para usar la IA")
            val payload = json.encodeToString(reqSerializer, body)
            val request = Request.Builder()
                .url("$baseUrl$path")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = parseErrorMessage(responseBody)
                        ?: "Error ${response.code} del servidor IA"
                    throw IOException(message)
                }
                json.decodeFromString(resSerializer, responseBody)
            }
        }
    }

    private suspend fun currentIdToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        val result = user.getIdToken(false).await()
        return result.token
    }

    private fun parseErrorMessage(body: String): String? = runCatching {
        if (body.isBlank()) return@runCatching null
        json.decodeFromString(
            com.example.fitfusion.data.ai.AiErrorResponse.serializer(),
            body,
        ).error
    }.getOrNull()
}
