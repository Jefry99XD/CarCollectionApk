package com.example.carcollection.presentation.add_edit_car

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.launch

class AddEditCarViewModel(
    private val repository: CarRepository
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

    fun onEvent(event: AddEditCarEvent) {
        when (event) {
            is AddEditCarEvent.EnteredBrand -> brand.value = event.value
            is AddEditCarEvent.EnteredName -> name.value = event.value
            is AddEditCarEvent.EnteredSerie -> serie.value = event.value
            is AddEditCarEvent.EnteredYear -> year.value = event.value
            is AddEditCarEvent.EnteredPhotoUrl -> photoUrl.value = event.value

            is AddEditCarEvent.SaveCar -> {
                viewModelScope.launch {
                    val car = Car(
                        brand = brand.value,
                        name = name.value,
                        serie = serie.value,
                        year = year.value.toIntOrNull() ?: 0,
                        photoUrl = photoUrl.value
                    )
                    repository.insertCar(car)
                }
            }
        }
    }
}
