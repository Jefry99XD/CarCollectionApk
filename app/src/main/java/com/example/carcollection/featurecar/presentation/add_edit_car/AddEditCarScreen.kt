package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.CarFormViewModel
import com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen.LogoSelectorDialog
import com.example.carcollection.presentation.common.ConfirmBackButton
import kotlinx.coroutines.launch

// ✅ Constantes movidas fuera del composable
private val QUALITY_OPTIONS = listOf("Basico", "TH", "STH", "Premium", "Silver Series", "RLC", "Chase")

// ✅ ExposedDropdownField extraído como función top-level con optimizaciones
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    sortDescending: Boolean = false
) {
    // ✅ Memoizar el sorting para evitar recalcular en cada recomposición
    val sortedSuggestions = remember(suggestions, value, sortDescending) {
        suggestions
            .filter { it.contains(value, ignoreCase = true) }
            .let { filtered ->
                if (sortDescending) {
                    filtered.sortedByDescending { it.lowercase() }
                } else {
                    filtered.sortedBy { it.lowercase() }
                }
            }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        // ✅ Solo mostrar menu si está expandido
        if (expanded) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                sortedSuggestions.forEach { suggestion ->
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
}

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
    var showBackConfirmation by remember { mutableStateOf(false) }
    var addAnotherCar by remember { mutableStateOf(false) }

    // Handle physical back button
    BackHandler {
        showBackConfirmation = true
    }

    // Confirmation dialog for physical back button
    if (showBackConfirmation) {
        AlertDialog(
            onDismissRequest = { showBackConfirmation = false },
            title = { Text("¿Deseas salir?") },
            text = { Text("Perderás los cambios no guardados. ¿Deseas salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirmation = false
                    onBackClick()
                }) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agregar/Editar Carro",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = { ConfirmBackButton(onConfirmBack = onBackClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Checkbox para agregar otro carro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = addAnotherCar,
                            onCheckedChange = { addAnotherCar = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agregar otro carro después de guardar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botón de guardar
                    Button(
                        onClick = {
                            viewModel.saveCar(onComplete = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("¡Carro guardado con éxito!")
                                }

                                if (addAnotherCar) {
                                    // Resetear solo name y photoUrl para agregar otro carro
                                    viewModel.resetForNewCar()
                                } else {
                                    // Salir de la pantalla
                                    onSaveSuccess()
                                }
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (addAnotherCar) "Guardar y Agregar Otro" else "Guardar Carro",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Info Section
            SectionCard(
                title = "Información del Carro",
                icon = Icons.Default.Info
            ) {
                CarFormFields(viewModel)
            }

            // Image Section
            SectionCard(
                title = "Imagen",
                icon = Icons.Default.Image
            ) {
                CarImagePickerSection(viewModel)
            }

            // Tags Section
            SectionCard(
                title = "Etiquetas",
                icon = Icons.Default.LocalOffer
            ) {
                CarTagsSection(viewModel)
            }

            // Background Section
            SectionCard(
                title = "Fondo",
                icon = Icons.Default.Palette
            ) {
                CarBackgroundSection(viewModel, categories)
            }

            // Extra spacing at the bottom
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
fun CarImagePickerSection(viewModel: CarFormViewModel) {
    var showImagePicker by remember { mutableStateOf(false) }
    var showLogoSelector by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = viewModel.photoUrl.value,
        onValueChange = { viewModel.onPhotoUrlChange(it) },
        label = { Text("URL de la Foto") },
        placeholder = { Text("www.imagen.com/asdasd.jpg") },
        modifier = Modifier.fillMaxWidth()
    )

    if (viewModel.photoUrl.value.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            AsyncImage(
                model = viewModel.photoUrl.value,
                contentDescription = "Vista previa de imagen",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp),
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { showImagePicker = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Galería")
        }

        Button(
            onClick = { showLogoSelector = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logo")
        }
    }

    if (showImagePicker) {
        CarImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { url -> viewModel.onPhotoUrlChange(url) }
        )
    }

    if (showLogoSelector) {
        LogoSelectorDialog(
            onDismiss = { showLogoSelector = false },
            onLogoSelected = { url -> viewModel.onPhotoUrlChange(url) }
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
    var expandedQuality by remember { mutableStateOf(false) }

    // ✅ Usar by collectAsState() en lugar de .collectAsState().value
    val brandSuggestions by viewModel.brandSuggestions.collectAsState()
    val serieSuggestions by viewModel.serieSuggestions.collectAsState()
    val yearSuggestions by viewModel.yearSuggestions.collectAsState()
    val typeSuggestions by viewModel.typeSuggestions.collectAsState()
    val colorSuggestions by viewModel.colorSuggestions.collectAsState()

    // ✅ Detectar orientación y tamaño de pantalla
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    // ✅ Determinar número de columnas para grid de campos
    val columns = when {
        isTablet && isLandscape -> 3
        isTablet -> 2
        isLandscape -> 2
        else -> 1
    }

    // MARCA
    if (columns == 1) {
        ExposedDropdownField(
            value = viewModel.brand.value,
            onValueChange = { viewModel.onBrandChange(it) },
            label = "Marca",
            suggestions = brandSuggestions,
            expanded = expandedBrand,
            onExpandedChange = { expandedBrand = it }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownField(
                    value = viewModel.brand.value,
                    onValueChange = { viewModel.onBrandChange(it) },
                    label = "Marca",
                    suggestions = brandSuggestions,
                    expanded = expandedBrand,
                    onExpandedChange = { expandedBrand = it }
                )
            }
            // SERIE
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownField(
                    value = viewModel.serie.value,
                    onValueChange = { viewModel.onSerieChange(it) },
                    label = "Serie",
                    suggestions = serieSuggestions,
                    expanded = expandedSerie,
                    onExpandedChange = { expandedSerie = it }
                )
            }
            if (columns >= 3) {
                // AÑO (solo si hay 3+ columnas)
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownField(
                        value = viewModel.year.value,
                        onValueChange = { viewModel.onYearChange(it) },
                        label = "Año",
                        suggestions = yearSuggestions,
                        expanded = expandedYear,
                        onExpandedChange = { expandedYear = it },
                        sortDescending = true
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // NOMBRE (siempre full width)
    OutlinedTextField(
        value = viewModel.name.value,
        onValueChange = { viewModel.onNameChange(it) },
        label = { Text("Nombre") },
        placeholder = { Text("Honda Civic, Reverb, etc.") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Si no estamos en tablet/horizontal, mostrar año aquí
    if (columns < 3) {
        ExposedDropdownField(
            value = viewModel.year.value,
            onValueChange = { viewModel.onYearChange(it) },
            label = "Año",
            suggestions = yearSuggestions,
            expanded = expandedYear,
            onExpandedChange = { expandedYear = it },
            sortDescending = true
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    // TIPO y COLOR
    if (columns == 1) {
        ExposedDropdownField(
            value = viewModel.type.value,
            onValueChange = { viewModel.onTypeChange(it) },
            label = "Tipo",
            suggestions = typeSuggestions,
            expanded = expandedType,
            onExpandedChange = { expandedType = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownField(
            value = viewModel.color.value,
            onValueChange = { viewModel.onColorChange(it) },
            label = "Color",
            suggestions = colorSuggestions,
            expanded = expandedColor,
            onExpandedChange = { expandedColor = it }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownField(
                    value = viewModel.type.value,
                    onValueChange = { viewModel.onTypeChange(it) },
                    label = "Tipo",
                    suggestions = typeSuggestions,
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownField(
                    value = viewModel.color.value,
                    onValueChange = { viewModel.onColorChange(it) },
                    label = "Color",
                    suggestions = colorSuggestions,
                    expanded = expandedColor,
                    onExpandedChange = { expandedColor = it }
                )
            }
            if (columns >= 3) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownField(
                        value = viewModel.quality.value,
                        onValueChange = { viewModel.onQualityChange(it) },
                        label = "Calidad",
                        suggestions = QUALITY_OPTIONS,
                        expanded = expandedQuality,
                        onExpandedChange = { expandedQuality = it }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // CALIDAD (si no está en la fila anterior)
    if (columns < 3) {
        ExposedDropdownField(
            value = viewModel.quality.value,
            onValueChange = { viewModel.onQualityChange(it) },
            label = "Calidad",
            suggestions = QUALITY_OPTIONS,
            expanded = expandedQuality,
            onExpandedChange = { expandedQuality = it }
        )
    }
}

// Función helper para calcular si un color es claro u oscuro
private fun Color.isLightColor(): Boolean {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255

    // Calcular luminancia usando la fórmula estándar
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return luminance > 0.5
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarTagsSection(viewModel: CarFormViewModel) {
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    // ✅ Crear map de índices una sola vez para evitar indexOf() repetido (O(1) en lugar de O(n))
    val selectedTagsIndexMap = remember(selectedTags) {
        selectedTags.withIndex().associate { it.value to it.index + 1 }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableTags.forEach { tag ->
            // ✅ Memoizar cálculos de color por tag para evitar recalcular en cada recomposición
            val backgroundColor = remember(tag.color) {
                Color(tag.color?.toColorInt() ?: 0)
            }

            // ✅ Memoizar textColor basado en backgroundColor
            val textColor = remember(backgroundColor) {
                if (backgroundColor.isLightColor()) {
                    Color.Black
                } else {
                    Color.White
                }
            }

            val isSelected = tag.name in selectedTags
            val orderNumber = selectedTagsIndexMap[tag.name]  // ✅ O(1) lookup en lugar de indexOf()

            FilterChip(
                selected = isSelected,
                onClick = { viewModel.toggleTag(tag.name) },
                label = {
                    if (orderNumber != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tag.name,
                                color = textColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = orderNumber.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                modifier = Modifier
                                    .padding(start = 2.dp, end = 2.dp)
                                    .background(
                                        color = textColor.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        Text(
                            text = tag.name,
                            color = textColor
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = backgroundColor,
                    selectedContainerColor = backgroundColor,
                    labelColor = textColor,
                    selectedLabelColor = textColor
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
    var showBackgroundPicker by remember { mutableStateOf(false) }

    // Obtener nombre del fondo seleccionado
    val selectedBackground = remember(viewModel.backgroundName.value, availableCategories) {
        availableCategories
            .flatMap { it.backgrounds }
            .find { it.id == viewModel.backgroundName.value }
    }

    // Mostrar preview del fondo seleccionado
    if (selectedBackground != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = selectedBackground.url,
                    contentDescription = selectedBackground.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fondo: ${selectedBackground.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Botón para abrir selector de fondos
    Button(
        onClick = { showBackgroundPicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Cambiar Fondo")
    }

    // Diálogo de selección
    if (showBackgroundPicker) {
        BackgroundPickerDialog(
            categories = availableCategories,
            selectedBackgroundId = viewModel.backgroundName.value,
            onBackgroundSelected = { viewModel.onBackgroundNameChange(it) },
            onDismiss = { showBackgroundPicker = false }
        )
    }
}




