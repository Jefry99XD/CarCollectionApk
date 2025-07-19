// presentation/main/MainScreen.kt
package com.example.carcollection.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.data.local.Car
import com.example.carcollection.presentation.main.components.CarCard

@Composable
fun DropdownMenuBox(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption)
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
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val allBrands by viewModel.allBrands.collectAsState()
    val allYears by viewModel.allYears.collectAsState()
    val allSeries by viewModel.allSeries.collectAsState()
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSeries by viewModel.selectedSeries.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Filtrado de coches", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                    contentDescription = if (expanded) "Hide filters" else "Show filters"
                )
            }

            if (selectedBrand != null || selectedYear != null || selectedSeries != null || selectedTag != null) {
                TextButton(onClick = { viewModel.clearFilters() }) {
                    Text("Clear")
                }
            }
        }

        if (expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Brand filter
                DropdownMenuBox(
                    selectedOption = selectedBrand ?: "Marcas",
                    options = listOf("Marcas") + allBrands,
                    onOptionSelected = {
                        viewModel.onBrandSelected(if (it == "Marcas") null else it)
                    }
                )

                // Year filter
                DropdownMenuBox(
                    selectedOption = selectedYear ?: "Años",
                    options = listOf("Años") + allYears,
                    onOptionSelected = {
                        viewModel.onYearSelected(if (it == "Años") null else it)
                    }
                )

                // Series filter
                DropdownMenuBox(
                    selectedOption = selectedSeries ?: "Serie",
                    options = listOf("Serie") + allSeries,
                    onOptionSelected = {
                        viewModel.onSeriesSelected(if (it == "Serie") null else it)
                    }
                )

                // Tag filter
                if (allTags.isNotEmpty()) {
                    val tagNames = allTags.map { it.name }
                    DropdownMenuBox(
                        selectedOption = selectedTag ?: "Tag",
                        options = listOf("Tag") + tagNames,
                        onOptionSelected = {
                            viewModel.onTagSelected(if (it == "Tag") null else it)
                        }
                    )

                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAdd: () -> Unit,
    onBackClick: () -> Unit,
    onEditCar: (Int) -> Unit,
    navController: NavHostController
) {
    val carsList by viewModel.filteredCars.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    val itemsPerPageOptions = listOf(5, 10, 20, 50)
    var itemsPerPage by rememberSaveable { mutableIntStateOf(viewModel.savedItemsPerPage) }

    var currentPage by rememberSaveable { mutableIntStateOf(viewModel.savedPage) }

    val totalPages = maxOf(1, (carsList.size + itemsPerPage - 1) / itemsPerPage)
    val paginatedCars = carsList.drop(currentPage * itemsPerPage).take(itemsPerPage)

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    var carToDelete by remember { mutableStateOf<Car?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mi Colección")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Total",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("(${carsList.size})")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.savedPage = currentPage
                onNavigateToAdd()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add car")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.onSearchQueryChange(it)
                    currentPage = 0
                },
                label = { Text("Buscar...") },
                placeholder = { Text("Search by any field") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterSection(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Items ")
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownMenuBox(
                        selectedOption = itemsPerPage.toString(),
                        options = itemsPerPageOptions.map { it.toString() },
                        onOptionSelected = {
                            itemsPerPage = it.toInt()
                            viewModel.setItemsPerPage(itemsPerPage)
                            currentPage = 0
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pag ${currentPage + 1} / $totalPages")
                    IconButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                    IconButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                state = listState
            ) {
                items(paginatedCars, key = { it.id }) { car ->
                    CarCard(
                        car = car,
                        allTags = allTags, // Pass empty list or actual tags if needed
                        onEdit = {
                            viewModel.savedPage = currentPage
                            onEditCar(car.id)
                        },
                        onDelete = { carToDelete = car },
                        onClick = { navController.navigate("car_detail/${car.id}") }
                    )
                }
            }

            carToDelete?.let { car ->
                AlertDialog(
                    onDismissRequest = { carToDelete = null },
                    title = { Text("Deseas borrarlo?") },
                    text = { Text("Borrar ${car.name}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteCar(car)
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