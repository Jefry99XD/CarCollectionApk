package com.example.carcollection.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

object ImageSearchUtil {
    suspend fun searchImageUrl(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val requestUrl = "https://duckduckgo.com/?q=$encodedQuery&iax=images&ia=images"

            val html = URL(requestUrl).readText()

            val pattern = Pattern.compile("(?<=imgurl=)(.*?)(?=&)")
            val matcher = pattern.matcher(html)

            if (matcher.find()) {
                return@withContext matcher.group()
            }

            null
        } catch (_: Exception) {
            null
        }
    }
}
