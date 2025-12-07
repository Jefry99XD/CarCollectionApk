package com.example.carcollection.featurecar.presentation.add_edit_car
import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

@Serializable
data class CarImageEntry(
    val name: String? = null,
    val url: String? = null,
    val year: String? = null,
    val series: String? = null,
    val color: String? = null,
)

@Serializable
data class CarVariation(
    val year: String? = null,
    val series: String? = null,
    val color: String? = null,
    val tampo: String? = null,
    @SerializedName("base_color_type")
    val chassisColorType: String? = null,
    @SerializedName("window_color")
    val windowColor: String? = null,
    @SerializedName("interior_color")
    val interiorColor: String? = null,
    @SerializedName("wheel_type")
    val wheelType: String? = null,
    @SerializedName("toy_number")
    val toyNumber: String? = null,
    val country: String? = null,
    val notes: String? = null,
    @SerializedName("photo_url")
    val url: String? = null
)

@Serializable
data class CarLibraryEntry(
    val name: String? = null,
    @SerializedName("Description")
    val description: String? = null,
    val variations: List<CarVariation>? = null
)

