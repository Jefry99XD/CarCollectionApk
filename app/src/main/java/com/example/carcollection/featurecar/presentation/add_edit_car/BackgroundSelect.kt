package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundSelector(
    availableCategories: List<BackgroundCategory>,
    selectedBackground: String,
    onBackgroundSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        availableCategories.forEach { category ->
            BackgroundCategoryRow(
                category = category,
                selectedBackground = selectedBackground,
                onBackgroundSelected = onBackgroundSelected
            )
        }
    }
}

@Composable
fun BackgroundCategoryRow(
    category: BackgroundCategory,
    selectedBackground: String,
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
            BackgroundThumbnail(
                background = background,
                isSelected = background.resource == selectedBackground,
                onClick = { onBackgroundSelected(background.resource) }
            )
        }
    }
}

@Composable
fun BackgroundThumbnail(
    background: BackgroundItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val thumbResId = backgroundResourceMap["${background.resource}_thumb"]
    val fullResId = backgroundResourceMap[background.resource]
    val resId = thumbResId ?: fullResId

    if (resId != null) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = background.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

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
}