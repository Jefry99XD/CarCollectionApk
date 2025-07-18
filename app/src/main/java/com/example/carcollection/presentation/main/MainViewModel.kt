package com.example.carcollection.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class MainViewModel(
    internal val repository: CarRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _cars = repository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cars: StateFlow<List<Car>> = _cars

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Filter states
    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand

    private val _selectedYear = MutableStateFlow<String?>(null)
    val selectedYear: StateFlow<String?> = _selectedYear

    private val _selectedSeries = MutableStateFlow<String?>(null)
    val selectedSeries: StateFlow<String?> = _selectedSeries

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag



    // Collections for filter options
    val allBrands: StateFlow<List<String>> = _cars
        .map { cars -> cars.map { it.brand }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allYears: StateFlow<List<String>> = _cars
        .map { cars -> cars.map { it.year }.distinct().sortedByDescending { it } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSeries: StateFlow<List<String>> = _cars
        .map { cars -> cars.map { it.serie }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags = tagRepository.getAllTagsFlow()


    // Current car for editing
    var currentCar: Car? = null
    fun setCarForEdit(car: Car) {
        currentCar = car
    }

    // Pagination
    var savedPage: Int = 0
    var savedItemsPerPage: Int = 20
        private set

    val filteredCars: StateFlow<List<Car>> = _cars
        .combine(_searchQuery) { cars, query ->
            cars.filter { car ->
                query.isBlank() ||
                        car.name.contains(query, ignoreCase = true) ||
                        car.brand.contains(query, ignoreCase = true) ||
                        car.color.contains(query, ignoreCase = true) ||
                        car.year.contains(query, ignoreCase = true) ||
                        car.type.contains(query, ignoreCase = true) ||
                        car.serie.contains(query, ignoreCase = true) ||
                        car.id.toString().contains(query, ignoreCase = true) ||
                        car.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
        .combine(_selectedBrand) { cars, brand ->
            brand?.let { b -> cars.filter { it.brand.equals(b, ignoreCase = true) } } ?: cars
        }
        .combine(_selectedYear) { cars, year ->
            year?.let { y -> cars.filter { it.year.equals(y, ignoreCase = true) } } ?: cars
        }
        .combine(_selectedSeries) { cars, series ->
            series?.let { s -> cars.filter { it.serie.equals(s, ignoreCase = true) } } ?: cars
        }
        .combine(_selectedTag) { cars, tag ->
            tag?.let { t -> cars.filter { it.tags.any { tg -> tg.equals(t, ignoreCase = true) } } } ?: cars
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search function
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Filter functions
    fun onBrandSelected(brand: String?) {
        _selectedBrand.value = brand
    }

    fun onYearSelected(year: String?) {
        _selectedYear.value = year
    }

    fun onSeriesSelected(series: String?) {
        _selectedSeries.value = series
    }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = tag
    }

    fun clearFilters() {
        _selectedBrand.value = null
        _selectedYear.value = null
        _selectedSeries.value = null
        _selectedTag.value = null
    }

    // Car operations
    fun getCarByIdSync(id: Int): Car? {
        return runBlocking {
            repository.getCarById(id)
        }
    }

    fun insertCar(car: Car) {
        viewModelScope.launch {
            repository.insertCar(car)
        }
    }

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            repository.deleteCar(car)
        }
    }

    fun setItemsPerPage(value: Int) {
        savedItemsPerPage = value
    }

}