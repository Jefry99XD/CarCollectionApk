package com.example.carcollection.presentation.add_edit_car

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    val availableTags = tagRepository.getAllTagsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedTags = mutableStateOf<List<String>>(emptyList())

    fun toggleTag(tagName: String) {
        val current = selectedTags.value.toMutableList()
        if (current.contains(tagName)) {
            current.remove(tagName)
        } else {
            current.add(tagName)
        }
        selectedTags.value = current
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
            is AddEditCarEvent.EnteredTags -> selectedTags.value = event.value


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
                        tags = selectedTags.value
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
                selectedTags.value = car.tags

            }

        }
    }



}
