package com.example.carcollection.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class UserViewModel(
    private val carRepository: CarRepository
) : ViewModel() {

    // Devuelve el count de carros como StateFlow
    val carCount = carRepository.getCarCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
}