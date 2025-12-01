package com.example.carcollection.presentation.consultas

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.presentation.add_edit_car.CarImageEntry
import com.example.carcollection.featurecar.presentation.add_edit_car.CarLibraryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.client.call.*

@RequiresApi(Build.VERSION_CODES.O)
class CarLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _allCars = MutableStateFlow<List<CarImageEntry>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _paginatedCars = MutableStateFlow<List<CarImageEntry>>(emptyList())
    val paginatedCars: StateFlow<List<CarImageEntry>> = _paginatedCars

    private val itemsPerPage = 15

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }


    init {
        println("🎬 CarLibrary: ViewModel initialized")
        loadCarsFromWeb()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCarsFromWeb() {
        viewModelScope.launch {
            try {
                println("🌐 CarLibrary: Starting to load cars from web...")
                val url =
                    "https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/refs/heads/main/app/src/main/assets/diecast_images.json"

                println("🌐 CarLibrary: Fetching from URL: $url")
                val response = client.get(url) // Make the GET request
                val json: String = response.body() // Explicitly get the body as String

                println("📄 CarLibrary: JSON received, length = ${json.length}")
                println("📄 CarLibrary: JSON preview (first 200 chars): ${json.take(200)}")

                val gson = Gson()
                val carLibraryEntries = try {
                    println("🔍 CarLibrary: Attempting to parse as array...")
                    // Try to parse as an array first
                    val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
                    val entries = gson.fromJson<List<CarLibraryEntry>>(json, typeArray)
                    println("✅ CarLibrary: Successfully parsed as array, size = ${entries.size}")
                    entries
                } catch (e: Exception) {
                    println("⚠️ CarLibrary: Array parsing failed: ${e.message}")
                    println("🔍 CarLibrary: Attempting to parse as single object...")
                    // If that fails, try to parse as a single object and wrap it in a list
                    val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                    val singleEntry = gson.fromJson<CarLibraryEntry>(json, typeSingle)
                    println("✅ CarLibrary: Successfully parsed as single object")
                    println("📝 CarLibrary: Car name = ${singleEntry.name}")
                    println("📝 CarLibrary: Variations count = ${singleEntry.variations?.size ?: 0}")
                    listOf(singleEntry)
                }

                println("🚗 CarLibrary: Total car entries = ${carLibraryEntries.size}")

                // Flatten the variations into individual CarImageEntry items
                val flattenedCars = mutableListOf<CarImageEntry>()
                carLibraryEntries.forEachIndexed { index, entry ->
                    println("🚗 CarLibrary: Processing entry #$index: ${entry.name}")
                    println("   Variations: ${entry.variations?.size ?: 0}")

                    entry.variations?.forEachIndexed { varIndex, variation ->
                        flattenedCars.add(
                            CarImageEntry(
                                name = entry.name,
                                url = variation.url,
                                year = variation.year,
                                series = variation.series,
                                color = variation.color
                            )
                        )
                        if (varIndex < 3) { // Log first 3 variations
                            println("   Variation #$varIndex: ${variation.year} - ${variation.color} - ${variation.series}")
                        }
                    }
                }

                println("✅ CarLibrary: Total flattened cars = ${flattenedCars.size}")
                _allCars.value = flattenedCars
                println("✅ CarLibrary: _allCars.value updated")
                updatePagination()
                println("✅ CarLibrary: Pagination updated")
            } catch (e: Exception) {
                println("❌ CarLibrary: ERROR - ${e.message}")
                e.printStackTrace()
                // Puedes emitir un estado de error si deseas
            }
        }
    }


    fun updateSearch(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
        updatePagination()
    }

    fun nextPage() {
        if ((_currentPage.value + 1) * itemsPerPage < filteredCars().size) {
            _currentPage.value++
            updatePagination()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updatePagination()
        }
    }

    private fun filteredCars(): List<CarImageEntry> {
        return if (_searchQuery.value.isBlank()) {
            _allCars.value
        } else {
            _allCars.value.filter {
                it.name?.contains(_searchQuery.value, ignoreCase = true) == true
            }
        }
    }

    private fun updatePagination() {
        val filtered = filteredCars()
        val start = _currentPage.value * itemsPerPage
        val end = minOf(start + itemsPerPage, filtered.size)
        _paginatedCars.value = filtered.subList(start, end)
        println("📄 CarLibrary: Pagination - filtered: ${filtered.size}, page: ${_currentPage.value}, showing: ${_paginatedCars.value.size} items")
    }
}
