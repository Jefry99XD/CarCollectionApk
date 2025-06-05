package com.example.carcollection.presentation.add_edit_car

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Changed to autoMirrored
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember // This import is not used, consider removing it
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCarScreen(
    viewModel: AddEditCarViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
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
            placeholder = { Text("Hot Wheels, Maisto, Matchbox, etc.") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredName(it)) },
            label = { Text("Nombre") },
            placeholder = { Text("Honda Civic, Reverb, etc.") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.serie.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredSerie(it)) },
            placeholder = { Text("HW Flames, J-Imports, etc.") },
            label = { Text("Serie") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.year.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredYear(it)) },
            label = { Text("Año") },
            placeholder = { Text("2025") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.color.value, // Access the .value property
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredColor(it)) },
            label = { Text("Color") },
            placeholder = { Text("Rojo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.type.value, // Access the .value property
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredType(it)) },
            label = { Text("Tipo") },
            placeholder = { Text("Real, Fantasia, STH, Premium, etc") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.photoUrl.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredPhotoUrl(it)) },
            label = { Text("URL de la Foto") },
            placeholder = { Text("www.imagen.com/asdasd.jpg") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Tags", style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.availableTags.collectAsState().value.forEach { tag ->
                val isSelected = tag.name in viewModel.selectedTags.value

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleTag(tag.name) },
                    label = { Text(tag.name) }
                )
            }
        }



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