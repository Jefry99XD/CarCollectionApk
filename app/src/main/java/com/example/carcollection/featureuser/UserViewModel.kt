package com.example.carcollection.featureuser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _carCount = MutableStateFlow(0)
    val carCount: StateFlow<Int> = _carCount

    private val _tagCount = MutableStateFlow(0)
    val tagCount: StateFlow<Int> = _tagCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _recentCars = MutableStateFlow<List<Car>>(emptyList())
    val recentCars: StateFlow<List<Car>> = _recentCars


    private val userMethods = UserMethods()
    private val carMethods = CarMethods()
    private val tagsMethods = TagsMethods()

    private val auth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        viewModelScope.launch {
            val current = auth.currentUser
            if (current != null) {
                // 🔁 usuario nuevo: recargar todo
                fetchUserProfile()
                fetchUserStats()
                fetchRecentCars()
            } else {
                // 🚪 usuario salió: limpiar estado
                _user.value = null
                _carCount.value = 0
                _tagCount.value = 0
                _recentCars.value = emptyList()
            }
        }
    }


    init {
        // Fetch initial state
        fetchUserProfile()
        fetchUserStats()
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
            _isLoading.value = true
            val result = userMethods.getUserProfile()
            _user.value = result.getOrNull()
            _errorMessage.value = result.exceptionOrNull()?.message
            _isLoading.value = false
        }
    }


    fun fetchCarCount() {
        viewModelScope.launch {
            val result = carMethods.getUserCars()
            _carCount.value = result.getOrNull()?.size ?: 0
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = userMethods.loginUser(email, password)
            result.onSuccess {
                // 🔄 Recargar todo lo del nuevo usuario
                fetchUserProfile()
                fetchUserStats()
                fetchRecentCars()

                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }

    fun registerUser(
        username: String,
        email: String,
        photoUrl: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = userMethods.registerUser(username, email, photoUrl, password)
            result.onSuccess {
                fetchUserProfile()
                fetchCarCount()
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userMethods.logoutUser()
            _user.value = null
            _carCount.value = 0
            _tagCount.value = 0
            _recentCars.value = emptyList() // 🧹 limpiar también los carros recientes
            _errorMessage.value = null
        }
    }



    suspend fun editUser(
        username: String,
        photoUrl: String,
        password: String,
        email: String,
        bio: String
    ): Result<Unit> {
        return try {
            val result = userMethods.editUserProfile(username, photoUrl, password, email, bio)
            if (result.isSuccess) {
                fetchUserProfile()
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun fetchUserStats() {
        viewModelScope.launch {
            try {
                val cars = carMethods.getUserCars().getOrNull()?.size ?: 0
                val tags = tagsMethods.getAllTags().size

                _carCount.value = cars
                _tagCount.value = tags

                // Actualizar el objeto User con sus estadísticas
                _user.value = _user.value?.updateStats(cars, tags)
            } catch (e: Exception) {
                _carCount.value = 0
                _tagCount.value = 0
            }
        }
    }

    fun fetchRecentCars() {
        viewModelScope.launch {
            try {
                val cars = carMethods.getRecentCars()
                _recentCars.value = cars
            } catch (e: Exception) {
                _recentCars.value = emptyList()
            }
        }
    }

    fun setCarsCreateAt(){
        viewModelScope.launch {
            try {
                carMethods.addMissingCreatedAtToAllCars()
            } catch (e: Exception) {
            }
        }
    }

}