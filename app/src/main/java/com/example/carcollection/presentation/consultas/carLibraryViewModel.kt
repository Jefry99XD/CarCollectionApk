package com.example.carcollection.presentation.consultas

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.presentation.add_edit_car.CarImageEntry
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
        loadCarsFromWeb()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCarsFromWeb() {
        viewModelScope.launch {
            try {
                val url =
                    "https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/refs/heads/main/app/src/main/assets/diecast_images.json"
                val response = client.get(url) // Make the GET request
                val json: String = response.body() // Explicitly get the body as String

                val type = object : TypeToken<List<CarImageEntry>>() {}.type
                val cars = Gson().fromJson<List<CarImageEntry>>(json, type)

                _allCars.value = cars
                updatePagination()
            } catch (e: Exception) {
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
    }
}
