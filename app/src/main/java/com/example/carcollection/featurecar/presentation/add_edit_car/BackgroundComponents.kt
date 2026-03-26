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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Componente base reutilizable para thumbnail de fondo
 * @param background El item de fondo a mostrar
 * @param size Tamaño del thumbnail (default: 80.dp)
 * @param isSelected Si el thumbnail está seleccionado
 * @param onClick Callback cuando se hace click
 * @param showLabel Si se muestra el nombre del fondo debajo
 * @param modifier Modifier adicional
 */
@Composable
fun GenericBackgroundThumbnail(
    background: BackgroundItem,
    size: Dp = 80.dp,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    showLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
        ) {
            // 🔹 Usar imagen optimizada con tamaño reducido
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(background.thumbnailUrl)
                    .size(
                        width = (size.value * 2).toInt(),
                        height = (size.value * 2).toInt()
                    )
                    .crossfade(300)
                    .build(),
                contentDescription = background.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay de selección
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

        if (showLabel) {
            Text(
                background.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

/**
 * Componente para mostrar fila de fondos (LazyRow horizontal)
 * @param backgrounds Lista de fondos a mostrar
 * @param selectedId ID del fondo seleccionado
 * @param onSelect Callback cuando se selecciona un fondo
 * @param thumbnailSize Tamaño de cada thumbnail
 * @param showLabels Si se muestran los nombres
 */
@Composable
fun GenericBackgroundRow(
    backgrounds: List<BackgroundItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    thumbnailSize: Dp = 80.dp,
    showLabels: Boolean = false
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(backgrounds) { background ->
            GenericBackgroundThumbnail(
                background = background,
                size = thumbnailSize,
                isSelected = background.id == selectedId,
                onClick = { onSelect(background.id) },
                showLabel = showLabels
            )
        }
    }
}

/**
 * Componente para mostrar grid de fondos (LazyVerticalGrid)
 * @param backgrounds Lista de fondos a mostrar
 * @param selectedId ID del fondo seleccionado
 * @param onSelect Callback cuando se selecciona un fondo
 * @param columns Número de columnas en el grid (default: 3)
 * @param thumbnailSize Tamaño de cada thumbnail
 */
@Composable
fun GenericBackgroundGrid(
    backgrounds: List<BackgroundItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    columns: Int = 3,
    thumbnailSize: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(backgrounds) { background ->
            GenericBackgroundThumbnail(
                background = background,
                size = thumbnailSize,
                isSelected = background.id == selectedId,
                onClick = { onSelect(background.id) },
                showLabel = true
            )
        }
    }
}

/**
 * Componente para mostrar múltiples categorías de fondos en filas (landscape)
 * @param categories Lista de categorías de fondos
 * @param selectedId ID del fondo seleccionado
 * @param onSelect Callback cuando se selecciona un fondo
 */
@Composable
fun BackgroundCategoriesRowView(
    categories: List<BackgroundCategory>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            BackgroundCategoryLabel(category.category)
            GenericBackgroundRow(
                backgrounds = category.backgrounds,
                selectedId = selectedId,
                onSelect = onSelect,
                thumbnailSize = 80.dp,
                showLabels = false
            )
        }
    }
}

/**
 * Componente para mostrar múltiples categorías de fondos en grid (diálogo)
 * @param categories Lista de categorías de fondos
 * @param selectedId ID del fondo seleccionado
 * @param onSelect Callback cuando se selecciona un fondo
 */
@Composable
fun BackgroundCategoriesGridView(
    categories: List<BackgroundCategory>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            BackgroundCategoryLabel(category.category)
            GenericBackgroundGrid(
                backgrounds = category.backgrounds,
                selectedId = selectedId,
                onSelect = onSelect,
                columns = 3,
                thumbnailSize = 100.dp
            )
        }
    }
}

/**
 * Label para categoría de fondos
 */
@Composable
private fun BackgroundCategoryLabel(categoryName: String) {
    Text(
        text = categoryName,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

