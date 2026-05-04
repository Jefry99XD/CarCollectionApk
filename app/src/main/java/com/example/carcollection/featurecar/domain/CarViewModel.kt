package com.example.carcollection.featurecar.domain

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class CarViewModel(
    private val carMethods: CarMethods,
    private val tagsMethods: TagsMethods
) : ViewModel() {

    // 🔹 Estado principal de autos
    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars

    private val achievementMethods = AchievementMethods()

    // 🔹 Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 🔹 Flag para evitar cargas duplicadas
    private var hasLoadedInitialData = false

    // 🔹 Estado de filtros
    private val _filterState = MutableStateFlow(CarFilterState())
    val filterState: StateFlow<CarFilterState> = _filterState

    // 🔹 Tags disponibles
    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags

    // 🔹 Variables para búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 🔹 Estados de ordenamiento
    private val _sortBy = MutableStateFlow("name")  // "name" o "date"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // ✅ Job para cancelar búsquedas anteriores (debounce)
    private var searchJob: Job? = null

    // ✅ Job y debouncing para evaluación de logros
    private var achievementCheckJob: Job? = null
    private var lastAchievementCheck = 0L
    private val ACHIEVEMENT_CHECK_DELAY = 3000L // 3 segundos

    // 🔹 Paginación
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _itemsPerPage = 50  // 50 carros por página
    val itemsPerPage: Int = _itemsPerPage

    // 🔹 Carro actual para edición
    var currentCar: Car? = null
        private set

    init {
        viewModelScope.launch {
            loadUserCars()
            _allTags.value = tagsMethods.getAllTags()
        }
    }

    fun checkAchievements(carEventOnly: Boolean = false) {
        // Cancelar job anterior si existe
        achievementCheckJob?.cancel()

        val now = System.currentTimeMillis()
        val timeSinceLastCheck = now - lastAchievementCheck

        // Si ya pasó suficiente tiempo, ejecutar inmediatamente
        // Si no, programar con delay
        val delay = if (timeSinceLastCheck > ACHIEVEMENT_CHECK_DELAY) {
            0L
        } else {
            ACHIEVEMENT_CHECK_DELAY - timeSinceLastCheck
        }

        achievementCheckJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (delay > 0) {
                    delay(delay)
                }

                lastAchievementCheck = System.currentTimeMillis()

                // Check API level at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Obtener la lista actualizada de autos del usuario
                    val userCars = _cars.value
                    // Obtener el usuario actual
                    val userMethods = com.example.carcollection.featureuser.data.UserMethods()
                    val currentUser = userMethods.getUserProfile().getOrNull()
                    // ✅ EVALUACIÓN INCREMENTAL: si es un evento de carro, solo evaluar COLLECTION + TIME_BASED + EXCLUSIVE
                    if (carEventOnly) {
                        achievementMethods.evaluateAchievementsForCarEvent(userCars, currentUser)
                    } else {
                        achievementMethods.evaluateAchievements(userCars, currentUser)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadUserCars() {
        // Evitar cargas duplicadas si ya se cargó
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = carMethods.getUserCars()
                _cars.value = result.getOrDefault(emptyList())

                // Solo verificar logros en la primera carga o cuando se agregan/eliminan autos
                if (!hasLoadedInitialData) {
                    hasLoadedInitialData = true
                    checkAchievements()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTags() {
        viewModelScope.launch {
            _allTags.value = tagsMethods.getAllTags()
        }
    }

    // 🔹 Selecciona el carro para editar
    fun setCarForEdit(car: Car) {
        currentCar = car
    }

    // 🔹 Funciones para actualizar filtros con debounce en búsqueda
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query  // ✅ Actualizar inmediatamente para UI
        _currentPage.value = 0  // Resetear a primera página

        // ✅ Debounce: cancelar búsqueda anterior y esperar 300ms
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)  // Esperar 300ms de inactividad
            _filterState.value = _filterState.value.copy(query = query)
        }
    }

    fun onBrandSelected(brand: String?) {
        _filterState.value = _filterState.value.copy(brand = brand)
        _currentPage.value = 0
    }

    fun onYearSelected(year: String?) {
        _filterState.value = _filterState.value.copy(year = year)
        _currentPage.value = 0
    }

    fun onSeriesSelected(series: String?) {
        _filterState.value = _filterState.value.copy(series = series)
        _currentPage.value = 0
    }

    fun onTagSelected(tag: String?) {
        _filterState.value = _filterState.value.copy(tag = tag)
        _currentPage.value = 0
    }

    fun onColorSelected(color: String?) {
        _filterState.value = _filterState.value.copy(color = color)
        _currentPage.value = 0
    }

    fun onTypeSelected(type: String?) {
        _filterState.value = _filterState.value.copy(type = type)
        _currentPage.value = 0
    }

    fun onQualitySelected(quality: String?) {
        _filterState.value = _filterState.value.copy(quality = quality)
        _currentPage.value = 0
    }

    fun clearFilters() {
        _filterState.value = CarFilterState()
        _currentPage.value = 0  // Resetear a primera página
    }

    // 🔹 Funciones de ordenamiento
    fun setSortBy(sortOption: String) {
        _sortBy.value = sortOption
        _currentPage.value = 0  // Resetear a primera página
    }

    fun setSortAscending(ascending: Boolean) {
        _sortAscending.value = ascending
    }

    // 🔹 Funciones de paginación
    fun nextPage() {
        val totalPages = (filteredCars.value.size + _itemsPerPage - 1) / _itemsPerPage
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value++
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    fun goToPage(pageNumber: Int) {
        val totalPages = (filteredCars.value.size + _itemsPerPage - 1) / _itemsPerPage
        if (pageNumber >= 0 && pageNumber < totalPages) {
            _currentPage.value = pageNumber
        }
    }

    fun resetPagination() {
        _currentPage.value = 0
    }

    // 🔹 Lista filtrada de autos (combinando todos los filtros)
    val filteredCars: StateFlow<List<Car>> = combine(_cars, _filterState) { cars, filters ->
        cars.filter { car ->
            (filters.query.isBlank() ||
                    car.name?.contains(filters.query, ignoreCase = true) == true ||
                    car.brand?.contains(filters.query, ignoreCase = true) == true ||
                    car.color?.contains(filters.query, ignoreCase = true) == true ||
                    car.year?.contains(filters.query, ignoreCase = true) == true ||
                    car.type?.contains(filters.query, ignoreCase = true) == true ||
                    car.serie?.contains(filters.query, ignoreCase = true) == true ||
                    car.id.toString().contains(filters.query, ignoreCase = true) ||
                    car.tags.any { it.contains(filters.query, ignoreCase = true) })
                    &&
                    (filters.brand?.equals(car.brand, ignoreCase = true) ?: true) &&
                    (filters.year?.equals(car.year, ignoreCase = true) ?: true) &&
                    (filters.series?.equals(car.serie, ignoreCase = true) ?: true) &&
                    (filters.color?.equals(car.color, ignoreCase = true) ?: true) &&
                    (filters.type?.equals(car.type, ignoreCase = true) ?: true) &&
                    (filters.quality?.equals(car.quality, ignoreCase = true) ?: true) &&
                    (filters.tag?.let { tag -> car.tags.any { it.equals(tag, ignoreCase = true) } }
                        ?: true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹 Carros paginados (basados en filtros + ordenamiento + paginación)
    val paginatedCars: StateFlow<List<Car>> = combine(
        filteredCars,
        _currentPage,
        _sortBy,
        _sortAscending
    ) { filtered, page, sortBy, ascending ->
        // 1️⃣ Aplicar ordenamiento
        val sorted = when (sortBy) {
            "date" -> filtered.sortedBy { it.createdAt ?: 0 }
            else -> filtered.sortedBy { it.name ?: "" }  // "name" es default
        }

        // 2️⃣ Aplicar dirección (ascendente/descendente)
        val finalList = if (ascending) sorted else sorted.reversed()

        // 3️⃣ Paginar
        val startIndex = page * _itemsPerPage
        val endIndex = minOf(startIndex + _itemsPerPage, finalList.size)
        if (startIndex < finalList.size) {
            finalList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹 Total de páginas disponibles
    val totalPages: StateFlow<Int> = filteredCars
        .map { cars -> (cars.size + _itemsPerPage - 1) / _itemsPerPage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 🔹 Operaciones sobre autos
    fun addCar(car: Car) {
        viewModelScope.launch {
            val result = carMethods.addCarToCollection(car)
            if (result.isSuccess) {
                loadUserCars()
                // ✅ EVALUACIÓN INCREMENTAL: solo COLLECTION + TIME_BASED + EXCLUSIVE
                checkAchievements(carEventOnly = true)
            }
        }
    }

    fun deleteCar(carId: String) {
        viewModelScope.launch {
            val result = carMethods.deleteCarFromCollection(carId)
            if (result.isSuccess) {
                // ✅ Actualizar estado local en lugar de forzar reload completo
                _cars.value = _cars.value.filter { it.id != carId }
                // ✅ EVALUACIÓN INCREMENTAL: solo COLLECTION + TIME_BASED + EXCLUSIVE
                checkAchievements(carEventOnly = true)
            }
        }
    }

    // 🔹 Actualizar tags de un carro (para Mass Tag)
    fun updateCarTags(carId: String, newTags: List<String>) {
        viewModelScope.launch {
            val car = _cars.value.find { it.id == carId }
            if (car != null) {
                val updatedCar = car.copy(tags = newTags)
                val result = carMethods.updateCarInCollection(carId, updatedCar)
                if (result.isSuccess) {
                    loadUserCars()
                }
            }
        }
    }

    // Para listas de sugerencias
    val allBrands: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.brand }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allYears: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.year }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSeries: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.serie }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allColors: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.color }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTypes: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.type }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQualities: StateFlow<List<String>> = _cars
        .map { cars -> cars.mapNotNull { it.quality }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Para filtros seleccionados
    val selectedBrand: StateFlow<String?> = _filterState.map { it.brand }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedYear: StateFlow<String?> = _filterState.map { it.year }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedSeries: StateFlow<String?> = _filterState.map { it.series }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedTag: StateFlow<String?> = _filterState.map { it.tag }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedColor: StateFlow<String?> = _filterState.map { it.color }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedType: StateFlow<String?> = _filterState.map { it.type }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val selectedQuality: StateFlow<String?> = _filterState.map { it.quality }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // 🔹 Variables para paginación
    private val _savedItemsPerPage = MutableStateFlow(20) // Default 20 items por página
    val savedItemsPerPage: StateFlow<Int> = _savedItemsPerPage.asStateFlow()

    private val _savedPage = MutableStateFlow(1)
    val savedPage = MutableStateFlow(0)

    fun setItemsPerPage(items: Int) {
        _savedItemsPerPage.value = items
    }

    fun setPage(page: Int) {
        _savedPage.value = page
    }
}
