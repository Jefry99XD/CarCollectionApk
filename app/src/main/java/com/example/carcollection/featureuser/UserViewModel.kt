package com.example.carcollection.featureuser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

class UserViewModel(
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _carCount = MutableStateFlow(0)
    val carCount: StateFlow<Int> = _carCount

    private val _tagCount = MutableStateFlow(0)
    val tagCount: StateFlow<Int> = _tagCount

    private val _achievementCount = MutableStateFlow(0)
    val achievementCount: StateFlow<Int> = _achievementCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _recentCars = MutableStateFlow<List<Car>>(emptyList())
    val recentCars: StateFlow<List<Car>> = _recentCars


    private val userMethods = UserMethods()
    private val carMethods = CarMethods()
    private val tagsMethods = TagsMethods()
    private val achievementMethods = AchievementMethods()

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
                _achievementCount.value = 0
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
            _achievementCount.value = 0
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

                // Fetch achievement count
                fetchAchievementCount()
            } catch (e: Exception) {
                _carCount.value = 0
                _tagCount.value = 0
                _achievementCount.value = 0
            }
        }
    }

    fun fetchAchievementCount() {
        viewModelScope.launch {
            try {
                val achievements = achievementMethods.getAllAchievements()
                // Count only unlocked achievements
                val unlockedCount = achievements.count { (_, userAchievement) ->
                    userAchievement?.unlocked == true
                }
                _achievementCount.value = unlockedCount
            } catch (e: Exception) {
                _achievementCount.value = 0
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

    private val _publicUser = MutableStateFlow<User?>(null)
    val publicUser: StateFlow<User?> = _publicUser

    private val _publicStats = MutableStateFlow<Map<String, Int>?>(null)
    val publicStats: StateFlow<Map<String, Int>?> = _publicStats

    private val _publicRecentCars = MutableStateFlow<List<Car>>(emptyList())
    val publicRecentCars: StateFlow<List<Car>> = _publicRecentCars


    fun clearPublicUserData() {
        _publicUser.value = null
        _publicStats.value = null
        _publicRecentCars.value = emptyList()
    }

    fun fetchPublicUserProfile(uid: String) {
        viewModelScope.launch {
            // Clear old data first to avoid showing stale information
            _publicUser.value = null
            val result = userMethods.getPublicUserProfile(uid)
            _publicUser.value = result.getOrNull()
        }
    }

    fun fetchPublicUserStats(uid: String) {
        viewModelScope.launch {
            // Clear old stats first to avoid showing cached data
            _publicStats.value = null
            val result = userMethods.getPublicUserStats(uid)
            _publicStats.value = result.getOrNull()
        }
    }

    fun fetchPublicRecentCars(uid: String) {
        viewModelScope.launch {
            // Clear old cars first
            _publicRecentCars.value = emptyList()
            val result = userMethods.getPublicRecentCars(uid)
            _publicRecentCars.value = result.getOrNull() ?: emptyList()

        }
    }

    private val _publicUsers = MutableStateFlow<List<User>>(emptyList())
    val publicUsers = _publicUsers.asStateFlow()

    fun fetchPublicUsers() {
        viewModelScope.launch {
            val result = userMethods.getAllPublicUsers()

            val mapped = result.getOrNull()?.map { map ->
                // Get counts from the map, handling different possible type conversions
                val carsCount = when (val count = map["carsCount"]) {
                    is Long -> count.toInt()
                    is Int -> count
                    is Number -> count.toInt()
                    else -> 0
                }

                val achievementsCount = when (val count = map["achievementsCount"]) {
                    is Long -> count.toInt()
                    is Int -> count
                    is Number -> count.toInt()
                    else -> 0
                }

                // Create a list of dummy badges based on achievements count for display purposes
                val badgesList = List(achievementsCount) { "Achievement_$it" }

                User(
                    uid = map["id"] as? String ?: "",
                    username = map["username"] as? String ?: "Sin nombre",
                    photoUrl = map["photoUrl"] as? String ?: "",
                    // email, bio no existen aquí así que se dejan default
                    totalCars = carsCount,
                    totalTags = 0, // No existe en el backend
                    totalFriends = 0,
                    totalSeries = 0,
                    badges = badgesList, // Use achievements count
                    lastActive = System.currentTimeMillis()
                )
            } ?: emptyList()

            _publicUsers.value = mapped
        }
    }

    private val _publicUserCars = MutableStateFlow<List<Car>>(emptyList())
    val publicUserCars = _publicUserCars.asStateFlow()

    fun fetchPublicUserCars(uid: String) {
        viewModelScope.launch {
            val result = userMethods.fetchPublicUserCars(uid)
            _publicUserCars.value = result.getOrNull() ?: emptyList()
        }
    }



}