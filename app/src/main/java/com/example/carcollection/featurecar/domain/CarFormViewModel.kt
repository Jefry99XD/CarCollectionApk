package com.example.carcollection.featurecar.domain

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.presentation.add_edit_car.BackgroundCategory
import com.example.carcollection.featurecar.presentation.add_edit_car.loadBackgroundCategories
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarFormViewModel(
    private val carMethods: CarMethods,
    private val tagsMethods: TagsMethods,
) : ViewModel() {

    // Form fields
    var brand = mutableStateOf("")
        private set
    var name = mutableStateOf("")
        private set
    var serie = mutableStateOf("")
        private set
    var year = mutableStateOf("")
        private set
    var photoUrl = mutableStateOf("")
        private set
    var color = mutableStateOf("")
        private set
    var type = mutableStateOf("")
        private set
    var quality = mutableStateOf("")
        private set
    var backgroundName = mutableStateOf("fondo")
        private set

    private var currentCarId: String? = null


    // Tags
    val availableTags = MutableStateFlow<List<Tag>>(emptyList())
    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags = _selectedTags.asStateFlow()

    private val _backgroundCategories = MutableStateFlow<List<BackgroundCategory>>(emptyList())
    val backgroundCategories = _backgroundCategories.asStateFlow()

    // Suggestions
    val brandSuggestions = MutableStateFlow<List<String>>(emptyList())
    val yearSuggestions = MutableStateFlow<List<String>>(emptyList())
    val typeSuggestions = MutableStateFlow<List<String>>(emptyList())
    val serieSuggestions = MutableStateFlow<List<String>>(emptyList())
    val colorSuggestions = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            // Cargar backgrounds
            _backgroundCategories.value = loadBackgroundCategories()

            // Cargar tags
            availableTags.value = tagsMethods.getAllTags()

            // Cargar sugerencias basadas en autos existentes
            carMethods.getUserCars().onSuccess { cars ->
                brandSuggestions.value = cars.mapNotNull { it.brand }.distinct()
                yearSuggestions.value = cars.mapNotNull { it.year }.distinct()
                typeSuggestions.value = cars.mapNotNull { it.type }.distinct()
                serieSuggestions.value = cars.mapNotNull { it.serie }.distinct()
                colorSuggestions.value = cars.mapNotNull { it.color }.distinct()
            }
        }
    }

    // Toggle tags
    fun toggleTag(tagName: String) {
        _selectedTags.update { current ->
            if (tagName in current) current.filterNot { it == tagName }
            else current + tagName
        }
    }

    // Event handlers for form fields
    fun onBrandChange(value: String) { brand.value = value }
    fun onNameChange(value: String) { name.value = value }
    fun onSerieChange(value: String) { serie.value = value }
    fun onYearChange(value: String) { year.value = value }
    fun onPhotoUrlChange(value: String) { photoUrl.value = value }
    fun onColorChange(value: String) { color.value = value }
    fun onTypeChange(value: String) { type.value = value }
    fun onQualityChange(value: String) { quality.value = value }
    fun onBackgroundNameChange(value: String) { backgroundName.value = value }

    // Load car data for editing
    fun loadCar(carId: String) {
        currentCarId = carId
        viewModelScope.launch {
            carMethods.getCarById(carId).onSuccess { car ->
                brand.value = car.brand.orEmpty()
                name.value = car.name.orEmpty()
                serie.value = car.serie.orEmpty()
                year.value = car.year.orEmpty()
                photoUrl.value = car.photoUrl.orEmpty()
                color.value = car.color.orEmpty()
                type.value = car.type.orEmpty()
                quality.value = car.quality.orEmpty()
                backgroundName.value = car.backgroundName.orEmpty()
                _selectedTags.value = car.tags
            }
        }
    }

    // Save or update car
    fun saveCar(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val car = Car(
                id = currentCarId,
                brand = brand.value,
                name = name.value,
                serie = serie.value,
                year = year.value,
                photoUrl = photoUrl.value,
                color = color.value,
                type = type.value,
                quality = quality.value,
                backgroundName = backgroundName.value,
                tags = selectedTags.value
            )

            val result = if (currentCarId == null) {
                carMethods.addCarToCollection(car)
            } else {
                carMethods.updateCarInCollection(currentCarId!!, car)
            }

            if (result.isSuccess) {
                onComplete?.invoke()
            } else {
                // Aquí puedes manejar errores si quieres
                println("Error saving car: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Reset only name and photoUrl for adding another car quickly
    fun resetForNewCar() {
        currentCarId = null
        name.value = ""
        photoUrl.value = ""
        // Todos los demás campos se mantienen (brand, serie, year, color, type, quality, backgroundName, tags)
    }
}

// ViewModelFactory for CarFormViewModel
class CarFormViewModelFactory(
    private val carMethods: CarMethods,
    private val tagsMethods: TagsMethods
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarFormViewModel::class.java)) {
            return CarFormViewModel(carMethods, tagsMethods) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

