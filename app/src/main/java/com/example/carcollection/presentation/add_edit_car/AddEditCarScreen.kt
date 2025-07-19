package com.example.carcollection.presentation.add_edit_car

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Changed to autoMirrored
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember // This import is not used, consider removing it
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import android.content.Context
import coil.compose.AsyncImage


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    viewModel: AddEditCarViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val backgroundOptions = listOf("fondo", "fondo2")
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar/Editar Carro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(AddEditCarEvent.SaveCar)
                        onSaveSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Guardar")
                }
            }
        }
    ) { padding ->

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ExposedDropdownMenuBox(
            expanded = expandedBrand,
            onExpandedChange = { expandedBrand = it }
        ) {
            OutlinedTextField(
                value = viewModel.brand.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredBrand(it))
                },
                label = { Text("Marca") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrand)
                }
            )

            ExposedDropdownMenu(
                expanded = expandedBrand,
                onDismissRequest = { expandedBrand = false }
            ) {
                filteredBrandSuggestions.forEach { suggestion ->
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
            expanded = expandedSerie,
            onExpandedChange = { expandedSerie = it}
        ) {
            OutlinedTextField(
                value = viewModel.serie.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredSerie(it))
                },
                label = { Text("Serie") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                        trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded =  expandedSerie)
                }
            )
            ExposedDropdownMenu(
                expanded = expandedSerie,
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
            expanded = expandedYear,
            onExpandedChange = { expandedYear = it }
        ) {
            OutlinedTextField(
                value = viewModel.year.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredYear(it))
                },
                label = { Text("Año") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded =  expandedYear)
                }
            )
            ExposedDropdownMenu(
                expanded = expandedYear ,
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
            expanded = expandedColor,
            onExpandedChange = { expandedColor = it}
        ) {
            OutlinedTextField(
                value = viewModel.color.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredColor(it))
                },
                label = { Text("Color") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor)
                }

            )
            ExposedDropdownMenu(
                expanded = expandedColor,
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
            expanded = expandedType,
            onExpandedChange = { expandedType = it }
        ) {
            OutlinedTextField(
                value = viewModel.type.value,
                onValueChange = {
                    viewModel.onEvent(AddEditCarEvent.EnteredType(it))
                },
                label = { Text("Tipo") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                }
            )
            ExposedDropdownMenu(
                expanded = expandedType,
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
        if (viewModel.photoUrl.value.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = viewModel.photoUrl.value,
                contentDescription = "Vista previa de imagen",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(4.dp),
            )
        }

        var showImagePicker by remember { mutableStateOf(false) }

        Button(
            onClick = { showImagePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir imagen desde la galería")
        }

        if (showImagePicker) {
            CarImagePickerDialog(
                onDismiss = { showImagePicker = false },
                onImageSelected = { url ->
                    viewModel.onEvent(AddEditCarEvent.EnteredPhotoUrl(url))
                }
            )
        }



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
        Text("Selecciona un fondo", style = MaterialTheme.typography.titleMedium)

        BackgroundSelector(
            availableBackgrounds = listOf("fondo", "fondo2", "fondo3", "fondo4", "fondo5", "fondo6", "fondo7", "fondo8", "fondo9", "fondo10", "fondo11", "fondo13"),
            selectedBackground = viewModel.backgroundName.value,
            onBackgroundSelected = { viewModel.onEvent(AddEditCarEvent.EnteredBackgroundName(it)) }
        )
    }
}}

@Composable
fun getImageResourceIdByName(name: String): Int? {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
            .takeIf { it != 0 }
    }
}

@DrawableRes
fun getDrawableIdByName(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}


@Composable
fun BackgroundSelector(
    availableBackgrounds: List<String>,
    selectedBackground: String,
    onBackgroundSelected: (String) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        availableBackgrounds.forEach { bgName ->
            val drawableId = getDrawableIdByName(context, bgName)
            if (drawableId != 0) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onBackgroundSelected(bgName) }
                ) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = "Fondo $bgName",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (bgName == selectedBackground) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(20.dp)
                                .background(Color.White, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionado",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


