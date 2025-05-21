package com.example.carcollection.presentation.main

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: CarRepository
) : ViewModel() {

    private val _cars = repository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cars: StateFlow<List<Car>> = _cars

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    val filteredCars: StateFlow<List<Car>> = _searchQuery
        .combine(_cars) { query, cars ->
            if (query.isBlank()) {
                cars
            } else {
                cars.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.brand.contains(query, ignoreCase = true)
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
