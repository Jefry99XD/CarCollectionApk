package com.example.carcollection.featureNotification.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.featureNotification.domain.Notification
import com.example.carcollection.featureNotification.domain.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: (String) -> Unit,
    onDelete: (Notification) -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de tipo de notificación
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (notification.iconUrl != null && notification.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = notification.iconUrl,
                        contentDescription = notification.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = getNotificationIcon(notification.type),
                        contentDescription = notification.type.name,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Contenido de la notificación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Indicador de no leída
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimestamp(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Acciones
            Column {
                if (!notification.isRead) {
                    IconButton(
                        onClick = { onMarkAsRead(notification.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Marcar como leída",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onDelete(notification) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Retorna el ícono correspondiente al tipo de notificación
 */
private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.INFO -> Icons.Default.Info
        NotificationType.ACHIEVEMENT -> Icons.Default.EmojiEvents
        NotificationType.REMINDER -> Icons.Default.Alarm
        NotificationType.SOCIAL -> Icons.Default.People
        NotificationType.SYSTEM -> Icons.Default.Settings
        NotificationType.UPDATE -> Icons.Default.Update
    }
}

/**
 * Retorna el color correspondiente al tipo de notificación
 */
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.INFO -> Color(0xFF2196F3)
        NotificationType.ACHIEVEMENT -> Color(0xFFFFC107)
        NotificationType.REMINDER -> Color(0xFFFF5722)
        NotificationType.SOCIAL -> Color(0xFF4CAF50)
        NotificationType.SYSTEM -> Color(0xFF9E9E9E)
        NotificationType.UPDATE -> Color(0xFF673AB7)
    }
}

/**
 * Formatea el timestamp a un formato legible
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Ahora"
        diff < 3_600_000 -> "${diff / 60_000} min"
        diff < 86_400_000 -> "${diff / 3_600_000} h"
        diff < 604_800_000 -> "${diff / 86_400_000} d"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

