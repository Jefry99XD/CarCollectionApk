package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuremenu.main.components.CarCard
import com.example.carcollection.featuretags.domain.Tag

/**
 * Renderiza mensaje cuando no hay carros
 */
@Composable
fun EmptyCarListMessage(carsList: List<Car>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (carsList.isEmpty()) "No hay autos en tu colección" else "No se encontraron autos con ese filtro",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Renderiza un item de carro individual
 */
@Composable
fun CarItemRow(
    car: Car,
    allTags: List<Tag>,
    onDelete: (Car) -> Unit,
    onEdit: (Car) -> Unit,
    onClick: (Car) -> Unit
) {
    CarCard(
        car = car,
        allTags = allTags,
        modifier = Modifier.fillMaxWidth(),
        onDelete = { onDelete(car) },
        onEdit = { onEdit(car) },
        onClick = { onClick(car) }
    )
}

