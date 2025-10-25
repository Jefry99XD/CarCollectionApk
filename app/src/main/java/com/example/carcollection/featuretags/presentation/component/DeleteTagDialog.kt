package com.example.carcollection.featuretags.presentation.component

import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import com.example.carcollection.featuretags.domain.Tag

@Composable
fun DeleteTagDialog(tag: Tag, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar tag?") },
        text = { Text("¿Estás seguro de que deseas eliminar el tag \"${tag.name}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}