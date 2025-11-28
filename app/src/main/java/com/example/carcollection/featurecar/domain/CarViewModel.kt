package com.example.carcollection.featurecar.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class CarViewModel(
    private val carMethods: CarMethods,
    private val tagsMethods: TagsMethods
) : ViewModel() {

    // 🔹 Estado principal de autos
    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars

    private val achievementMethods = AchievementMethods()

    // 🔹 Estado de filtros
    private val _filterState = MutableStateFlow(CarFilterState())
    val filterState: StateFlow<CarFilterState> = _filterState

    // 🔹 Tags disponibles
    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags

    // 🔹 Carro actual para edición
    var currentCar: Car? = null
        private set

    init {
        viewModelScope.launch {
            loadUserCars()
            _allTags.value = tagsMethods.getAllTags()
            checkAchievements()
        }
    }

    fun checkAchievements() {
        viewModelScope.launch {
            try {
                // Obtener la lista actualizada de autos del usuario
                val userCars = _cars.value
                // Revisar y actualizar logros
                achievementMethods.checkAndUpdateAchievements(userCars) { achievement ->
                    notifyAchievementUnlocked(achievement)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadUserCars() {
        viewModelScope.launch {
            val result = carMethods.getUserCars()
            _cars.value = result.getOrDefault(emptyList())
            checkAchievements()
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

    // 🔹 Funciones para actualizar filtros
    fun onSearchQueryChange(query: String) {    _searchQuery.value = query
        _filterState.value = _filterState.value.copy(query = query) }
    fun onBrandSelected(brand: String?) { _filterState.value = _filterState.value.copy(brand = brand) }
    fun onYearSelected(year: String?) { _filterState.value = _filterState.value.copy(year = year) }
    fun onSeriesSelected(series: String?) { _filterState.value = _filterState.value.copy(series = series) }
    fun onTagSelected(tag: String?) { _filterState.value = _filterState.value.copy(tag = tag) }
    fun clearFilters() { _filterState.value = CarFilterState() }

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
                    (filters.tag?.let { tag -> car.tags.any { it.equals(tag, ignoreCase = true) } } ?: true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹 Operaciones sobre autos
    fun addCar(car: Car) {
        viewModelScope.launch {
            val result = carMethods.addCarToCollection(car)
            if (result.isSuccess) {
                loadUserCars()
                checkAchievements()
            }
        }
    }

    fun deleteCar(carId: String) {
        viewModelScope.launch {
            val result = carMethods.deleteCarFromCollection(carId)
            if (result.isSuccess) {
                loadUserCars()
                checkAchievements()
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

    // 🔹 Variables para búsqueda y paginación
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _savedItemsPerPage = MutableStateFlow(20) // Default 20 items por página
    val savedItemsPerPage: StateFlow<Int> = _savedItemsPerPage.asStateFlow()

    private val _savedPage = MutableStateFlow(1)
    val savedPage = MutableStateFlow(0)

    fun setItemsPerPage(items: Int) { _savedItemsPerPage.value = items }

    fun setPage(page: Int) { _savedPage.value = page }


    private val _unlockedAchievement = MutableStateFlow<AchievementGlobal?>(null)
    val unlockedAchievement = _unlockedAchievement.asStateFlow()

    fun notifyAchievementUnlocked(achievement: AchievementGlobal?) {
        _unlockedAchievement.value = achievement
    }
}
