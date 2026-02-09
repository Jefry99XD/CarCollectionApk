package com.example.carcollection.featureNotification.domain

import com.google.firebase.firestore.Exclude

/**
 * Representa una notificación del usuario.
 */
data class Notification(
    @get:Exclude
    var id: String = "",

    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // Opcional: referencia a un recurso específico (ej: carId, achievementId)
    val referenceId: String? = null,
    val referenceType: String? = null, // "car", "achievement", "user", etc.

    // Opcional: imagen/icono
    val iconUrl: String? = null
) {
    constructor() : this(
        id = "",
        title = "",
        message = "",
        type = NotificationType.INFO,
        isRead = false,
        createdAt = System.currentTimeMillis()
    )
}

/**
 * Tipos de notificaciones
 */
enum class NotificationType {
    INFO,           // Información general
    ACHIEVEMENT,    // Logro desbloqueado
    REMINDER,       // Recordatorio
    SOCIAL,         // Interacción social (like, follow, etc.)
    SYSTEM,         // Notificación del sistema
    UPDATE          // Actualización disponible
}

