package com.example.carcollection.presentation.add_edit_car

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update


class AddEditCarViewModel(
    private val repository: CarRepository,
    private val tagRepository: TagRepository,
    private var currentCarId: Int? = null
) : ViewModel() {

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

    var backgroundName = mutableStateOf("fondo")
        private set


    val availableTags = tagRepository.getAllTagsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val brandSuggestions = MutableStateFlow<List<String>>(emptyList())
    val yearSuggestions = MutableStateFlow<List<String>>(emptyList())
    val typeSuggestions = MutableStateFlow<List<String>>(emptyList())
    val serieSuggestions = MutableStateFlow<List<String>>(emptyList())
    val colorSuggestions = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            repository.getAllCars().collect { cars ->
                brandSuggestions.value =
                    cars.map { it.brand }.filter { it.isNotBlank() }.distinct()
                yearSuggestions.value =
                    cars.map { it.year }.filter { it.isNotBlank() }.distinct()
                typeSuggestions.value =
                    cars.map { it.type }.filter { it.isNotBlank() }.distinct()
                serieSuggestions.value =
                    cars.map { it.serie }.filter { it.isNotBlank() }.distinct()
                colorSuggestions.value =
                    cars.map { it.color }.filter { it.isNotBlank() }.distinct()
            }
        }
    }




    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags = _selectedTags.asStateFlow()

    fun toggleTag(tagName: String) {
        _selectedTags.update { current ->
            if (tagName in current) {
                current.filterNot { it == tagName }  // lo quita
            } else {
                current + tagName                    // lo añade al final → mantiene el orden
            }
        }
    }


    fun onEvent(event: AddEditCarEvent) {
        when (event) {
            is AddEditCarEvent.EnteredBrand -> brand.value = event.value
            is AddEditCarEvent.EnteredName -> name.value = event.value
            is AddEditCarEvent.EnteredSerie -> serie.value = event.value
            is AddEditCarEvent.EnteredYear -> year.value = event.value
            is AddEditCarEvent.EnteredColor -> color.value = event.value
            is AddEditCarEvent.EnteredType -> type.value = event.value
            is AddEditCarEvent.EnteredPhotoUrl -> photoUrl.value = event.value
            is AddEditCarEvent.EnteredTags -> _selectedTags.value = event.value

            is AddEditCarEvent.EnteredBackgroundName -> {
                backgroundName.value = event.value
            }




            is AddEditCarEvent.SaveCar -> {
                viewModelScope.launch {
                    val car = Car(
                        id = currentCarId ?: 0,
                        brand = brand.value,
                        name = name.value,
                        serie = serie.value,
                        color = color.value,
                        type = type.value,
                        year = (year.value.toIntOrNull() ?: 0).toString(),
                        photoUrl = photoUrl.value,
                        tags = selectedTags.value,
                        backgroundName = backgroundName.value
                        )
                    repository.insertCar(car)
                }
            }

        }
        }
    fun loadCar(id: Int) {
        viewModelScope.launch {
            repository.getCarById(id)?.let { car ->
                brand.value = car.brand
                name.value = car.name
                serie.value = car.serie
                year.value = car.year
                photoUrl.value = car.photoUrl
                color.value = car.color
                type.value = car.type
                currentCarId = car.id
                _selectedTags.value = car.tags

            }

        }
    }



}
