package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Cache global para evitar lecturas repetidas del JSON
private var backgroundsCache: Map<String, String>? = null

suspend fun loadBackgroundCategoriesFromJson(context: Context): List<BackgroundCategory> {
    return withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
            val response = Gson().fromJson(jsonString, BackgroundsResponse::class.java)

            // Cachear el mapa ID -> URL para futuras consultas
            if (backgroundsCache == null) {
                backgroundsCache = response.categories
                    .flatMap { it.backgrounds }
                    .associate { it.id to it.url }
            }

            response.categories
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun getBackgroundUrlById(context: Context, backgroundId: String): String {
    return withContext(Dispatchers.IO) {
        try {
            // Si ya tenemos el caché, usarlo
            backgroundsCache?.let { cache ->
                return@withContext cache[backgroundId] ?: ""
            }

            // Si no, cargar el JSON y cachear
            val jsonString = context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
            val response = Gson().fromJson(jsonString, BackgroundsResponse::class.java)

            // Cachear para futuras consultas
            val cache = response.categories
                .flatMap { it.backgrounds }
                .associate { it.id to it.url }
            backgroundsCache = cache

            // Retornar la URL solicitada
            cache[backgroundId] ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

// Función para limpiar el caché si necesitas recargar (ej: después de actualizar backgrounds.json)
fun clearBackgroundsCache() {
    backgroundsCache = null
}
