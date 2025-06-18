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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.local.Tag
import androidx.core.graphics.toColorInt


fun getContrastingTextColor(background: Color): Color {
    return if (background.luminance() > 0.5) Color.Black else Color.White
}


@Composable
fun CarCard(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    allTags: List<Tag>
) {
    val firstTagColor = car.tags.firstOrNull()?.let { tagName ->
        allTags.find { it.name == tagName }?.color
    } ?: "#FFFFFF" // Blanco por defecto

    val cardColor = try {
        Color(firstTagColor.toColorInt())
    } catch (e: Exception) {
        Color.White
    }
    val textColor = getContrastingTextColor(cardColor)
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor) // <-- aquí
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
                Text(text = car.brand, color = textColor, style = MaterialTheme.typography.titleMedium)
                Text(text = car.name, color = textColor, style = MaterialTheme.typography.bodyLarge)
                Text(text = car.serie, color = textColor, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Color: ${car.color}", color = textColor, style = MaterialTheme.typography.bodySmall)
                Text(text = "Año: ${car.year}", color = textColor, style = MaterialTheme.typography.bodySmall)
                Text(text = "Tipo: ${car.type}", color = textColor, style = MaterialTheme.typography.bodySmall)
                Text(text = "Tags: ${car.tags.joinToString(", ")}", color = textColor, style = MaterialTheme.typography.bodySmall)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.Info, contentDescription = "Ver detalles", tint = textColor)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = textColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = textColor)
                }
            }
        }
    }
}

