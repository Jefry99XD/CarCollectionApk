package com.example.carcollection.featureNotification.presentation

import com.example.carcollection.featureNotification.domain.Notification
import com.example.carcollection.featureNotification.domain.NotificationType

sealed class NotificationEvent {
    data class OnTitleChanged(val value: String) : NotificationEvent()
    data class OnMessageChanged(val value: String) : NotificationEvent()
    data class OnTypeChanged(val value: NotificationType) : NotificationEvent()
    data class OnReferenceIdChanged(val value: String?) : NotificationEvent()
    data class OnReferenceTypeChanged(val value: String?) : NotificationEvent()
    data class OnIconUrlChanged(val value: String?) : NotificationEvent()

    data object OnSaveClicked : NotificationEvent()
    data class OnMarkAsRead(val notificationId: String) : NotificationEvent()
    data class OnDelete(val notification: Notification) : NotificationEvent()
    data object OnMarkAllAsRead : NotificationEvent()
    data object OnDeleteAllRead : NotificationEvent()
    data object OnDeleteAll : NotificationEvent()
    data object OnClearAll : NotificationEvent() // Marca todas como leídas y las elimina
}

