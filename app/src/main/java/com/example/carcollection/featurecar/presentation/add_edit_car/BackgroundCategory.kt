package com.example.carcollection.featurecar.presentation.add_edit_car

data class BackgroundCategory(
    val category: String,
    val backgrounds: List<BackgroundItem>
)

data class BackgroundItem(
    val id: String,
    val name: String,
    val url: String,
    val thumbnailUrl: String
)

data class BackgroundsResponse(
    val categories: List<BackgroundCategory>
)