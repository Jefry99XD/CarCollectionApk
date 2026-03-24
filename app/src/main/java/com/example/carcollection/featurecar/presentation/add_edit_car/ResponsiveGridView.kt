package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuremenu.main.components.CarCard
import com.example.carcollection.featuretags.domain.Tag
import com.example.carcollection.presentation.navigation.NavRoutes

/**
 * Grid responsivo que muestra carros en columnas
 * Utilizado en tablets o modo horizontal
 * Usa LazyColumn con filas para evitar problemas de constraints infinitas
 */
@Composable
fun ResponsiveGridViewCars(
    paginatedCars: List<Car>,
    allTags: List<Tag>,
    onDelete: (Car) -> Unit,
    onEdit: (Car) -> Unit,
    onClick: (Car) -> Unit,
    navController: NavHostController,
    columns: Int = 2
) {
    // Agrupar carros por filas según el número de columnas
    val groupedCars = paginatedCars.chunked(columns)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        items(groupedCars) { rowCars ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCars.forEach { car ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        CarCard(
                            car = car,
                            allTags = allTags,
                            modifier = Modifier.fillMaxWidth(),
                            onDelete = { onDelete(car) },
                            onEdit = { onEdit(car) },
                            onClick = {
                                car.id?.let { carId ->
                                    navController.navigate("${NavRoutes.DETAIL}/$carId")
                                }
                            }
                        )
                    }
                }
                // Espacios en blanco para completar la fila si no hay suficientes carros
                repeat(columns - rowCars.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Versión compacta del grid para usar dentro de un LazyColumn item
 * No usa fillMaxSize para evitar problemas de constraints infinitas
 */
@Composable
fun ResponsiveGridViewCarsWithinLazyColumn(
    paginatedCars: List<Car>,
    allTags: List<Tag>,
    onDelete: (Car) -> Unit,
    onEdit: (Car) -> Unit,
    onClick: (Car) -> Unit,
    navController: NavHostController,
    columns: Int = 2
) {
    // Agrupar carros por filas según el número de columnas
    val groupedCars = paginatedCars.chunked(columns)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedCars.forEach { rowCars ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCars.forEach { car ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        CarCard(
                            car = car,
                            allTags = allTags,
                            modifier = Modifier.fillMaxWidth(),
                            onDelete = { onDelete(car) },
                            onEdit = { onEdit(car) },
                            onClick = {
                                car.id?.let { carId ->
                                    navController.navigate("${NavRoutes.DETAIL}/$carId")
                                }
                            }
                        )
                    }
                }
                // Espacios en blanco para completar la fila si no hay suficientes carros
                repeat(columns - rowCars.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

