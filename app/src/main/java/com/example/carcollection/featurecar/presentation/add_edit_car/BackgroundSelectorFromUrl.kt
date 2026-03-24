package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BackgroundSelectorFromUrl(
    availableCategories: List<BackgroundCategory>,
    selectedBackgroundId: String,
    onBackgroundSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        availableCategories.forEach { category ->
            BackgroundCategoryRowFromUrl(
                category = category,
                selectedBackgroundId = selectedBackgroundId,
                onBackgroundSelected = onBackgroundSelected
            )
        }
    }
}

@Composable
fun BackgroundCategoryRowFromUrl(
    category: BackgroundCategory,
    selectedBackgroundId: String,
    onBackgroundSelected: (String) -> Unit
) {
    Text(
        text = category.category,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
    )

    LazyRow(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(category.backgrounds) { background ->
            BackgroundThumbnailFromUrl(
                background = background,
                isSelected = background.id == selectedBackgroundId,
                onClick = { onBackgroundSelected(background.id) }
            )
        }
    }
}

@Composable
fun BackgroundThumbnailFromUrl(
    background: BackgroundItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .size(80.dp)
    ) {
        // Cargar imagen desde URL
        AsyncImage(
            model = background.thumbnailUrl,
            contentDescription = background.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay cuando está seleccionado
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

