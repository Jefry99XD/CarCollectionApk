package com.example.carcollection.presentation.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carcollection.presentation.main.DropdownMenuBox


@Composable
fun AddTagScreen(viewModel: AddEditTagViewModel,
                 onTagAdded: () -> Unit,
                 onBackClick: () -> Unit
) {
    val tagName = viewModel.tagName.value
    val tagColor = viewModel.tagColor.value

    val colorOptions = listOf(
        "#EF9A9A", // suave rojo
        "#F48FB1", // suave rosa
        "#CE93D8", // suave púrpura
        "#B39DDB", // suave índigo
        "#9FA8DA", // suave azul
        "#90CAF9", // suave celeste
        "#81D4FA", // suave azul claro
        "#80DEEA", // suave cian
        "#80CBC4", // suave verde azulado
        "#A5D6A7", // suave verde
        "#C5E1A5", // suave verde lima
        "#E6EE9C", // suave amarillo verdoso
        "#FFF59D", // suave amarillo
        "#FFE082", // suave ámbar
        "#FFCC80", // suave naranja
        "#FFAB91", // suave rojo anaranjado
        "#BCAAA4", // marrón claro
        "#E0E0E0", // gris claro
        "#B0BEC5", // azul grisáceo claro
        "#9E9E9E"  // gris medio
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
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colorOptions.forEach { colorHex ->
                val color = Color(android.graphics.Color.parseColor(colorHex))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color)
                        .clickable {
                            viewModel.tagColor.value = colorHex
                        }
                        .border(
                            width = if (tagColor == colorHex) 3.dp else 1.dp,
                            color = if (tagColor == colorHex) Color.Black else Color.Gray
                        )
                )
            }
        }


        Button(onClick = {
            viewModel.saveTag {
                onTagAdded()
            }
        }) {
            Text("Guardar")
        }
    }

}
