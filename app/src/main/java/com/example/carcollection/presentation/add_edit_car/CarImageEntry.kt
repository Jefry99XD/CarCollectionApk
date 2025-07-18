package com.example.carcollection.presentation.add_edit_car
import kotlinx.serialization.Serializable

@Serializable
data class CarImageEntry(
    val name: String? = null,
    val url: String? = null,
    val year: String? = null,
    val series: String? = null,
    val color: String? = null,
)
