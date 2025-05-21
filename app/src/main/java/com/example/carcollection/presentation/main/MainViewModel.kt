package com.example.carcollection.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: CarRepository
) : ViewModel() {

    val cars = repository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertCar(car: Car) {
        viewModelScope.launch {
            repository.insertCar(car)
            // Ya no es necesario llamar a loadCars() si estás usando Flow correctamente
        }
    }
}
