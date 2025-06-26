package com.example.carcollection.presentation.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt


@Composable
fun AddTagScreen(
    viewModel: AddEditTagViewModel,
    onTagAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val tagName = viewModel.tagName.value
    val tagColor = viewModel.tagColor.value

    val colorOptions = listOf(
        "#EF9A9A", "#F48FB1", "#CE93D8", "#B39DDB",
        "#9FA8DA", "#90CAF9", "#81D4FA", "#80DEEA",
        "#80CBC4", "#A5D6A7", "#C5E1A5", "#E6EE9C",
        "#FFF59D", "#FFE082", "#FFCC80", "#FFAB91",
        "#BCAAA4", "#E0E0E0", "#B0BEC5", "#9E9E9E"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }

        OutlinedTextField(
            value = tagName,
            onValueChange = { viewModel.tagName.value = it },
            label = { Text("Nombre del Tag") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Color del Tag")

        /** Matriz de colores: 4 columnas **/
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)                        // ocupa todo el espacio disponible
                .padding(bottom = 2.dp),          // margen inferior para que no choque con el botón
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = true              // permite desplazarse si se desborda
        ) {
            items(colorOptions) { colorHex ->
                val color = Color(colorHex.toColorInt())
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color)
                        .border(
                            width = if (tagColor == colorHex) 3.dp else 1.dp,
                            color = if (tagColor == colorHex) Color.Black else Color.Gray
                        )
                        .clickable { viewModel.tagColor.value = colorHex }
                )
            }
        }
        Button(
            onClick = {
                viewModel.saveTag { onTagAdded() }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
