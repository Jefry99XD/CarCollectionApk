package com.example.carcollection.presentation.add_edit_car

sealed class AddEditCarEvent {
    data class EnteredBrand(val value: String) : AddEditCarEvent()
    data class EnteredName(val value: String) : AddEditCarEvent()
    data class EnteredSerie(val value: String) : AddEditCarEvent()
    data class EnteredYear(val value: String) : AddEditCarEvent()
    data class EnteredPhotoUrl(val value: String) : AddEditCarEvent()
    data class EnteredColor(val value: String) : AddEditCarEvent()
    data class EnteredType(val value: String) : AddEditCarEvent()
    data class EnteredTags(val value: List<String>) : AddEditCarEvent()
    object SaveCar : AddEditCarEvent()
}
