package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// URL remota del JSON — actualízalo aquí para que todos los usuarios reciban fondos nuevos sin APK
private const val REMOTE_BACKGROUNDS_URL =
    "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/backgrounds.json"

// Cache global para evitar lecturas repetidas
private var backgroundsCache: Map<String, String>? = null
private var categoriesCache: List<BackgroundCategory>? = null

/** Intenta descargar el JSON remoto; retorna null si falla (sin internet, 404, etc.) */
private fun fetchRemoteJson(): String? {
    return try {
        val connection = (URL(REMOTE_BACKGROUNDS_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout  = 5_000
            requestMethod = "GET"
        }
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else null
    } catch (e: Exception) {
        null
    }
}

/** Lee el JSON local de assets como fallback */
private fun fetchLocalJson(context: Context): String? {
    return try {
        context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun parseAndCache(jsonString: String): List<BackgroundCategory> {
    val response = Gson().fromJson(jsonString, BackgroundsResponse::class.java)
    backgroundsCache = response.categories
        .flatMap { it.backgrounds }
        .associate { it.id to it.url }
    categoriesCache = response.categories
    return response.categories
}

suspend fun loadBackgroundCategoriesFromJson(context: Context): List<BackgroundCategory> {
    return withContext(Dispatchers.IO) {
        // Devolver caché si ya existe
        categoriesCache?.let { return@withContext it }

        // 1. Intentar remoto, 2. Fallback a local
        val jsonString = fetchRemoteJson() ?: fetchLocalJson(context)
        if (jsonString != null) parseAndCache(jsonString) else emptyList()
    }
}

suspend fun getBackgroundUrlById(context: Context, backgroundId: String): String {
    return withContext(Dispatchers.IO) {
        // Usar caché si disponible
        backgroundsCache?.let { return@withContext it[backgroundId] ?: "" }

        // Cargar si no hay caché
        val jsonString = fetchRemoteJson() ?: fetchLocalJson(context)
        if (jsonString != null) {
            parseAndCache(jsonString)[0]  // solo para poblar caché
            backgroundsCache?.get(backgroundId) ?: ""
        } else ""
    }
}

/** Llama esto para forzar recarga (ej: después de subir un nuevo backgrounds.json al repo) */
fun clearBackgroundsCache() {
    backgroundsCache = null
    categoriesCache = null
}
