package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.CarFormViewModel
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.presentation.common.ConfirmBackButton
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    viewModel: CarFormViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val categories by viewModel.backgroundCategories.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Agregar/Editar Carro") },
                navigationIcon = { ConfirmBackButton(onConfirmBack = onBackClick) }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        viewModel.saveCar(onComplete = onSaveSuccess)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("¡Carro guardado con éxito!")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Guardar") }
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
            CarFormFields(viewModel)
            CarImagePickerSection(viewModel)
            CarTagsSection(viewModel)
            CarBackgroundSection(viewModel, categories)
        }
    }
}

@Composable
fun CarImagePickerSection(viewModel: CarFormViewModel) {
    var showImagePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = viewModel.photoUrl.value,
        onValueChange = { viewModel.onPhotoUrlChange(it) },
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

    Button(
        onClick = { showImagePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Elegir imagen desde la galería") }

    if (showImagePicker) {
        CarImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { url -> viewModel.onPhotoUrlChange(url) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarFormFields(viewModel: CarFormViewModel) {
    var expandedBrand by remember { mutableStateOf(false) }
    var expandedSerie by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    val brandSuggestions = viewModel.brandSuggestions.collectAsState().value
    val serieSuggestions = viewModel.serieSuggestions.collectAsState().value
    val yearSuggestions = viewModel.yearSuggestions.collectAsState().value
    val typeSuggestions = viewModel.typeSuggestions.collectAsState().value
    val colorSuggestions = viewModel.colorSuggestions.collectAsState().value

    // Dropdown helper
    @Composable
    fun ExposedDropdownField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        suggestions: List<String>,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit
    ) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                suggestions.filter { it.contains(value, ignoreCase = true) }.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }

    ExposedDropdownField(
        value = viewModel.brand.value,
        onValueChange = { viewModel.onBrandChange(it) },
        label = "Marca",
        suggestions = brandSuggestions,
        expanded = expandedBrand,
        onExpandedChange = { expandedBrand = it }
    )

    OutlinedTextField(
        value = viewModel.name.value,
        onValueChange = { viewModel.onNameChange(it) },
        label = { Text("Nombre") },
        placeholder = { Text("Honda Civic, Reverb, etc.") },
        modifier = Modifier.fillMaxWidth()
    )

    ExposedDropdownField(
        value = viewModel.serie.value,
        onValueChange = { viewModel.onSerieChange(it) },
        label = "Serie",
        suggestions = serieSuggestions,
        expanded = expandedSerie,
        onExpandedChange = { expandedSerie = it }
    )

    ExposedDropdownField(
        value = viewModel.year.value,
        onValueChange = { viewModel.onYearChange(it) },
        label = "Año",
        suggestions = yearSuggestions,
        expanded = expandedYear,
        onExpandedChange = { expandedYear = it }
    )

    ExposedDropdownField(
        value = viewModel.type.value,
        onValueChange = { viewModel.onTypeChange(it) },
        label = "Tipo",
        suggestions = typeSuggestions,
        expanded = expandedType,
        onExpandedChange = { expandedType = it }
    )

    ExposedDropdownField(
        value = viewModel.color.value,
        onValueChange = { viewModel.onColorChange(it) },
        label = "Color",
        suggestions = colorSuggestions,
        expanded = expandedColor,
        onExpandedChange = { expandedColor = it }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarTagsSection(viewModel: CarFormViewModel) {
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    Text("Tags", style = MaterialTheme.typography.titleMedium)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableTags.forEach { tag ->
            val isSelected = tag.name in selectedTags
            val orderNumber = if (isSelected) selectedTags.indexOf(tag.name) + 1 else null
            val backgroundColor = Color(tag.color?.toColorInt() ?: 0)

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
}

@Composable
fun CarBackgroundSection(
    viewModel: CarFormViewModel,
    availableCategories: List<BackgroundCategory>
) {
    Text("Selecciona un fondo", style = MaterialTheme.typography.titleMedium)

    BackgroundSelector(
        availableCategories = availableCategories,
        selectedBackground = viewModel.backgroundName.value,
        onBackgroundSelected = { viewModel.onBackgroundNameChange(it) }
    )
}




