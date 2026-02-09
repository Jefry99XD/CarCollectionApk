package com.example.carcollection.featureNotification.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureNotification.data.NotificationMethods
import com.example.carcollection.featureNotification.domain.Notification
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationMethods: NotificationMethods = NotificationMethods()
) : ViewModel() {

    private val _notificationState = mutableStateOf(NotificationUiState())
    val notificationState: State<NotificationUiState> = _notificationState

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    private var unreadCountListener: ListenerRegistration? = null

    init {
        // Carga inicial de notificaciones
        loadNotifications()
        // Iniciar listener en tiempo real para el contador de no leídas
        startUnreadCountListener()
    }

    // ─────────────────────────────────────────────────────────────
    // REAL-TIME LISTENER
    // ─────────────────────────────────────────────────────────────

    private fun startUnreadCountListener() {
        unreadCountListener = notificationMethods.listenToUnreadCount { count ->
            _unreadCount.value = count
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Detener el listener cuando el ViewModel se destruye
        unreadCountListener?.remove()
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.OnTitleChanged ->
                _notificationState.value = _notificationState.value.copy(title = event.value)

            is NotificationEvent.OnMessageChanged ->
                _notificationState.value = _notificationState.value.copy(message = event.value)

            is NotificationEvent.OnTypeChanged ->
                _notificationState.value = _notificationState.value.copy(type = event.value)

            is NotificationEvent.OnReferenceIdChanged ->
                _notificationState.value = _notificationState.value.copy(referenceId = event.value)

            is NotificationEvent.OnReferenceTypeChanged ->
                _notificationState.value = _notificationState.value.copy(referenceType = event.value)

            is NotificationEvent.OnIconUrlChanged ->
                _notificationState.value = _notificationState.value.copy(iconUrl = event.value)

            is NotificationEvent.OnSaveClicked -> saveNotification()

            is NotificationEvent.OnMarkAsRead -> markAsRead(event.notificationId)

            is NotificationEvent.OnDelete -> deleteNotification(event.notification)

            is NotificationEvent.OnMarkAllAsRead -> markAllAsRead()

            is NotificationEvent.OnDeleteAllRead -> deleteAllRead()

            is NotificationEvent.OnDeleteAll -> deleteAllNotifications()

            is NotificationEvent.OnClearAll -> clearAllNotifications()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CARGAR NOTIFICACIONES
    // ─────────────────────────────────────────────────────────────

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = notificationMethods.getAllNotifications()
                _notifications.value = result
                updateUnreadCount()
            } catch (e: Exception) {
                _uiMessage.emit("Error al cargar notificaciones: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateUnreadCount() {
        try {
            _unreadCount.value = notificationMethods.getUnreadCount()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GUARDAR NOTIFICACIÓN
    // ─────────────────────────────────────────────────────────────

    private fun saveNotification() {
        viewModelScope.launch {
            val state = _notificationState.value

            if (state.title.isBlank()) {
                _uiMessage.emit("El título no puede estar vacío")
                return@launch
            }

            if (state.message.isBlank()) {
                _uiMessage.emit("El mensaje no puede estar vacío")
                return@launch
            }

            _isLoading.value = true
            try {
                val notification = Notification(
                    title = state.title,
                    message = state.message,
                    type = state.type,
                    referenceId = state.referenceId,
                    referenceType = state.referenceType,
                    iconUrl = state.iconUrl,
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )

                notificationMethods.addNotification(notification)
                loadNotifications()
                _uiMessage.emit("Notificación creada")
                resetNotificationState()
            } catch (e: Exception) {
                _uiMessage.emit("Error al guardar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MARCAR COMO LEÍDA
    // ─────────────────────────────────────────────────────────────

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationMethods.markAsRead(notificationId)
                loadNotifications()
            } catch (e: Exception) {
                _uiMessage.emit("Error al marcar como leída: ${e.message}")
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationMethods.markAllAsRead()
                loadNotifications()
                _uiMessage.emit("Todas las notificaciones marcadas como leídas")
            } catch (e: Exception) {
                _uiMessage.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ELIMINAR NOTIFICACIÓN
    // ─────────────────────────────────────────────────────────────

    private fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            try {
                notificationMethods.deleteNotification(notification.id)
                loadNotifications()
                _uiMessage.emit("Notificación eliminada")
            } catch (e: Exception) {
                _uiMessage.emit("Error al eliminar: ${e.message}")
            }
        }
    }

    private fun deleteAllRead() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationMethods.deleteAllRead()
                loadNotifications()
                _uiMessage.emit("Notificaciones leídas eliminadas")
            } catch (e: Exception) {
                _uiMessage.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun deleteAllNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationMethods.deleteAllNotifications()
                loadNotifications()
                _uiMessage.emit("Todas las notificaciones eliminadas")
            } catch (e: Exception) {
                _uiMessage.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearAllNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Marca todas como leídas y luego las elimina
                notificationMethods.markAllAsRead()
                notificationMethods.deleteAllRead()
                loadNotifications()
                _uiMessage.emit("Todas las notificaciones eliminadas")
            } catch (e: Exception) {
                _uiMessage.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────

    private fun resetNotificationState() {
        _notificationState.value = NotificationUiState()
    }

    fun clearMessage() {
        viewModelScope.launch {
            _uiMessage.emit("")
        }
    }
}

