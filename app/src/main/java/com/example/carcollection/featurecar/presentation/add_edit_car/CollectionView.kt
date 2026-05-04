// presentation/main/MainScreen.kt
package com.example.carcollection.featurecar.presentation.add_edit_car

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featuremenu.main.components.CarCard
import com.example.carcollection.presentation.navigation.NavRoutes

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
    val paginatedCars by viewModel.paginatedCars.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()  // ✅ Collected as state for reactivity
    val snackbarHostState = remember { SnackbarHostState() }

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    // ✅ Recolectar TODAS las listas una sola vez
    val allBrands by viewModel.allBrands.collectAsState()
    val allYears by viewModel.allYears.collectAsState()
    val allSeries by viewModel.allSeries.collectAsState()
    val allColors by viewModel.allColors.collectAsState()
    val allTypes by viewModel.allTypes.collectAsState()
    val allQualities by viewModel.allQualities.collectAsState()

    val sortByState by viewModel.sortBy.collectAsState()
    val sortAscendingState by viewModel.sortAscending.collectAsState()

    // ✅ activeFiltersCount reacts automatically to filterState changes
    val activeFiltersCount by remember {
        derivedStateOf {
            listOfNotNull(
                filterState.brand,
                filterState.year,
                filterState.series,
                filterState.tag,
                filterState.color,
                filterState.type,
                filterState.quality
            ).size
        }
    }

    var carToDelete by remember { mutableStateOf<Car?>(null) }

    var expanded by rememberSaveable { mutableStateOf(false) }

    // Auto-expandir el panel de filtros cuando se activa alguno,
    // pero NO forzar la visibilidad — el botón toggle siempre tiene control.
    LaunchedEffect(activeFiltersCount > 0) {
        if (activeFiltersCount > 0) {
            expanded = true
        }
    }

    // ✅ NUEVO: Estado para forzar refresh cuando vuelve de editar/agregar
    var forceRefresh by remember { mutableStateOf(false) }

    // 🔹 MASS TAG - Estados para selección múltiple
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedCarIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMassTagDialog by remember { mutableStateOf(false) }

    // ✅ Detectar orientación del dispositivo
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    // ✅ Determinar número de columnas para grid
    val gridColumns = when {
        isTablet && isLandscape -> 3
        isTablet -> 2
        isLandscape -> 2
        else -> 1 // Vista de lista en móvil vertical
    }

    // ✅ Solo cargar datos si no están cargados (mejor rendimiento)
    val isLoading by viewModel.isLoading.collectAsState()

    // ✅ MEJORADO: Detectar cuando se vuelve a la pantalla y recargar datos
    LaunchedEffect(forceRefresh) {
        viewModel.loadUserCars()
        viewModel.loadTags()
        forceRefresh = false  // Resetear el flag
    }

    // ✅ Función para refrescar manualmente
    val refreshData = {
        forceRefresh = true
    }

    // ✅ NUEVO: Envolver en Box para agregar overlay de carga
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    // ✅ NUEVO: Botón de refresh
                    IconButton(
                        onClick = { refreshData() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar",
                            tint = if (isLoading)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }

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
            if (isSelectionMode) {
                // 🔹 En modo selección: Botones para asignar tags o cancelar
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón cancelar
                    FloatingActionButton(
                        onClick = {
                            isSelectionMode = false
                            selectedCarIds = emptySet()
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
                    }

                    // Botón asignar tags
                    FloatingActionButton(
                        onClick = {
                            showMassTagDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("+ Tags (${selectedCarIds.size})", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            } else {
                // 🔹 Modo normal: Agregar nuevo carro
                FloatingActionButton(
                    onClick = {
                        onNavigateToAdd()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar auto")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // ✅ Solo el filtro expandible queda fijo (si se desea agregar después)
            // De momento no mostramos FilterSection expandible
            if (expanded) {
                // Filtro section simplificado aquí si es necesario
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Vista responsiva: LazyColumn en móvil vertical, Grid en tablets/horizontal
            if (gridColumns == 1) {
                // 📱 MÓVIL VERTICAL - Lista tradicional
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // 1️⃣ SEARCH BAR (scrolleable)
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                viewModel.onSearchQueryChange(it)
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
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    }

                    // 2️⃣ SORT OPTIONS (scrolleable)
                    item {
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
                                // Selector de ordenamiento
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Orden:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    DropdownMenuBox(
                                        selectedOption = if (sortByState == "date") "Fecha de creación" else "Nombre (A-Z)",
                                        options = listOf("Nombre (A-Z)", "Fecha de creación"),
                                        onOptionSelected = { selected ->
                                            val sortValue = if (selected == "Fecha de creación") "date" else "name"
                                            viewModel.setSortBy(sortValue)
                                        }
                                    )
                                }

                                // Botón de dirección ascendente/descendente
                                IconButton(
                                    onClick = { viewModel.setSortAscending(!sortAscendingState) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (sortAscendingState) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = if (sortAscendingState) "Ascendente" else "Descendente",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // ✅ NUEVO: Botón de recarga manual
                                IconButton(
                                    onClick = { refreshData() },
                                    enabled = !isLoading,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Recargar colección",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (isLoading)
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 2️⃣B FILTROS
                    item {
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Filtros",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (activeFiltersCount > 0) {
                                            TextButton(onClick = { viewModel.clearFilters() }) {
                                                Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                     // Fila 1: Brand, Year
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.brand ?: "Marca",
                                                options = listOf("Marca") + allBrands,
                                                onOptionSelected = {
                                                    viewModel.onBrandSelected(if (it == "Marca") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.year ?: "Año",
                                                options = listOf("Año") + allYears,
                                                onOptionSelected = {
                                                    viewModel.onYearSelected(if (it == "Año") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 2: Series, Color
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.series ?: "Serie",
                                                options = listOf("Serie") + allSeries,
                                                onOptionSelected = {
                                                    viewModel.onSeriesSelected(if (it == "Serie") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.color ?: "Color",
                                                options = listOf("Color") + allColors,
                                                onOptionSelected = {
                                                    viewModel.onColorSelected(if (it == "Color") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 3: Type, Quality
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.type ?: "Tipo",
                                                options = listOf("Tipo") + allTypes,
                                                onOptionSelected = {
                                                    viewModel.onTypeSelected(if (it == "Tipo") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.quality ?: "Calidad",
                                                options = listOf("Calidad") + allQualities,
                                                onOptionSelected = {
                                                    viewModel.onQualitySelected(if (it == "Calidad") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 4: Tags
                                    if (allTags.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                DropdownMenuBox(
                                                    selectedOption = filterState.tag ?: "Tag",
                                                    options = listOf("Tag") + allTags.map { it.name },
                                                    onOptionSelected = {
                                                        viewModel.onTagSelected(if (it == "Tag") null else it)
                                                    }
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
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
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.prevPage() },
                                    enabled = currentPage > 0,
                                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
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

                                Text(
                                    "${currentPage + 1} / $totalPages",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = { viewModel.nextPage() },
                                    enabled = currentPage < totalPages - 1,
                                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
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

                    // 4️⃣ CONTENIDO PAGINADO (carros)
                    if (paginatedCars.isEmpty()) {
                        item {
                            EmptyCarListMessage(carsList)
                        }
                    } else {
                        itemsIndexed(paginatedCars) { _, car ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                // 🔹 Long press: Solo si NO está en modo selección
                                                if (!isSelectionMode) {
                                                    isSelectionMode = true
                                                    selectedCarIds = setOf(car.id ?: "")
                                                }
                                            },
                                            onTap = {
                                                // 🔹 Tap simple
                                                if (isSelectionMode) {
                                                    // En modo selección, tap agrega/quita
                                                    selectedCarIds = if (selectedCarIds.contains(car.id)) {
                                                        selectedCarIds - car.id.orEmpty()
                                                    } else {
                                                        selectedCarIds + car.id.orEmpty()
                                                    }
                                                } else {
                                                    // Modo normal, navegar
                                                    car.id?.let { carId ->
                                                        navController.navigate("${NavRoutes.DETAIL}/$carId")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    // 🔹 Resalte visual cuando está seleccionado
                                    .then(
                                        if (isSelectionMode && selectedCarIds.contains(car.id)) {
                                            Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 3.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(4.dp)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                CarCard(
                                    car = car,
                                    allTags = allTags,
                                    modifier = Modifier.fillMaxWidth(),
                                    onDelete = { carToDelete = car },
                                    onEdit = {
                                        onEditCar(car.id ?: "")
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            // En modo selección, tap hace toggle
                                            selectedCarIds = if (selectedCarIds.contains(car.id)) {
                                                selectedCarIds - car.id.orEmpty()
                                            } else {
                                                selectedCarIds + car.id.orEmpty()
                                            }
                                        } else {
                                            // Modo normal, navegar
                                            car.id?.let { carId ->
                                                navController.navigate("${NavRoutes.DETAIL}/$carId")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 5️⃣ PAGINACIÓN INFERIOR (móvil) — para no tener que subir al terminar la lista
                    if (totalPages > 1) {
                        item {
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.prevPage() },
                                        enabled = currentPage > 0,
                                        colors = IconButtonDefaults.iconButtonColors(
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
                                    Text(
                                        "${currentPage + 1} / $totalPages",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.width(50.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { viewModel.nextPage() },
                                        enabled = currentPage < totalPages - 1,
                                        colors = IconButtonDefaults.iconButtonColors(
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
                    }
                }
            } else {
                // 📱 TABLET/HORIZONTAL - Vista Grid (Todo scrollable)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // 1️⃣ SEARCH BAR
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                viewModel.onSearchQueryChange(it)
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
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    }

                    // 2️⃣ SORT OPTIONS
                    item {
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
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Orden:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                DropdownMenuBox(
                                    selectedOption = if (sortByState == "date") "Fecha de creación" else "Nombre (A-Z)",
                                    options = listOf("Nombre (A-Z)", "Fecha de creación"),
                                    onOptionSelected = { selected ->
                                        val sortValue = if (selected == "Fecha de creación") "date" else "name"
                                        viewModel.setSortBy(sortValue)
                                    }
                                )
                                IconButton(
                                    onClick = { viewModel.setSortAscending(!sortAscendingState) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (sortAscendingState) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = if (sortAscendingState) "Ascendente" else "Descendente",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // ✅ NUEVO: Botón de recarga manual para Tablet
                                IconButton(
                                    onClick = { refreshData() },
                                    enabled = !isLoading,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Recargar colección",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isLoading)
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 2️⃣B FILTROS (Tablet)
                    item {
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Filtros",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (activeFiltersCount > 0) {
                                            TextButton(onClick = { viewModel.clearFilters() }) {
                                                Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    // Fila 1: Brand, Year
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.brand ?: "Marca",
                                                options = listOf("Marca") + allBrands,
                                                onOptionSelected = {
                                                    viewModel.onBrandSelected(if (it == "Marca") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.year ?: "Año",
                                                options = listOf("Año") + allYears,
                                                onOptionSelected = {
                                                    viewModel.onYearSelected(if (it == "Año") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 2: Series, Color
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.series ?: "Serie",
                                                options = listOf("Serie") + allSeries,
                                                onOptionSelected = {
                                                    viewModel.onSeriesSelected(if (it == "Serie") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.color ?: "Color",
                                                options = listOf("Color") + allColors,
                                                onOptionSelected = {
                                                    viewModel.onColorSelected(if (it == "Color") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 3: Type, Quality
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.type ?: "Tipo",
                                                options = listOf("Tipo") + allTypes,
                                                onOptionSelected = {
                                                    viewModel.onTypeSelected(if (it == "Tipo") null else it)
                                                }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            DropdownMenuBox(
                                                selectedOption = filterState.quality ?: "Calidad",
                                                options = listOf("Calidad") + allQualities,
                                                onOptionSelected = {
                                                    viewModel.onQualitySelected(if (it == "Calidad") null else it)
                                                }
                                            )
                                        }
                                    }

                                    // Fila 4: Tags
                                    if (allTags.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                DropdownMenuBox(
                                                    selectedOption = filterState.tag ?: "Tag",
                                                    options = listOf("Tag") + allTags.map { it.name },
                                                    onOptionSelected = {
                                                        viewModel.onTagSelected(if (it == "Tag") null else it)
                                                    }
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3️⃣ Grid responsivo con carros - Renderizar filas directamente
                    if (paginatedCars.isNotEmpty()) {
                        val groupedCars = paginatedCars.chunked(gridColumns)
                        items(groupedCars.size) { index ->
                            val rowCars = groupedCars[index]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowCars.forEach { car ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        // 🔹 Long press: Solo si NO está en modo selección
                                                        if (!isSelectionMode) {
                                                            isSelectionMode = true
                                                            selectedCarIds = setOf(car.id ?: "")
                                                        }
                                                    },
                                                    onTap = {
                                                        // 🔹 Tap simple
                                                        if (isSelectionMode) {
                                                            // En modo selección, tap agrega/quita
                                                            selectedCarIds = if (selectedCarIds.contains(car.id)) {
                                                                selectedCarIds - car.id.orEmpty()
                                                            } else {
                                                                selectedCarIds + car.id.orEmpty()
                                                            }
                                                        } else {
                                                            // Modo normal, navegar
                                                            car.id?.let { carId ->
                                                                navController.navigate("${NavRoutes.DETAIL}/$carId")
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                        CarCard(
                                            car = car,
                                            allTags = allTags,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                // 🔹 Resalte visual cuando está seleccionado (solo en grid)
                                                .then(
                                                    if (isSelectionMode && selectedCarIds.contains(car.id)) {
                                                        Modifier
                                                            .background(
                                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                            .border(
                                                                width = 3.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(4.dp)
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            onDelete = { carToDelete = car },
                                            onEdit = {
                                                onEditCar(car.id ?: "")
                                            },
                                            onClick = {
                                                if (isSelectionMode) {
                                                    // En modo selección, tap hace toggle
                                                    selectedCarIds = if (selectedCarIds.contains(car.id)) {
                                                        selectedCarIds - car.id.orEmpty()
                                                    } else {
                                                        selectedCarIds + car.id.orEmpty()
                                                    }
                                                } else {
                                                    // Modo normal, navegar
                                                    car.id?.let { carId ->
                                                        navController.navigate("${NavRoutes.DETAIL}/$carId")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                // Espacios en blanco para completar la fila si no hay suficientes carros
                                repeat(gridColumns - rowCars.size) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        item {
                            EmptyCarListMessage(carsList)
                        }
                    }

                    // 4️⃣ Controles de paginación al final
                    item {
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
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.prevPage() },
                                    enabled = currentPage > 0,
                                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                        contentColor = if (currentPage > 0)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Página anterior",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    "${currentPage + 1} / $totalPages",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = { viewModel.nextPage() },
                                    enabled = currentPage < totalPages - 1,
                                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                        contentColor = if (currentPage < totalPages - 1)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Página siguiente",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ NUEVO: Indicador de carga overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .size(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Cargando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    }

    // ✅ Diálogo de confirmación para eliminar
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

    // 🔹 Diálogo de Mass Tag
    if (showMassTagDialog) {
        MassAddTagDialog(
            selectedCarsCount = selectedCarIds.size,
            allTags = allTags,
            onConfirm = { tagsToAdd ->
                // Asignar tags a todos los carros seleccionados
                selectedCarIds.forEach { carId ->
                    val car = paginatedCars.find { it.id == carId }
                    if (car != null) {
                        val updatedTags = (car.tags.toMutableList() + tagsToAdd).distinct()
                        viewModel.updateCarTags(carId, updatedTags)
                    }
                }

                // Resetear selección
                isSelectionMode = false
                selectedCarIds = emptySet()
                showMassTagDialog = false
            },
            onDismiss = {
                showMassTagDialog = false
            }
        )
    }
}

// 🔹 Función removida: CarItemRowWithSelection
// Ahora usamos CarCard con long press en lugar de una componente separada

