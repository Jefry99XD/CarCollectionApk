package com.example.carcollection.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text


@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val color = remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onColorSelected(color.value) }) {
                Text("Seleccionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        },
        title = { Text("Seleccionar color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = color.value.red,
                    onValueChange = { color.value = color.value.copy(red = it) },
                    colors = SliderDefaults.colors(thumbColor = Color.Red)
                )
                Slider(
                    value = color.value.green,
                    onValueChange = { color.value = color.value.copy(green = it) },
                    colors = SliderDefaults.colors(thumbColor = Color.Green)
                )
                Slider(
                    value = color.value.blue,
                    onValueChange = { color.value = color.value.copy(blue = it) },
                    colors = SliderDefaults.colors(thumbColor = Color.Blue)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.value, shape = CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }
    )
}
