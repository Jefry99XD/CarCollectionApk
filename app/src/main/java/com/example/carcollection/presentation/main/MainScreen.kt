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
import com.example.carcollection.data.local.Car // Assuming Car is in this package
import com.example.carcollection.presentation.main.MainViewModel
import com.example.carcollection.presentation.main.components.CarCard

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToAdd: () -> Unit) {
    // Collect the StateFlow into a State<List<Car>>
    // and then get its value (the actual List<Car>)
    val carsList: List<Car> by viewModel.cars.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar carro")
            }

        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(items = carsList, key = { car -> car.id }) { car -> // Assuming Car has an 'id' property for key
                CarCard(car = car, onClick = {
                    // Aquí puedes navegar o hacer algo con el carro
                    println("Car clicked: ${car.id}")
                })

            }

        }
    }
}