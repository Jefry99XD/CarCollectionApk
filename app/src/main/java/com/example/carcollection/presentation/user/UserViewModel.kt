package com.example.carcollection.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.User
import com.google.firebase.auth.FirebaseAuth
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
    private val carMethods = CarMethods()
    private val auth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        // React to auth state changes: if user logged in, fetch profile and cars; if logged out, clear state
        viewModelScope.launch {
            val current = auth.currentUser
            if (current != null) {
                fetchUserProfile()
                fetchCarCount()
            } else {
                _user.value = null
                _carCount.value = 0
            }
        }
    }

    init {
        // Fetch initial state
        fetchUserProfile()
        fetchCarCount()
        // Register auth listener so ViewModel updates automatically when auth state changes
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            auth.removeAuthStateListener(authStateListener)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val result = userMethods.getUserProfile()
            _user.value = result.getOrNull()
        }
    }

    fun fetchCarCount() {
        viewModelScope.launch {
            val result = carMethods.getUserCars()
            _carCount.value = result.getOrNull()?.size ?: 0
        }
    }

    fun transferAllLocalCarsToFirebase(){
        viewModelScope.launch {
            val localCars = carRepository.getAllCarsList()
            val remoteCars = localCars.map { localCar ->
                Car(
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
            carMethods.syncLocalCarsToFirebase(remoteCars)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userMethods.logoutUser()
            _user.value = null
            _carCount.value = 0 }
    }

    fun editUser(username: String, photoUrl: String, password: String, email: String) {
        viewModelScope.launch {
            val result = userMethods.editUserProfile(username, photoUrl, password, email)
            if (result.isSuccess) {
                fetchUserProfile()
            }
        }
    }
}