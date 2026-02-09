package com.example.carcollection.featureNotification.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Escuchar mensajes del ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { message ->
            if (message.isNotEmpty()) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Limpiar todas (marcar como leídas y eliminar)
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(NotificationEvent.OnClearAll)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar todas",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Indicador de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                notifications.isEmpty() -> {
                    // Estado vacío
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Sin notificaciones",
                            modifier = Modifier.padding(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No tienes notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aquí aparecerán tus logros desbloqueados y otras actualizaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    // Lista de notificaciones
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Separar notificaciones no leídas de leídas
                        val unreadNotifications = notifications.filter { !it.isRead }
                        val readNotifications = notifications.filter { it.isRead }

                        // Sección de no leídas
                        if (unreadNotifications.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Nuevas (${unreadNotifications.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(unreadNotifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onMarkAsRead = { id ->
                                        viewModel.onEvent(NotificationEvent.OnMarkAsRead(id))
                                    },
                                    onDelete = { notif ->
                                        viewModel.onEvent(NotificationEvent.OnDelete(notif))
                                    }
                                )
                            }
                        }

                        // Sección de leídas
                        if (readNotifications.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Anteriores",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(readNotifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onMarkAsRead = { id ->
                                        viewModel.onEvent(NotificationEvent.OnMarkAsRead(id))
                                    },
                                    onDelete = { notif ->
                                        viewModel.onEvent(NotificationEvent.OnDelete(notif))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

