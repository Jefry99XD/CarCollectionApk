package com.example.carcollection.featuremenu.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.carcollection.R
import com.example.carcollection.data.local.Car
import com.example.carcollection.featuretags.domain.Tag

fun getContrastingTextColor(background: Color): Color {
    return if (background.luminance() > 0.5) Color.Black else Color.White
}
@Composable
fun CarCard(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    allTags: List<Tag>,
    modifier: Modifier = Modifier
) {
    val firstTagColor = car.tags.firstOrNull()?.let { tagName ->
        allTags.find { it.name == tagName }?.color
    } ?: "#FFFFFF"

    val cardColor = try {
        Color(firstTagColor.toColorInt())
    } catch (_: Exception) {
        Color.White
    }
    val textColor = getContrastingTextColor(cardColor)

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp), // Menor separación entre cards
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp) // Reducido de 12.dp
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = car.photoUrl,
                contentDescription = "${car.brand} ${car.name}",
                placeholder = painterResource(R.drawable.placeholder),
                modifier = Modifier
                    .size(100.dp) // Reducido de 140.dp
                    .background(Color.Transparent)
            )

            Spacer(modifier = Modifier.width(8.dp)) // Reducido

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = car.name,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium, // Más compacto
                    maxLines = 1
                )
                Text(
                    text = car.brand,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                car.tags.firstOrNull()?.let { firstTag ->
                    Text(
                        text = firstTag,
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver detalles",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}



