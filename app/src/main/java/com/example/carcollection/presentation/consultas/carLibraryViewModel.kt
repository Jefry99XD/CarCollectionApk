package com.example.carcollection.presentation.consultas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.presentation.add_edit_car.CarImageEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class carLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _allCars = MutableStateFlow<List<CarImageEntry>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _paginatedCars = MutableStateFlow<List<CarImageEntry>>(emptyList())
    val paginatedCars: StateFlow<List<CarImageEntry>> = _paginatedCars

    private val itemsPerPage = 15

    init {
        loadCarsFromAssets()
    }

    private fun loadCarsFromAssets() {
        viewModelScope.launch {
            val jsonString = readJsonFromAssets("diecast_images.json")
            val type = object : TypeToken<List<CarImageEntry>>() {}.type
            val cars = Gson().fromJson<List<CarImageEntry>>(jsonString, type)
            _allCars.value = cars
            updatePagination()
        }
    }

    private fun readJsonFromAssets(fileName: String): String {
        val context = getApplication<Application>().applicationContext
        val inputStream = context.assets.open(fileName)
        return BufferedReader(inputStream.reader()).use { it.readText() }
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
                it.name?.contains(_searchQuery.value, ignoreCase = true) ?:  false
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
