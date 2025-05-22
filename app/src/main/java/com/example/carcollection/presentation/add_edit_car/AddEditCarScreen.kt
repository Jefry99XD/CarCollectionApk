package com.example.carcollection.presentation.add_edit_car

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Changed to autoMirrored
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // This import is not used, consider removing it
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AddEditCarScreen(
    viewModel: AddEditCarViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // It's a common practice to put navigation actions (like a back button)
        // at the top or within a TopAppBar, consider restructuring for better UX.
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Using AutoMirrored for better LTR/RTL support
                contentDescription = "Volver"
            )
        } // <<<<--- ADD THIS CLOSING BRACE

        OutlinedTextField(
            value = viewModel.brand.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredBrand(it)) },
            label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredName(it)) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.serie.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredSerie(it)) },
            label = { Text("Serie") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.year.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredYear(it)) },
            label = { Text("Año") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.color.value, // Access the .value property
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredColor(it)) },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.type.value, // Access the .value property
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredType(it)) },
            label = { Text("Tipo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.photoUrl.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredPhotoUrl(it)) },
            label = { Text("URL de la Foto") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.onEvent(AddEditCarEvent.SaveCar)
                onSaveSuccess()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}