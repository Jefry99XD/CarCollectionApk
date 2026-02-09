package com.example.carcollection.featureNotification.data

import com.example.carcollection.featureNotification.domain.Notification
import com.example.carcollection.featureNotification.domain.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationMethods {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Obtiene la colección de notificaciones del usuario actual
     */
    private fun notificationsCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("notifications")

    // ─────────────────────────────────────────────────────────────
    // OBTENER NOTIFICACIONES
    // ─────────────────────────────────────────────────────────────

    /**
     * Obtiene todas las notificaciones del usuario ordenadas por fecha (más reciente primero)
     */
    suspend fun getAllNotifications(): List<Notification> {
        if (auth.currentUser == null) return emptyList()

        val querySnapshot = notificationsCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(Notification::class.java)?.copy(id = doc.id)
        }
    }

    /**
     * Obtiene solo las notificaciones no leídas
     */
    suspend fun getUnreadNotifications(): List<Notification> {
        if (auth.currentUser == null) return emptyList()

        // Obtener todas las notificaciones sin filtrar para evitar necesidad de índice
        val querySnapshot = notificationsCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        // Filtrar las no leídas en memoria
        return querySnapshot.documents.mapNotNull { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            if (!isRead) {
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            } else {
                null
            }
        }
    }

    /**
     * Obtiene el conteo de notificaciones no leídas
     */
    suspend fun getUnreadCount(): Int {
        if (auth.currentUser == null) return 0

        // Obtener todas las notificaciones sin filtrar para evitar necesidad de índice
        val querySnapshot = notificationsCollection()
            .get()
            .await()

        // Contar las no leídas en memoria
        return querySnapshot.documents.count { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            !isRead
        }
    }

    // ─────────────────────────────────────────────────────────────
    // AGREGAR NOTIFICACIÓN
    // ─────────────────────────────────────────────────────────────

    /**
     * Agrega una nueva notificación
     */
    suspend fun addNotification(notification: Notification): String {
        if (auth.currentUser == null) {
            throw IllegalStateException("Usuario no autenticado")
        }

        val docRef = notificationsCollection().add(notification).await()
        return docRef.id
    }

    /**
     * Crea una notificación de logro desbloqueado
     */
    suspend fun createAchievementNotification(
        achievementTitle: String,
        achievementId: String,
        iconUrl: String? = null
    ) {
        val notification = Notification(
            title = "¡Logro desbloqueado!",
            message = "Has desbloqueado: $achievementTitle",
            type = NotificationType.ACHIEVEMENT,
            referenceId = achievementId,
            referenceType = "achievement",
            iconUrl = iconUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        addNotification(notification)
    }

    // ─────────────────────────────────────────────────────────────
    // ACTUALIZAR NOTIFICACIÓN
    // ─────────────────────────────────────────────────────────────

    /**
     * Marca una notificación como leída
     */
    suspend fun markAsRead(notificationId: String) {
        if (auth.currentUser == null) return

        notificationsCollection()
            .document(notificationId)
            .update("isRead", true)
            .await()
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    suspend fun markAllAsRead() {
        if (auth.currentUser == null) return

        // Obtener todas las notificaciones sin filtrar para evitar necesidad de índice
        val allNotifications = notificationsCollection()
            .get()
            .await()

        val batch = db.batch()

        // Filtrar solo las no leídas en memoria
        allNotifications.documents.forEach { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            if (!isRead) {
                batch.update(doc.reference, "isRead", true)
            }
        }

        batch.commit().await()
    }

    // ─────────────────────────────────────────────────────────────
    // ELIMINAR NOTIFICACIÓN
    // ─────────────────────────────────────────────────────────────

    /**
     * Elimina una notificación específica
     */
    suspend fun deleteNotification(notificationId: String) {
        if (auth.currentUser == null) return

        notificationsCollection()
            .document(notificationId)
            .delete()
            .await()
    }

    /**
     * Elimina todas las notificaciones leídas
     */
    suspend fun deleteAllRead() {
        if (auth.currentUser == null) return

        // Obtener todas las notificaciones sin filtrar para evitar necesidad de índice
        val allNotifications = notificationsCollection()
            .get()
            .await()

        val batch = db.batch()

        // Eliminar solo las leídas en memoria
        allNotifications.documents.forEach { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            if (isRead) {
                batch.delete(doc.reference)
            }
        }

        batch.commit().await()
    }

    /**
     * Elimina todas las notificaciones del usuario
     */
    suspend fun deleteAllNotifications() {
        if (auth.currentUser == null) return

        val allNotifications = notificationsCollection().get().await()

        val batch = db.batch()
        allNotifications.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        batch.commit().await()
    }

    // ─────────────────────────────────────────────────────────────
    // REAL-TIME LISTENER PARA CONTADOR DE NO LEÍDAS
    // ─────────────────────────────────────────────────────────────

    /**
     * Escucha cambios en tiempo real en el contador de notificaciones no leídas
     * @param onCountChanged Callback que se invoca cuando cambia el contador
     * @return ListenerRegistration que se puede usar para detener el listener
     */
    fun listenToUnreadCount(onCountChanged: (Int) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        if (auth.currentUser == null) return null

        return notificationsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onCountChanged(0)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val unreadCount = snapshot.documents.count { doc ->
                        val isRead = doc.getBoolean("isRead") ?: false
                        !isRead
                    }
                    onCountChanged(unreadCount)
                }
            }
    }
}

