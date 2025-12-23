package com.example.carcollection.featureuser.domain

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
data class User(
    @get:Exclude
    val uid: String = "", // No se guarda como campo, solo se usa como ID del documento

    // ─── Datos básicos ───
    val username: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,

    // ─── Datos de registro ───
    val createdAt: Long = System.currentTimeMillis(),

    // ─── Estadísticas básicas ───
    @get:Exclude
    var totalCars: Int = 0,

    @get:Exclude
    var totalTags: Int = 0,
    val totalFriends: Int = 0,

    @get:Exclude
    var totalSeries: Int = 0,

    // ─── Insignias ───
    val badges: List<String> = emptyList(), // Ejemplo: ["Coleccionista", "Veterano"]

    // ─── Derechos de administrador ───
    val adminRights: Boolean = false, // Solo puede ser modificado en Firebase

    // ─── Última actividad ───
    val lastActive: Long = System.currentTimeMillis(),
) {
    constructor() : this("", null, null, null, null, 0L, 0, 0, 0, 0, emptyList(), false, 0L)

    // Propiedad computada: días desde que se unió
    @get:Exclude
    val memberDays: Int
        get() = ((System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)).toInt()

    // Método para actualizar las estadísticas
    fun updateStats(cars: Int, tags: Int, series: Int = 0): User {
        return this.copy().apply {
            totalCars = cars
            totalTags = tags
            totalSeries = series
        }
    }

    // Método para verificar si el usuario es admin
    @get:Exclude
    val isAdmin: Boolean
        get() = adminRights
}
