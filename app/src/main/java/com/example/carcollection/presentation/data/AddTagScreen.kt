package com.example.carcollection.presentation.data

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.carcollection.utils.ColorPickerDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagScreen(
    viewModel: AddEditTagViewModel,
    onTagAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val tagName = viewModel.tagName.value

    // Tag color sincronizado con viewModel, para que se actualice correctamente
    val tagColor by remember { derivedStateOf { Color(viewModel.tagColor.value.toColorInt()) } }
    val currentColor = remember { mutableStateOf(tagColor) }
    val showColorPicker = remember { mutableStateOf(false) }

    // Sincronizar currentColor si tagColor cambia (p. ej. carga o reset)
    LaunchedEffect(tagColor) {
        currentColor.value = tagColor
    }

    if (showColorPicker.value) {
        ColorPickerDialog(
            initialColor = currentColor.value,
            onDismissRequest = { showColorPicker.value = false },
            onColorSelected = {
                currentColor.value = it
                viewModel.tagColor.value = "#%02X%02X%02X".format(
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                )
                showColorPicker.value = false
            }
        )
    }

    val isNameValid = tagName.isNotBlank()
    val animatedColor by animateColorAsState(targetValue = currentColor.value)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Tag") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = tagName,
                onValueChange = { viewModel.tagName.value = it },
                label = { Text("Nombre del Tag") },
                modifier = Modifier.fillMaxWidth(),
                isError = !isNameValid,
                singleLine = true
            )
            if (!isNameValid) {
                Text(
                    text = "El nombre no puede estar vacío",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Text("Color del Tag", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(animatedColor, shape = RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    .clickable { showColorPicker.value = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Toca para cambiar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveTag { onTagAdded() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isNameValid
            ) {
                Text("GUARDAR TAG")
            }
        }
    }
}


