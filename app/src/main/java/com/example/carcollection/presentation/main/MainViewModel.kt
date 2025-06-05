package com.example.carcollection.presentation.main

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.example.carcollection.data.repository.TagRepository

class MainViewModel(
    internal val repository: CarRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _cars = repository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cars: StateFlow<List<Car>> = _cars

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getCarByIdSync(id: Int): Car? {
        return runBlocking {
            repository.getCarById(id)
        }
    }

    val allTags = tagRepository.getAllTagsFlow()

    var currentCar: Car? = null
    fun setCarForEdit(car: Car) {
        currentCar = car
    }

    // 👇 NUEVA PROPIEDAD
    var savedPage: Int = 0

    val filteredCars: StateFlow<List<Car>> = _searchQuery
        .combine(_cars) { query, cars ->
            if (query.isBlank()) {
                cars
            } else {
                cars.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.brand.contains(query, ignoreCase = true) ||
                            it.color.contains(query, ignoreCase = true) ||
                            it.year.contains(query, ignoreCase = true) ||
                            it.type.contains(query, ignoreCase = true) ||
                            it.serie.contains(query, ignoreCase = true) ||
                            it.id.toString().contains(query, ignoreCase = true) ||
                            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}

