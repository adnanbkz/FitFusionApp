package com.example.fitfusion.data.repository

import android.util.Log
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.Serving
import kotlinx.coroutines.CancellationException
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
    private const val BASE_URL = "https://world.openfoodfacts.org/cgi/search.pl"
    private const val FIELDS   = "code,product_name,product_name_es,brands,nutriments,serving_size,serving_quantity,countries_tags"
    private const val USER_AGENT = "FitFusion/1.0 (https://github.com/adnanbkz/FitFusionApp)"
    private const val MIN_QUERY_LENGTH = 3
    private const val CACHE_TTL_MS = 10 * 60 * 1_000L
    private const val MIN_SEARCH_INTERVAL_MS = 1_500L

    data class SearchResult(
        val foods: List<Food> = emptyList(),
        val hasMore: Boolean = false,
        val failed: Boolean = false,
    )

    private data class CacheEntry(
        val createdAtMs: Long,
        val result: SearchResult,
    )

    private val cache = mutableMapOf<String, CacheEntry>()
    private var lastNetworkSearchAtMs = 0L

    suspend fun search(query: String, pageSize: Int = 20, page: Int = 1): SearchResult = withContext(Dispatchers.IO) {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return@withContext SearchResult()

        val safePageSize = pageSize.coerceIn(1, 50)
        val safePage = page.coerceAtLeast(1)
        val cacheKey = "$normalizedQuery|$safePageSize|$safePage"

        val now = System.currentTimeMillis()
        cache[cacheKey]
            ?.takeIf { now - it.createdAtMs <= CACHE_TTL_MS }
            ?.let { return@withContext it.result }

        synchronized(this@OpenFoodFactsRepository) {
            val elapsedMs = now - lastNetworkSearchAtMs
            if (elapsedMs < MIN_SEARCH_INTERVAL_MS) {
                Log.d(TAG, "skipping query='$normalizedQuery': rate limited locally")
                return@withContext SearchResult(failed = true)
            }
            lastNetworkSearchAtMs = now
        }

        val url = buildSearchUrl(normalizedQuery, safePageSize, safePage)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 4_000
            readTimeout    = 4_000
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "search failed for query='$normalizedQuery': HTTP $responseCode")
                return@withContext SearchResult(failed = true)
            }
            val body = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
            val json = JSONObject(body)
            val foods = parseProducts(json)
            SearchResult(
                foods = foods,
                hasMore = json.hasMoreProducts(safePageSize, safePage, foods.size),
            ).also { result ->
                cache[cacheKey] = CacheEntry(System.currentTimeMillis(), result)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "search failed for query='$normalizedQuery'", e)
            SearchResult(failed = true)
        } finally {
            connection.disconnect()
        }
    }

    private fun buildSearchUrl(query: String, pageSize: Int, page: Int): URL {
        val params = linkedMapOf(
            "search_terms" to query,
            "search_simple" to "1",
            "action" to "process",
            "json" to "1",
            "lc" to "es",
            "sort_by" to "unique_scans_n",
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "fields" to FIELDS,
        )
        val encodedParams = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        }
        return URL("$BASE_URL?$encodedParams")
    }

    private fun JSONObject.hasMoreProducts(pageSize: Int, requestedPage: Int, returnedCount: Int): Boolean {
        val totalCount = optInt("count", 0)
        val currentPage = optInt("page", requestedPage).coerceAtLeast(1)
        return if (totalCount > 0) {
            currentPage * pageSize < totalCount
        } else {
            returnedCount >= pageSize
        }
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

    suspend fun lookupByBarcode(barcode: String): Food? = withContext(Dispatchers.IO) {
        val normalizedBarcode = barcode.toProductBarcodeOrNull() ?: return@withContext null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(
                "https://world.openfoodfacts.org/api/v2/product/$normalizedBarcode.json" +
                "?fields=code,product_name,product_name_es,brands,nutriments,serving_size,serving_quantity"
            )
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout    = 10_000
                setRequestProperty("User-Agent", USER_AGENT)
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "barcode lookup failed for '$normalizedBarcode': HTTP $responseCode")
                return@withContext null
            }
            val body = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
            val json = JSONObject(body)
            if (json.optInt("status", 0) != 1) return@withContext null
            json.optJSONObject("product")?.toFood()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "barcode lookup failed for '$normalizedBarcode'", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun String.toProductBarcodeOrNull(): String? =
        filter(Char::isDigit).takeIf { it.length in 8..14 }

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
