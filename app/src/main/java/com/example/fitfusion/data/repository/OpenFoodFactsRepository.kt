package com.example.fitfusion.data.repository

import android.util.Log
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Serving
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID

object OpenFoodFactsRepository {

    private const val TAG = "OpenFoodFacts"
    private const val BASE_URL = "https://es.openfoodfacts.org/cgi/search.pl"
    private const val FIELDS   = "code,product_name,product_name_es,brands,nutriments,serving_size,serving_quantity,countries_tags"
    private const val USER_AGENT = "FitFusion/1.0 (https://github.com/adnanbkz/FitFusionApp)"
    private const val MIN_QUERY_LENGTH = 3
    private const val CACHE_TTL_MS = 10 * 60 * 1_000L
    private const val MIN_SEARCH_INTERVAL_MS = 6_000L

    private data class CacheEntry(
        val createdAtMs: Long,
        val results: List<Food>,
    )

    private val cache = mutableMapOf<String, CacheEntry>()
    private var lastNetworkSearchAtMs = 0L

    suspend fun search(query: String, pageSize: Int = 20): List<Food> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return@withContext emptyList()

        val now = System.currentTimeMillis()
        cache[normalizedQuery]
            ?.takeIf { now - it.createdAtMs <= CACHE_TTL_MS }
            ?.let { return@withContext it.results }

        synchronized(this@OpenFoodFactsRepository) {
            val elapsedMs = now - lastNetworkSearchAtMs
            if (elapsedMs < MIN_SEARCH_INTERVAL_MS) {
                Log.d(TAG, "skipping query='$normalizedQuery': rate limited locally")
                return@withContext emptyList()
            }
            lastNetworkSearchAtMs = now
        }

        val url = buildSearchUrl(normalizedQuery, pageSize.coerceIn(1, 20))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout    = 10_000
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "search failed for query='$normalizedQuery': HTTP $responseCode")
                return@withContext emptyList()
            }
            val body = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
            parseProducts(JSONObject(body)).also { results ->
                cache[normalizedQuery] = CacheEntry(System.currentTimeMillis(), results)
            }
        } catch (e: Exception) {
            Log.w(TAG, "search failed for query='$normalizedQuery'", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun buildSearchUrl(query: String, pageSize: Int): URL {
        val params = linkedMapOf(
            "search_terms" to query,
            "search_simple" to "1",
            "action" to "process",
            "json" to "1",
            "lc" to "es",
            "sort_by" to "unique_scans_n",
            "page_size" to pageSize.toString(),
            "fields" to FIELDS,
        )
        val encodedParams = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        }
        return URL("$BASE_URL?$encodedParams")
    }

    private fun parseProducts(json: JSONObject): List<Food> {
        val products = json.optJSONArray("products") ?: return emptyList()
        return buildList<JSONObject> {
            for (i in 0 until products.length()) {
                val p = products.optJSONObject(i) ?: continue
                add(p)
            }
        }
            .sortedWith(
                compareByDescending<JSONObject> { it.isSpainProduct() }
                    .thenByDescending { it.hasSpanishName() }
            )
            .mapNotNull { it.toFood() }
            .distinctBy { it.id }
    }

    private fun JSONObject.isSpainProduct(): Boolean =
        optJSONArray("countries_tags")?.contains("en:spain") == true

    private fun JSONObject.hasSpanishName(): Boolean =
        optString("product_name_es").isNotBlank()

    private fun JSONArray.contains(value: String): Boolean {
        for (i in 0 until length()) {
            if (optString(i) == value) return true
        }
        return false
    }

    private fun JSONObject.toFood(): Food? {
        val name = optString("product_name_es").trim()
            .ifBlank { optString("product_name").trim() }
            .takeIf { it.isNotBlank() } ?: return null
        val code = optString("code").trim()
        val id   = if (code.isNotBlank()) "off_$code" else "off_${UUID.randomUUID()}"

        val nutriments    = optJSONObject("nutriments") ?: JSONObject()
        val kcal          = nutriments.optDouble("energy-kcal_100g", 0.0).toFloat()
        val protein       = nutriments.optDouble("proteins_100g",    0.0).toFloat()
        val carbs         = nutriments.optDouble("carbohydrates_100g", 0.0).toFloat()
        val fat           = nutriments.optDouble("fat_100g",         0.0).toFloat()

        val brand         = optString("brands").trim()
            .split(",").firstOrNull { it.isNotBlank() }?.trim()

        val servingLabel  = optString("serving_size").trim().takeIf { it.isNotBlank() }
        val servingGrams  = optDouble("serving_quantity", 0.0).toFloat()
        val servingOptions = buildList {
            add(Serving("100g", 100f))
            if (servingLabel != null && servingGrams > 0f && servingLabel != "100g") {
                add(Serving(servingLabel, servingGrams))
            }
        }.distinctBy { it.label }

        return Food(
            id             = id,
            name           = name,
            brand          = brand,
            kcalPer100g    = kcal,
            proteinPer100g = protein,
            carbsPer100g   = carbs,
            fatsPer100g    = fat,
            emoji          = "🍽️",
            servingOptions = servingOptions,
        )
    }
}
