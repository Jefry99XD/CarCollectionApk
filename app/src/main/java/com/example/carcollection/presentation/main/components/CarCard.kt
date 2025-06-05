package com.example.carcollection.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.data.local.Car

@Composable
fun CarCard(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = car.photoUrl,
                contentDescription = "${car.brand} ${car.name}",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = car.brand, style = MaterialTheme.typography.titleMedium)
                Text(text = car.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = car.serie, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Color: ${car.color}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Año: ${car.year}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Tipo: ${car.type}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Tags: ${car.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.Info, contentDescription = "Ver detalles")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }

            }
        }
    }
}

