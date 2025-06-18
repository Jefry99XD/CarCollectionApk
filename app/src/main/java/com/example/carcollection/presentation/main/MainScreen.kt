// presentation/main/MainScreen.kt
package com.example.carcollection.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

    val itemsPerPageOptions = listOf(5, 10, 20, 50)
    var itemsPerPage by remember { mutableStateOf(20) }
    var currentPage by rememberSaveable { mutableStateOf(viewModel.savedPage) }


    val totalPages = maxOf(1, (carsList.size + itemsPerPage - 1) / itemsPerPage)

    val paginatedCars = carsList.drop(currentPage * itemsPerPage).take(itemsPerPage)

    var savedPage by mutableStateOf(0)

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Colección")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Total: (${carsList.size})  ")
                    }
                }
                ,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.savedPage = currentPage
                onNavigateToAdd()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar carro")
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
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Buscar") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad ")
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownMenuBox(
                        selectedOption = itemsPerPage.toString(),
                        options = itemsPerPageOptions.map { it.toString() },
                        onOptionSelected = {
                            itemsPerPage = it.toInt()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior")
                    }
                    IconButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente")
                    }
                }
            }



            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(paginatedCars, key = { it.id }) { car ->
                    CarCard(
                        car = car,
                        allTags = allTags,
                        onEdit = {
                            viewModel.savedPage = currentPage
                            navController.navigate("add_edit_car?carId=${car.id}")
                        },

                        onDelete = {
                            viewModel.deleteCar(car)
                        },

                        onClick = {
                            navController.navigate("car_detail/${car.id}")
                        },
                    )
                }
            }


        }
    }
}



