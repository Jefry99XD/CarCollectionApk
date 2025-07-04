package com.example.carcollection.presentation.add_edit_car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Changed to autoMirrored
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember // This import is not used, consider removing it
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    viewModel: AddEditCarViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var expandedColor by remember { mutableStateOf(false) }
    var expandedBrand by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedSerie by remember { mutableStateOf(false) }
    val brandSuggestions = viewModel.brandSuggestions.collectAsState().value
    val yearSuggestions = viewModel.yearSuggestions.collectAsState().value
    val typeSuggestions = viewModel.typeSuggestions.collectAsState().value
    val serieSuggestions = viewModel.serieSuggestions.collectAsState().value
    val colorSuggestions = viewModel.colorSuggestions.collectAsState().value

    val filteredBrandSuggestions = brandSuggestions
        .filter { it.contains(viewModel.brand.value, ignoreCase = true) }
    val filteredYearSuggestions = yearSuggestions
        .filter { it.contains(viewModel.year.value, ignoreCase = true) }
    val filteredTypeSuggestions = typeSuggestions
        .filter { it.contains(viewModel.type.value, ignoreCase = true) }
    val filteredColorSuggestions = colorSuggestions
        .filter { it.contains(viewModel.color.value, ignoreCase = true) }
    val filteredSerieSuggestions = serieSuggestions
        .filter { it.contains(viewModel.serie.value, ignoreCase = true) }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Using AutoMirrored for better LTR/RTL support
                contentDescription = "Volver"
            )
        } // <<<<--- ADD THIS CLOSING BRACE

        ExposedDropdownMenuBox(
            expanded = expandedBrand && brandSuggestions.isNotEmpty(),
            onExpandedChange = { expandedBrand = !expandedBrand && brandSuggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = viewModel.brand.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredBrand(it))
                    expandedBrand = true
                },
                label = { Text("Marca") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedBrand && brandSuggestions.isNotEmpty(),
                onDismissRequest = { expandedBrand = false }
            ) {
                brandSuggestions
                    .filter { it.contains(viewModel.brand.value, ignoreCase = true) }
                    .forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.onEvent(AddEditCarEvent.EnteredBrand(suggestion))
                                expandedBrand = false
                            }
                        )
                    }
            }
        }

        OutlinedTextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.onEvent(AddEditCarEvent.EnteredName(it)) },
            label = { Text("Nombre") },
            placeholder = { Text("Honda Civic, Reverb, etc.") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedSerie && filteredSerieSuggestions.isNotEmpty(),
            onExpandedChange = { expandedSerie = !expandedSerie && filteredSerieSuggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = viewModel.serie.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredSerie(it))
                    expandedSerie = true
                },
                label = { Text("Serie") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedSerie && filteredSerieSuggestions.isNotEmpty(),
                onDismissRequest = { expandedSerie = false }
            ) {
                filteredSerieSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            viewModel.onEvent(AddEditCarEvent.EnteredSerie(suggestion))
                            expandedSerie = false
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expandedYear && filteredYearSuggestions.isNotEmpty(),
            onExpandedChange = { expandedYear = !expandedYear && filteredYearSuggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = viewModel.year.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredYear(it))
                    expandedYear = true
                },
                label = { Text("Año") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedYear && filteredYearSuggestions.isNotEmpty(),
                onDismissRequest = { expandedYear = false }
            ) {
                yearSuggestions
                    .filter { it.contains(viewModel.year.value, ignoreCase = true) }
                    .forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.onEvent(AddEditCarEvent.EnteredYear(suggestion))
                                expandedYear = false
                            }
                        )
                    }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expandedColor && colorSuggestions.isNotEmpty(),
            onExpandedChange = { expandedColor = !expandedColor && colorSuggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = viewModel.color.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredColor(it))
                    expandedColor = true
                },
                label = { Text("Color") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedColor && colorSuggestions.isNotEmpty(),
                onDismissRequest = { expandedColor = false }
            ) {
                colorSuggestions
                    .filter { it.contains(viewModel.color.value, ignoreCase = true) }
                    .forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.onEvent(AddEditCarEvent.EnteredColor(suggestion))
                                expandedColor = false
                            }
                        )
                    }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedType && typeSuggestions.isNotEmpty(),
            onExpandedChange = { expandedType = !expandedType && typeSuggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = viewModel.type.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredType(it))
                    expandedType = true
                },
                label = { Text("Tipo") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedType && typeSuggestions.isNotEmpty(),
                onDismissRequest = { expandedType = false }
            ) {
                typeSuggestions
                    .filter { it.contains(viewModel.type.value, ignoreCase = true) }
                    .forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.onEvent(AddEditCarEvent.EnteredType(suggestion))
                                expandedType = false
                            }
                        )
                    }
            }
        }

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
            val selected = viewModel.selectedTags.collectAsState().value
            viewModel.availableTags.collectAsState().value.forEach { tag ->
                val isSelected = tag.name in selected
                val orderNumber = if (isSelected) selected.indexOf(tag.name) + 1 else null

                val backgroundColor = try {
                    Color(tag.color.toColorInt())
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.surfaceVariant // fallback color si está mal
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleTag(tag.name) },
                    label = {
                        if (orderNumber != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tag.name)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = orderNumber.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .padding(start = 2.dp, end = 2.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else {
                            Text(tag.name)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = backgroundColor,
                        selectedContainerColor = backgroundColor
                    )
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