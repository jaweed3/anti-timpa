package com.factlens.network

import com.factlens.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SearchClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    suspend fun search(query: String, maxResults: Int = 5): List<Source> = withContext(Dispatchers.IO) {
        try {
            val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) FactLens/1.0")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext emptyList()

            parseResults(html, maxResults)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseResults(html: String, max: Int): List<Source> {
        val results = mutableListOf<Source>()
        try {
            // Simple HTML parsing — extract result__title and result__snippet
            val titlePattern = Regex("""<a[^>]*class="[^"]*result__a[^"]*"[^>]*>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
            val snippetPattern = Regex("""<a[^>]*class="[^"]*result__snippet[^"]*"[^>]*>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
            val urlPattern = Regex("""uddg=([^&"']+)""")

            val titles = titlePattern.findAll(html).toList()
            val snippets = snippetPattern.findAll(html).toList()
            val urls = urlPattern.findAll(html).toList()

            for (i in 0 until minOf(titles.size, urls.size, max)) {
                val title = titles[i].groupValues[1].replace(Regex("<[^>]+>"), "").trim()
                val url = java.net.URLDecoder.decode(urls[i].groupValues[1], "UTF-8")
                val snippet = if (i < snippets.size) {
                    snippets[i].groupValues[1].replace(Regex("<[^>]+>"), "").trim()
                } else ""

                if (title.isNotBlank()) {
                    results.add(Source(title = title, url = url, snippet = snippet))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }
}
