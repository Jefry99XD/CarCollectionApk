package com.example.carcollection.featurecar.domain

/**
 * Lightweight data class used exclusively for statistics generation.
 * Contains only the fields needed to compute stats categories, deliberately
 * omitting heavy fields like photoUrl and backgroundUrl.
 */
data class CarStatsData(
    val brand: String? = null,
    val year: String? = null,
    val color: String? = null,
    val quality: String? = null,
    val type: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long? = null
)

