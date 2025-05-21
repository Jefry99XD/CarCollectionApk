// presentation/main/MainScreen.kt
package com.example.carcollection.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // Import collectAsState
import androidx.compose.runtime.getValue     // Import getValue delegate
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcollection.presentation.main.components.CarCard

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToAdd: () -> Unit) {
    val carsList by viewModel.filteredCars.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar carro")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Buscar por marca o nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = carsList, key = { car -> car.id }) { car ->
                    CarCard(
                        car = car,
                        onClick = { println("Car clicked: ${car.id}") },
                        onEditClick = { /* Aquí puedes navegar al edit */ },
                        onDeleteClick = { viewModel.deleteCar(car) }
                    )

                }
            }
        }
    }
}
