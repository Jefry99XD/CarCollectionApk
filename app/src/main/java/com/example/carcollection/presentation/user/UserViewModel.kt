package com.example.carcollection.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.user.User
import com.example.carcollection.data.user.UserMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _carCount = MutableStateFlow(0)
    val carCount: StateFlow<Int> = _carCount

    private val userMethods = UserMethods()

    init {
        fetchUserProfile()
        fetchCarCount()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val result = userMethods.getUserProfile()
            _user.value = result.getOrNull()
        }
    }

    fun fetchCarCount() {
        viewModelScope.launch {
            val result = userMethods.getUserCars()
            _carCount.value = result.getOrNull()?.size ?: 0
        }
    }

    fun transferAllLocalCarsToFirebase(){
        viewModelScope.launch {
            val localCars = carRepository.getAllCarsList()
            val remoteCars = localCars.map { localCar ->
                com.example.carcollection.data.car.Car(
                    brand = localCar.brand,
                    name = localCar.name,
                    serie = localCar.serie,
                    year = localCar.year,
                    photoUrl = localCar.photoUrl,
                    color = localCar.color,
                    type = localCar.type,
                    tags = localCar.tags,
                    backgroundName = localCar.backgroundName
                )
            }
            userMethods.syncLocalCarsToFirebase(remoteCars)
        }
    }
}