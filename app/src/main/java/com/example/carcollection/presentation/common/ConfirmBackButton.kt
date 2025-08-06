package com.example.carcollection.presentation.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun ConfirmBackButton(
    onConfirmBack: () -> Unit,
    confirmMessage: String = "Perderás los cambios no guardados. ¿Deseas salir?",
    confirmTitle: String = "¿Deseas salir?"
) {
    var showConfirmation by remember { mutableStateOf(false) }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text(confirmTitle) },
            text = { Text(confirmMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    onConfirmBack()
                }) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    IconButton(onClick = { showConfirmation = true }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver"
        )
    }
}