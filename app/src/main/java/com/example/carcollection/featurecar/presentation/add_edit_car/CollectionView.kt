// presentation/main/MainScreen.kt
package com.example.carcollection.featurecar.presentation.add_edit_car

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.featureAchievements.presentation.AchievementUnlockedPopup
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featuremenu.main.components.CarCard
import com.example.carcollection.presentation.navigation.NavRoutes

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
    viewModel: CarViewModel,
    modifier: Modifier = Modifier,
    expanded: Boolean
) {
    val allBrands by viewModel.allBrands.collectAsState()
    val allYears by viewModel.allYears.collectAsState()
    val allSeries by viewModel.allSeries.collectAsState()
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSeries by viewModel.selectedSeries.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    Column(modifier = modifier) {

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Filtros", style = MaterialTheme.typography.titleSmall)

                    if (selectedBrand != null || selectedYear != null || selectedSeries != null || selectedTag != null) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Limpiar")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DropdownMenuBox(
                        selectedOption = selectedBrand ?: "Marcas",
                        options = listOf("Marcas") + allBrands,
                        onOptionSelected = {
                            viewModel.onBrandSelected(if (it == "Marcas") null else it)
                        }
                    )

                    DropdownMenuBox(
                        selectedOption = selectedYear ?: "Años",
                        options = listOf("Años") + allYears,
                        onOptionSelected = {
                            viewModel.onYearSelected(if (it == "Años") null else it)
                        }
                    )
                    DropdownMenuBox(
                        selectedOption = selectedSeries ?: "Serie",
                        options = listOf("Serie") + allSeries,
                        onOptionSelected = {
                            viewModel.onSeriesSelected(if (it == "Serie") null else it)
                        }
                    )

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

    val itemsPerPageOptions = listOf(5, 10, 20, 50)
    var itemsPerPage by remember { mutableIntStateOf(viewModel.savedItemsPerPage.value) } // Changed to remember
    // Corrected line:
    var currentPage by rememberSaveable { mutableIntStateOf(viewModel.savedPage.value) }


    val totalPages = maxOf(1, (carsList.size + itemsPerPage - 1) / itemsPerPage)
    val paginatedCars = carsList.drop(currentPage * itemsPerPage).take(itemsPerPage)

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    var carToDelete by remember { mutableStateOf<Car?>(null) }

    var expanded by rememberSaveable { mutableStateOf(false) }


    val unlockedAchievement by viewModel.unlockedAchievement.collectAsState()


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Text(
                            "Mi Colección",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Total",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            "(${carsList.size})",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                            contentDescription = "Toggle Filters"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.savedPage.value = currentPage
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

            FilterSection(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded
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
            if (paginatedCars.isEmpty()){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        "No se encontraron autos con los filtros aplicados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else{
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),  // Animar cambios en tamaño del LazyColumn
                    state = listState
                ) {
                    itemsIndexed(paginatedCars, key = { index, car -> car.id ?: index.toString() }) { _, car ->
                    // Animar el item individualmente
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            CarCard(
                                car = car,
                                allTags = allTags,
                                modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(300)),
                                onEdit = {
                                    viewModel.savedPage.value = currentPage
                                    onEditCar(car.id.toString()) // 👈 Ya no lo conviertas a String, porque ya lo es
                                },
                                onDelete = { carToDelete = car },
                                onClick = {
                                    car.id?.let { id ->
                                        navController.navigate("${NavRoutes.DETAIL}/${car.id}")
                                    } ?: run {
                                        // Opcional: mostrar snackbar o log
                                        Log.w("CollectionView",
                                            "Car id es null, no se puede navegar$car"
                                        )
                                    }
                                }

                            )
                        }
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

            unlockedAchievement?.let { achievement ->
                AchievementUnlockedPopup(
                    achievement = achievement,
                    onDismiss = { viewModel.notifyAchievementUnlocked(null) }
                )
            }
        }
    }
}
