package com.example.carcollection.featurecar.domain

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties // Permite que Firestore ignore propiedades que no estén en el documento
data class Car(
    val id: String? = null,             // ID del documento en Firestore
    val brand: String? = null,
    val name: String? = null,
    val serie: String? = null,
    val year: String? = null,
    val photoUrl: String? = null,
    val color: String? = null,
    val type: String? = null,
    val tags: List<String> = emptyList(), // Lista de tags
    val backgroundName: String? = null,

    val createdAt: Long? = null // Timestamp de creación
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    constructor() : this(
        id = null,
        brand = null,
        name = null,
        serie = null,
        year = null,
        photoUrl = null,
        color = null,
        type = null,
        tags = emptyList(),
        backgroundName = null,
        createdAt = null
    )
}
