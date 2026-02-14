package com.example.carcollection.featureuser.domain

import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

/**
 * Registro de actividad de XP ganada
 */
@Serializable
@IgnoreExtraProperties
data class XPActivity(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val source: String = XPSource.CAR_ADDED.name,
    val sourceId: String? = null,  // ID del carro, logro, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val levelBefore: Int = 0,
    val levelAfter: Int = 0
) {
    constructor() : this("", "", 0, XPSource.CAR_ADDED.name, null, System.currentTimeMillis(), 0, 0)
}

/**
 * Fuentes de XP posibles
 */
enum class XPSource(val xpAmount: Int) {
    CAR_ADDED(100),
    ACHIEVEMENT_UNLOCKED(200);

    companion object {
        fun fromString(value: String): XPSource {
            return entries.find { it.name == value } ?: CAR_ADDED
        }
    }
}

