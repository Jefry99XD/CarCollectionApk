package com.example.carcollection.featureNotification.presentation

import com.example.carcollection.featureNotification.domain.NotificationType

data class NotificationUiState(
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val iconUrl: String? = null,
    val isEditMode: Boolean = false,
    val editingNotificationId: String? = null
)

