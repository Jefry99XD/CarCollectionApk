package com.example.carcollection.presentation.add_edit_car

data class BackgroundCategory(
    val category: String,
    val backgrounds: List<BackgroundItem>
)

data class BackgroundItem(
    val name: String,
    val resource: String
)


