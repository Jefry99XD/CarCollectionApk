package com.example.carcollection.featurecar.domain

data class CarFilterState(
    val query: String = "",
    val brand: String? = null,
    val year: String? = null,
    val series: String? = null,
    val tag: String? = null
)
