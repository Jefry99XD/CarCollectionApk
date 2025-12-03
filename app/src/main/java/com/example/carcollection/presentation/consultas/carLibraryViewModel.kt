package com.example.carcollection.presentation.consultas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

class CarLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _allCars = MutableStateFlow<List<CarLibraryEntry>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _paginatedCars = MutableStateFlow<List<CarLibraryEntry>>(emptyList())
    val paginatedCars: StateFlow<List<CarLibraryEntry>> = _paginatedCars

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCar = MutableStateFlow<CarLibraryEntry?>(null)
    val selectedCar: StateFlow<CarLibraryEntry?> = _selectedCar

    private val itemsPerPage = 20

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }


    init {
        loadCarsFromWeb()
    }

    private fun loadCarsFromWeb() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val url =
                    "https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/refs/heads/main/app/src/main/assets/diecast_images.json"

                val response = client.get(url)
                val json: String = response.body()

                val gson = Gson()
                val carLibraryEntries = try {
                    val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
                    gson.fromJson(json, typeArray)
                } catch (_: Exception) {
                    val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                    val singleEntry = gson.fromJson<CarLibraryEntry>(json, typeSingle)
                    listOf(singleEntry)
                }

                _allCars.value = carLibraryEntries
                updatePagination()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCar(car: CarLibraryEntry) {
        _selectedCar.value = car
    }

    fun clearSelection() {
        _selectedCar.value = null
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

    private fun filteredCars(): List<CarLibraryEntry> {
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
        _paginatedCars.value = if (filtered.isEmpty()) emptyList() else filtered.subList(start, end)
    }

    fun getTotalPages(): Int {
        val filtered = filteredCars()
        return if (filtered.isEmpty()) 0 else (filtered.size + itemsPerPage - 1) / itemsPerPage
    }
}
