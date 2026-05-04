package com.example.carcollection.featureuser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.data.SecureLogger
import com.example.carcollection.featureuser.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val _seriesCount = MutableStateFlow(0)
    val seriesCount: StateFlow<Int> = _seriesCount


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _recentCars = MutableStateFlow<List<Car>>(emptyList())
    val recentCars: StateFlow<List<Car>> = _recentCars


    private val userMethods = UserMethods()
    private val carMethods = CarMethods()
    private val achievementMethods = AchievementMethods()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 🔹 Listener para sincronización real-time del usuario
    private var userProfileListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener {
        viewModelScope.launch {
            val current = auth.currentUser
            if (current != null) {
                // 🔁 usuario nuevo: recargar todo
                fetchUserProfile()
                fetchUserStats()
                fetchRecentCars()
            } else {
                // 🚪 usuario salió: limpiar estado y detener listener real-time
                stopRealtimeSync()
                _user.value = null
                _carCount.value = 0
                _tagCount.value = 0
                _achievementCount.value = 0
                _seriesCount.value = 0
                _recentCars.value = emptyList()
                _favoriteCars.value = emptyList()
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
            // 🔹 Remover listener de Firestore cuando el ViewModel se destruye
            stopRealtimeSync()
        } catch (e: Exception) {
            // ignore
        }
    }

    // 🔹 Detener sincronización real-time (llámalo explícitamente en logout)
    fun stopRealtimeSync() {
        userProfileListener?.remove()
        userProfileListener = null
    }

    // 🔹 NUEVA FUNCIÓN: Sincronización real-time del perfil
    fun startRealtimeSync() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Remover listener anterior si existe
        userProfileListener?.remove()

        // 🔹 Configurar listener de Firestore para cambios en tiempo real
        userProfileListener = db.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error en sincronización: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val updatedUser = snapshot.toObject(User::class.java)?.copy(uid = userId)
                        viewModelScope.launch {
                            _user.value = updatedUser
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "Error al procesar datos: ${e.message}"
                    }
                }
            }
    }

    fun fetchUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isLoading.value = true
            }
            val result = userMethods.getUserProfile()
            withContext(Dispatchers.Main) {
                _user.value = result.getOrNull()
                _errorMessage.value = result.exceptionOrNull()?.message
                _isLoading.value = false

                // 🔹 Iniciar sincronización real-time después de cargar perfil
                startRealtimeSync()
            }

            // Verificar si necesita migración de XP
            checkAndMigrateXP()
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
                fetchUserProfile()  // fetchUserProfile ya llama startRealtimeSync()
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
            // 🔹 Detener listener real-time ANTES de cerrar sesión
            stopRealtimeSync()
            userMethods.logoutUser()
            _user.value = null
            _carCount.value = 0
            _tagCount.value = 0
            _achievementCount.value = 0
            _seriesCount.value = 0
            _recentCars.value = emptyList()
            _favoriteCars.value = emptyList()
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Use UserMethods.getUserStats() for efficient single-call stats retrieval
                val statsResult = userMethods.getUserStats()
                if (statsResult.isSuccess) {
                    val stats = statsResult.getOrNull() ?: emptyMap()
                    withContext(Dispatchers.Main) {
                        _carCount.value = stats["cars"] ?: 0
                        _tagCount.value = stats["tags"] ?: 0
                        _seriesCount.value = stats["series"] ?: 0

                        // Update user object with stats
                        _user.value = _user.value?.updateStats(
                            cars = _carCount.value,
                            tags = _tagCount.value,
                            series = _seriesCount.value
                        )
                    }
                } else {
                    // Fallback simplificado: solo mostrar 0
                    withContext(Dispatchers.Main) {
                        _carCount.value = 0
                        _tagCount.value = 0
                        _seriesCount.value = 0
                    }
                }

                // Fetch achievement count separately
                fetchAchievementCount()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _carCount.value = 0
                    _tagCount.value = 0
                    _seriesCount.value = 0
                    _achievementCount.value = 0
                }
            }
        }
    }

    fun fetchAchievementCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val achievements = achievementMethods.getAllAchievements()
                // Count only unlocked achievements
                val unlockedCount = achievements.count { (_, userAchievement) ->
                    userAchievement?.unlocked == true
                }
                withContext(Dispatchers.Main) {
                    _achievementCount.value = unlockedCount
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _achievementCount.value = 0
                }
            }
        }
    }

    fun fetchRecentCars() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cars = carMethods.getRecentCars()
                withContext(Dispatchers.Main) {
                    _recentCars.value = cars
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _recentCars.value = emptyList()
                }
            }
        }
    }

    fun setCarsCreateAt(){
        viewModelScope.launch(Dispatchers.IO) {
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

    private val _publicFavoriteCars = MutableStateFlow<List<Car>>(emptyList())
    val publicFavoriteCars: StateFlow<List<Car>> = _publicFavoriteCars

    fun clearPublicUserData() {
        _publicUser.value = null
        _publicStats.value = null
        _publicRecentCars.value = emptyList()
        _publicFavoriteCars.value = emptyList()
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

    fun fetchPublicFavoriteCars(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favoriteIds = userMethods.getPublicUserFavoriteCars(uid).getOrNull() ?: emptyList()
                // ✅ Usa getCarsByIdsForUser para leer de la colección del usuario público (no del actual)
                val favoriteCars = carMethods.getCarsByIdsForUser(uid, favoriteIds)
                withContext(Dispatchers.Main) {
                    _publicFavoriteCars.value = favoriteCars
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _publicFavoriteCars.value = emptyList()
                }
            }
        }
    }


    private val _publicUsers = MutableStateFlow<List<User>>(emptyList())
    val publicUsers = _publicUsers.asStateFlow()

    fun fetchPublicUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userMethods.getAllPublicUsers()

            val mapped: List<User> = result.getOrNull()?.map { map ->
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

                val level = when (val lvl = map["level"]) {
                    is Long -> lvl.toInt()
                    is Int -> lvl
                    is Number -> lvl.toInt()
                    else -> 1
                }

                val totalXP = when (val xp = map["totalXP"]) {
                    is Long -> xp
                    is Int -> xp.toLong()
                    is Number -> xp.toLong()
                    else -> 0L
                }

                // Create a list of dummy badges based on achievements count for display purposes
                val badgesList = List(achievementsCount) { "Achievement_$it" }

                User().copy(
                    uid = map["id"] as? String ?: "",
                    username = map["username"] as? String ?: "Sin nombre",
                    photoUrl = map["photoUrl"] as? String ?: "",
                    level = level,
                    totalXP = totalXP,
                    badges = badgesList,
                    totalCars = carsCount,
                    totalTags = 0,
                    totalSeries = 0
                )
            } ?: emptyList<User>()

            withContext(Dispatchers.Main) {
                _publicUsers.value = mapped
            }
        }
    }

    private val _publicUserCars = MutableStateFlow<List<Car>>(emptyList())
    val publicUserCars = _publicUserCars.asStateFlow()

    fun fetchPublicUserCars(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userMethods.fetchPublicUserCars(uid)
            withContext(Dispatchers.Main) {
                _publicUserCars.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    private val _favoriteCars = MutableStateFlow<List<Car>>(emptyList())
    val favoriteCars: StateFlow<List<Car>> = _favoriteCars.asStateFlow()


    // ════════════════════════════════════════════════════════════════
    // CARROS FAVORITOS
    // ════════════════════════════════════════════════════════════════

    /**
     * Agregar o remover un carro de favoritos
     */
    fun toggleFavoriteCar(carId: String) {
        viewModelScope.launch {
            val result = userMethods.toggleFavoriteCar(carId)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()
                _user.value = updatedUser
                fetchFavoriteCars()
            }
        }
    }

    /**
     * Verificar si un carro está en favoritos
     */
    fun isCarFavorite(carId: String): Boolean {
        return _user.value?.favoriteCars?.contains(carId) == true
    }

    /**
     * Obtener cantidad de carros favoritos
     */
    fun getFavoriteCarsCount(): Int {
        return _user.value?.favoriteCars?.size ?: 0
    }

    /**
     * Cargar los datos completos de los carros favoritos
     */
    fun fetchFavoriteCars() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favoriteCarIds = _user.value?.favoriteCars ?: emptyList()
                val cars = carMethods.getCarsByIds(favoriteCarIds)
                withContext(Dispatchers.Main) {
                    _favoriteCars.value = cars
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _favoriteCars.value = emptyList()
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SISTEMA DE NIVELES Y XP
    // ════════════════════════════════════════════════════════════════

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    private val _xpGainEvent = MutableStateFlow<Pair<Int, String>?>(null)
    val xpGainEvent: StateFlow<Pair<Int, String>?> = _xpGainEvent.asStateFlow()

    /**
     * Verificar si el usuario necesita migración de XP y ejecutarla automáticamente
     */
    private fun checkAndMigrateXP() {
        viewModelScope.launch {
            try {
                val needsMigration = userMethods.needsXPMigration().getOrNull() ?: false
                if (needsMigration) {
                    val result = userMethods.migrateUserXP()
                    if (result.isSuccess) {
                        _user.value = result.getOrNull()
                        SecureLogger.success("XP migrated successfully")
                    }
                }
            } catch (e: Exception) {
                SecureLogger.failure("Failed to check/migrate XP", e.message)
            }

            // ✅ Ejecutar migración de fondos después de XP
            migrateCarBackgrounds()
        }
    }

    /**
     * MIGRACIÓN DE FONDOS: Reemplazar backgroundName por backgroundUrl
     * Se ejecuta solo una vez por usuario (verificada por flag en SharedPreferences)
     */
    private fun migrateCarBackgrounds() {
        viewModelScope.launch {
            try {
                val carMethods = com.example.carcollection.featurecar.data.CarMethods()
                val result = carMethods.migrateBackgrounds()
                if (result.isSuccess) {
                    SecureLogger.success("Background migration completed: ${result.getOrNull()}")
                }
            } catch (e: Exception) {
                SecureLogger.failure("Failed to migrate backgrounds", e.message)
            }
        }
    }

    /**
     * Agregar XP al usuario actual (usado internamente cuando se agrega carro/logro)
     */
    suspend fun addXP(amount: Int, source: com.example.carcollection.featureuser.domain.XPSource, sourceId: String? = null): Result<User> {
        return try {
            val currentLevel = _user.value?.level ?: 1
            val result = userMethods.addXP(amount, source, sourceId)

            if (result.isSuccess) {
                val updatedUser = result.getOrNull()
                _user.value = updatedUser

                // Emitir evento de ganancia de XP
                _xpGainEvent.value = Pair(amount, source.name)

                // Verificar si subió de nivel
                val newLevel = updatedUser?.level ?: currentLevel
                if (newLevel > currentLevel) {
                    _levelUpEvent.value = newLevel
                }

                Result.success(updatedUser!!)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Limpiar evento de subida de nivel (después de mostrarlo en UI)
     */
    fun clearLevelUpEvent() {
        _levelUpEvent.value = null
    }

    /**
     * Limpiar evento de ganancia de XP
     */
    fun clearXPGainEvent() {
        _xpGainEvent.value = null
    }

    /**
     * Ejecutar migración manual de XP (para botón en settings)
     */
    fun manualMigrateXP() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userMethods.migrateUserXP()
                if (result.isSuccess) {
                    _user.value = result.getOrNull()
                    _errorMessage.value = "XP migrada exitosamente"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al migrar XP: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}