package com.example.carcollection.presentation.consultas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.CarLibraryEntry
import com.example.carcollection.featurecar.presentation.add_edit_car.CarVariation
import com.example.carcollection.featureWishlist.domain.WishListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarModelLibraryScreen(
    carEntry: CarLibraryEntry,
    onBackClick: () -> Unit,
    wishListViewModel: WishListViewModel = viewModel()
) {
    println("🚗 CarModelLibrary: Showing ${carEntry.name} with ${carEntry.variations?.size} variations")

    val message by wishListViewModel.message.collectAsState()
    val wishlist by wishListViewModel.wishlist.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    // Filtrar variaciones basándose en el texto de búsqueda
    val filteredVariations = remember(searchQuery, carEntry.variations) {
        if (searchQuery.isBlank()) {
            carEntry.variations ?: emptyList()
        } else {
            carEntry.variations?.filter { variation ->
                searchQuery.split(" ").all { term ->
                    val query = term.trim().lowercase()
                    variation.year?.lowercase()?.contains(query) == true ||
                    variation.color?.lowercase()?.contains(query) == true ||
                    variation.series?.lowercase()?.contains(query) == true ||
                    variation.wheelType?.lowercase()?.contains(query) == true ||
                    variation.interiorColor?.lowercase()?.contains(query) == true ||
                    variation.chassisColorType?.lowercase()?.contains(query) == true ||
                    variation.windowColor?.lowercase()?.contains(query) == true ||
                    variation.toyNumber?.lowercase()?.contains(query) == true ||
                    variation.country?.lowercase()?.contains(query) == true ||
                    variation.sticker?.lowercase()?.contains(query) == true ||
                    variation.notes?.lowercase()?.contains(query) == true
                }
            } ?: emptyList()
        }
    }

    // Mostrar mensajes
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            wishListViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = carEntry.name ?: "Modelo sin nombre",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "${carEntry.variations?.size ?: 0} variaciones"
                            } else {
                                "${filteredVariations.size} de ${carEntry.variations?.size ?: 0} variaciones"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Campo de búsqueda (ocupa todo el ancho)
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("Buscar por año, color, serie, ruedas...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Descripción expandible como header (ocupa todo el ancho)
            carEntry.description?.let { description ->
                item(span = { GridItemSpan(2) }) {
                    ExpandableDescriptionCard(description = description)
                }
            }

            // Grid de variaciones filtradas
            items(filteredVariations) { variation ->
                VariationDetailCard(
                    carName = carEntry.name,
                    variation = variation,
                    wishListViewModel = wishListViewModel,
                    wishlist = wishlist
                )
            }

            // Mensaje cuando no hay resultados
            if (filteredVariations.isEmpty() && searchQuery.isNotBlank()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron variaciones que coincidan con \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableDescriptionCard(description: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir"
                )
            }

            Spacer(modifier = Modifier.padding(4.dp))

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isExpanded) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VariationDetailCard(
    carName: String?,
    variation: CarVariation,
    wishListViewModel: WishListViewModel,
    wishlist: List<Car>
) {
    // Verificar si esta variación ya está en la wishlist
    val isInWishlist = remember(wishlist, carName, variation) {
        wishlist.any { car ->
            car.name == carName &&
            car.year == variation.year &&
            car.color == variation.color &&
            car.serie == variation.series
        }
    }

    // Encontrar el ID del item en wishlist si existe
    val wishlistItemId = remember(wishlist, carName, variation) {
        wishlist.find { car ->
            car.name == carName &&
            car.year == variation.year &&
            car.color == variation.color &&
            car.serie == variation.series
        }?.id
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart button at top right corner
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (isInWishlist && wishlistItemId != null) {
                            // Si ya está en wishlist, eliminarlo
                            wishListViewModel.removeFromWishlist(wishlistItemId)
                        } else {
                            // Si no está, agregarlo
                            val carToAdd = Car(
                                id = "", // Will be auto-generated
                                name = carName ?: "Unknown",
                                brand = "Hot Wheels", // Default brand from library
                                year = variation.year,
                                serie = variation.series,
                                color = variation.color,
                                photoUrl = variation.url,
                                type = null,
                                tags = emptyList()
                            )
                            wishListViewModel.addToWishlist(carToAdd)
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isInWishlist) "Eliminar de lista de deseados" else "Agregar a lista de deseados",
                        tint = if (isInWishlist) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Imagen de la variación
            variation.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${variation.color} - ${variation.year}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // Información principal
            variation.year?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            variation.color?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            variation.series?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            // Información adicional en formato compacto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                variation.wheelType?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Ruedas", value = it)
                }

                variation.interiorColor?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Interior", value = it)
                }

                variation.chassisColorType?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Chasis", value = it)
                }

                variation.windowColor?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Ventanas", value = it)
                }

                variation.toyNumber?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Toy #", value = it)
                }

                variation.country?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "País", value = it)
                }

                variation.sticker?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Sticker", value = it)
                }

                variation.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = "Nota: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 3
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End,
            maxLines = 2
        )
    }
}

