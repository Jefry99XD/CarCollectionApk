package com.example.carcollection.presentation.add_edit_car
import kotlinx.serialization.Serializable

@Serializable
data class CarImageEntry(
    val name: String,
    val url: String,
    val year: String,
    val series: String,
    val color: String,
)
