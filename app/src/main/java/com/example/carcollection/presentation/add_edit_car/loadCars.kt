package com.example.carcollection.presentation.add_edit_car

import android.content.Context
import kotlinx.serialization.json.Json



fun loadCarImagesFromAssets(context: Context): List<CarImageEntry> {
    return try {
        val inputStream = context.assets.open("diecast_images.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        Json.decodeFromString<List<CarImageEntry>>(json)
    } catch (e: Exception) {
        emptyList()
    }
}
