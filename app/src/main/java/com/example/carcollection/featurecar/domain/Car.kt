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
    val quality: String? = null, // Calidad del carro (Basico, TH, STH, etc.)
    val tags: List<String> = emptyList(), // Lista de tags
    val backgroundName: String? = null,
    val backgroundUrl: String? = null,  // URL del fondo (nuevo campo para migración)

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
        quality = null,
        tags = emptyList<String>(),
        backgroundName = null,
        backgroundUrl = null,
        createdAt = null
    )
}
