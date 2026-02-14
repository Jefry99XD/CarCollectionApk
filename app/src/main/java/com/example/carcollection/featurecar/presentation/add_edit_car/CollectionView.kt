// presentation/main/MainScreen.kt
package com.example.carcollection.featurecar.presentation.add_edit_car

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featuremenu.main.components.CarCard
import com.example.carcollection.presentation.navigation.NavRoutes

// ✅ Constante movida fuera del composable para evitar recreación
private val ITEMS_PER_PAGE_OPTIONS = listOf(5, 10, 20, 50)

@Composable
fun DropdownMenuBox(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Usar key para mantener estado entre recomposiciones
    var expanded by remember(selectedOption) { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = selectedOption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    viewModel: CarViewModel,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    activeFiltersCount: Int,
    // ✅ Recibir listas como parámetros para evitar recolección duplicada
    allBrands: List<String>,
    allYears: List<String>,
    allSeries: List<String>,
    allTags: List<com.example.carcollection.featuretags.domain.Tag>,
    allColors: List<String>,
    allTypes: List<String>,
    allQualities: List<String>,
    // ✅ Recibir selecciones como parámetros
    selectedBrand: String?,
    selectedYear: String?,
    selectedSeries: String?,
    selectedTag: String?,
    selectedColor: String?,
    selectedType: String?,
    selectedQuality: String?
) {
    // ✅ Ya no recolectamos estados aquí, vienen como parámetros

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header de filtros
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Filtros Activos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = activeFiltersCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        if (activeFiltersCount > 0) {
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpiar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Grid de filtros
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedBrand ?: "Marcas",
                                    options = listOf("Marcas") + allBrands,
                                    onOptionSelected = {
                                        viewModel.onBrandSelected(if (it == "Marcas") null else it)
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedYear ?: "Años",
                                    options = listOf("Años") + allYears,
                                    onOptionSelected = {
                                        viewModel.onYearSelected(if (it == "Años") null else it)
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedSeries ?: "Serie",
                                    options = listOf("Serie") + allSeries,
                                    onOptionSelected = {
                                        viewModel.onSeriesSelected(if (it == "Serie") null else it)
                                    }
                                )
                            }
                            if (allTags.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    val tagNames = allTags.map { it.name }
                                    DropdownMenuBox(
                                        selectedOption = selectedTag ?: "Tag",
                                        options = listOf("Tag") + tagNames,
                                        onOptionSelected = {
                                            viewModel.onTagSelected(if (it == "Tag") null else it)
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        // Nueva fila para color y tipo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedColor ?: "Color",
                                    options = listOf("Color") + allColors,
                                    onOptionSelected = {
                                        viewModel.onColorSelected(if (it == "Color") null else it)
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedType ?: "Tipo",
                                    options = listOf("Tipo") + allTypes,
                                    onOptionSelected = {
                                        viewModel.onTypeSelected(if (it == "Tipo") null else it)
                                    }
                                )
                            }
                        }

                        // Nueva fila para calidad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownMenuBox(
                                    selectedOption = selectedQuality ?: "Calidad",
                                    options = listOf("Calidad") + allQualities,
                                    onOptionSelected = {
                                        viewModel.onQualitySelected(if (it == "Calidad") null else it)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionViewScreen(
    viewModel: CarViewModel,
    onNavigateToAdd: () -> Unit,
    onBackClick: () -> Unit,
    onEditCar: (String) -> Unit,
    navController: NavHostController
) {
    val carsList by viewModel.filteredCars.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    // ✅ Recolectar TODAS las listas una sola vez
    val allBrands by viewModel.allBrands.collectAsState()
    val allYears by viewModel.allYears.collectAsState()
    val allSeries by viewModel.allSeries.collectAsState()
    val allColors by viewModel.allColors.collectAsState()
    val allTypes by viewModel.allTypes.collectAsState()
    val allQualities by viewModel.allQualities.collectAsState()

    // ✅ Recolectar estados de filtros una sola vez (fuera de TopAppBar)
    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSeries by viewModel.selectedSeries.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()

    var itemsPerPage by remember { mutableIntStateOf(viewModel.savedItemsPerPage.value) }
    var currentPage by rememberSaveable { mutableIntStateOf(viewModel.savedPage.value) }

    // ✅ Usar derivedStateOf para optimizar cálculos que dependen de estados
    val totalPages by remember {
        derivedStateOf {
            maxOf(1, (carsList.size + itemsPerPage - 1) / itemsPerPage)
        }
    }

    val paginatedCars by remember {
        derivedStateOf {
            carsList.drop(currentPage * itemsPerPage).take(itemsPerPage)
        }
    }

    // ✅ Calcular activeFiltersCount de forma optimizada
    val activeFiltersCount by remember {
        derivedStateOf {
            listOfNotNull(selectedBrand, selectedYear, selectedSeries, selectedTag, selectedColor, selectedType, selectedQuality).size
        }
    }


    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    var carToDelete by remember { mutableStateOf<Car?>(null) }

    var expanded by rememberSaveable { mutableStateOf(false) }

    // ✅ Solo cargar datos si no están cargados (mejor rendimiento)
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        // Solo cargar si la lista está vacía
        if (carsList.isEmpty() && !isLoading) {
            viewModel.loadUserCars()
            viewModel.loadTags()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Mi Colección",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Badge con contador de autos
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Total",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${carsList.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ✅ Usar valores ya recolectados fuera del TopAppBar
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = activeFiltersCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                                contentDescription = "Toggle Filters"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.savedPage.value = currentPage
                    onNavigateToAdd()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar auto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.onSearchQueryChange(it)
                    currentPage = 0
                },
                label = { Text("Buscar...") },
                placeholder = { Text("Honda Civic...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onSearchQueryChange("")
                            currentPage = 0
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Solo renderizar FilterSection si está expandido
            if (expanded) {
                FilterSection(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    activeFiltersCount = activeFiltersCount,
                    allBrands = allBrands,
                    allYears = allYears,
                    allSeries = allSeries,
                    allTags = allTags,
                    allColors = allColors,
                    allTypes = allTypes,
                    allQualities = allQualities,
                    selectedBrand = selectedBrand,
                    selectedYear = selectedYear,
                    selectedSeries = selectedSeries,
                    selectedTag = selectedTag,
                    selectedColor = selectedColor,
                    selectedType = selectedType,
                    selectedQuality = selectedQuality
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector de items por página
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Mostrar:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DropdownMenuBox(
                            selectedOption = itemsPerPage.toString(),
                            options = ITEMS_PER_PAGE_OPTIONS.map { it.toString() },
                            onOptionSelected = {
                                itemsPerPage = it.toInt()
                                viewModel.setItemsPerPage(itemsPerPage)
                                currentPage = 0
                            }
                        )
                    }

                    // Controles de paginación
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (currentPage > 0)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (currentPage > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Página anterior",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${currentPage + 1} / $totalPages",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (currentPage < totalPages - 1)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (currentPage < totalPages - 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Página siguiente",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (paginatedCars.isEmpty()){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No se encontraron autos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Intenta ajustar los filtros o la búsqueda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            else{
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {
                    itemsIndexed(paginatedCars, key = { index, car -> car.id ?: index.toString() }) { _, car ->
                        // ✅ Eliminar AnimatedVisibility innecesaria (visible = true siempre)
                        // animateItem() ya maneja las animaciones de LazyColumn
                        CarCard(
                            car = car,
                            allTags = allTags,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300)
                            ),
                            onEdit = {
                                viewModel.savedPage.value = currentPage
                                onEditCar(car.id.toString())
                            },
                            onDelete = { carToDelete = car },
                            onClick = {
                                car.id?.let { _ ->
                                    navController.navigate("${NavRoutes.DETAIL}/${car.id}")
                                } ?: run {
                                    Log.w("CollectionView",
                                        "Car id es null, no se puede navegar$car"
                                    )
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            carToDelete?.let { car ->
                AlertDialog(
                    onDismissRequest = { carToDelete = null },
                    title = { Text("Deseas borrarlo?") },
                    text = { Text("Borrar ${car.name}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteCar(car.id.toString())
                            carToDelete = null
                        }) {
                            Text("Borrar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { carToDelete = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }




        }
    }
}
