package com.example.carcollection.presentation.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.carcollection.utils.ColorPickerDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagScreen(
    viewModel: AddEditTagViewModel,
    onBackClick: () -> Unit,
    onTagSaved: () -> Unit,
) {
    val tagName = viewModel.tagName.value
    val tagColor = remember { mutableStateOf(Color(viewModel.tagColor.value.toColorInt())) }
    val showColorPicker = remember { mutableStateOf(false) }

    if (showColorPicker.value) {
        ColorPickerDialog(
            initialColor = tagColor.value,
            onDismissRequest = { showColorPicker.value = false },
            onColorSelected = {
                tagColor.value = it
                viewModel.tagColor.value = "#%02X%02X%02X".format(
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                )
                showColorPicker.value = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Tag") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = tagName,
                onValueChange = { viewModel.tagName.value = it },
                label = { Text("Nombre del Tag") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color del Tag")

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(tagColor.value, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
                    .clickable { showColorPicker.value = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.editTag {
                        onTagSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}

