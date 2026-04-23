package com.example.fitfusion.data.repository

import com.example.fitfusion.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class AlgoliaSearchPage(
    val documentIds: List<String>,
    val page: Int,
    val hasMore: Boolean,
)

class AlgoliaExerciseSearchRepository(
    private val appId: String = BuildConfig.ALGOLIA_APP_ID,
    private val searchApiKey: String = BuildConfig.ALGOLIA_SEARCH_API_KEY,
    private val indexName: String = BuildConfig.ALGOLIA_EXERCISES_INDEX_NAME,
) {
    val isConfigured: Boolean
        get() = appId.isNotBlank() && searchApiKey.isNotBlank() && indexName.isNotBlank()

    suspend fun searchExercises(
        query: String,
        page: Int,
        hitsPerPage: Int,
    ): AlgoliaSearchPage = withContext(Dispatchers.IO) {
        require(isConfigured) {
            "Configura ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY y ALGOLIA_EXERCISES_INDEX_NAME en local.properties."
        }

        val encodedIndexName = URLEncoder.encode(indexName, StandardCharsets.UTF_8.name())
        val endpoint = URL("https://$appId-dsn.algolia.net/1/indexes/$encodedIndexName/query")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("X-Algolia-Application-Id", appId)
            setRequestProperty("X-Algolia-API-Key", searchApiKey)
        }

        val requestBody = JSONObject()
            .put("query", query)
            .put("page", page)
            .put("hitsPerPage", hitsPerPage)
            .put(
                "attributesToRetrieve",
                JSONArray().put("documentId").put("exerciseId")
            )

        try {
            connection.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            val responseText = connection.readResponseBody(responseCode)

            if (responseCode !in 200..299) {
                throw IllegalStateException(parseAlgoliaError(responseText))
            }

            parseSearchPage(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSearchPage(responseText: String): AlgoliaSearchPage {
        val payload = JSONObject(responseText)
        val hits = payload.optJSONArray("hits") ?: JSONArray()
        val ids = buildList {
            for (index in 0 until hits.length()) {
                val hit = hits.optJSONObject(index) ?: continue
                val documentId = hit.optString("documentId")
                    .ifBlank { hit.optString("objectID") }
                    .ifBlank { null }
                if (!documentId.isNullOrBlank()) {
                    add(documentId)
                }
            }
        }.distinct()

        val currentPage = payload.optInt("page", 0)
        val totalPages = payload.optInt("nbPages", 0)

        return AlgoliaSearchPage(
            documentIds = ids,
            page = currentPage,
            hasMore = currentPage + 1 < totalPages,
        )
    }

    private fun parseAlgoliaError(responseText: String): String {
        if (responseText.isBlank()) return "No se pudo completar la busqueda en Algolia."
        return runCatching {
            JSONObject(responseText).optString("message")
        }.getOrNull()?.takeIf(String::isNotBlank)
            ?: responseText
    }

    private fun HttpURLConnection.readResponseBody(responseCode: Int): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream.readTextSafely()
    }

    private fun InputStream?.readTextSafely(): String {
        if (this == null) return ""
        return BufferedReader(InputStreamReader(this, StandardCharsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}
