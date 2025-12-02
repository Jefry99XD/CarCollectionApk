package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.mutableIntStateOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarImagePickerDialog(
    context: Context = LocalContext.current,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var allCarEntries by remember { mutableStateOf<List<CarLibraryEntry>>(emptyList()) }
    var selectedCarEntry by remember { mutableStateOf<CarLibraryEntry?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 20

    LaunchedEffect(Unit) {
        allCarEntries = try {
            val inputStream = context.assets.open("diecast_images.json")
            val json = inputStream.bufferedReader().use { it.readText() }

            println("🔍 ImagePicker: Loading JSON, length = ${json.length}")

            val gson = Gson()
            val carLibraryEntries = try {
                val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
                gson.fromJson<List<CarLibraryEntry>>(json, typeArray)
            } catch (_: Exception) {
                println("⚠️ ImagePicker: Array parsing failed, trying single object")
                val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                listOf(gson.fromJson<CarLibraryEntry>(json, typeSingle))
            }

            println("🚗 ImagePicker: Loaded ${carLibraryEntries.size} car entries")
            carLibraryEntries
        } catch (e: Exception) {
            println("❌ ImagePicker: Error loading images - ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    if (selectedCarEntry == null) {
        // Paso 1: Seleccionar modelo
        CarModelSelectionDialog(
            carEntries = allCarEntries,
            searchText = searchText,
            onSearchTextChange = {
                searchText = it
                currentPage = 0
            },
            currentPage = currentPage,
            pageSize = pageSize,
            onPageChange = { currentPage = it },
            onCarSelected = { selectedCarEntry = it },
            onDismiss = onDismiss
        )
    } else {
        // Paso 2: Seleccionar variación
        CarVariationSelectionDialog(
            carEntry = selectedCarEntry!!,
            onVariationSelected = { url ->
                onImageSelected(url)
                onDismiss()
            },
            onBack = { selectedCarEntry = null },
            onDismiss = onDismiss
        )
    }
}

@Composable
fun CarModelSelectionDialog(
    carEntries: List<CarLibraryEntry>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onCarSelected: (CarLibraryEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredCars = carEntries.filter {
        it.name?.contains(searchText, ignoreCase = true) != false
    }

    val pageCount = (filteredCars.size + pageSize - 1) / pageSize
    val currentCars = filteredCars.drop(currentPage * pageSize).take(pageSize)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecciona un modelo") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    label = { Text("Buscar modelo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCars.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(currentCars.size) { index ->
                            val car = currentCars[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCarSelected(car) },
                                elevation = CardDefaults.elevatedCardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    car.variations?.firstOrNull()?.url?.let { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = car.name,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(60.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = car.name ?: "Sin nombre",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${car.variations?.size ?: 0} variaciones",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { onPageChange(currentPage - 1) },
                            enabled = currentPage > 0
                        ) {
                            Text("Anterior")
                        }

                        Text("Página ${currentPage + 1} de $pageCount")

                        TextButton(
                            onClick = { onPageChange(currentPage + 1) },
                            enabled = (currentPage + 1) < pageCount
                        ) {
                            Text("Siguiente")
                        }
                    }
                } else {
                    Text(
                        text = "No se encontraron modelos.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun CarVariationSelectionDialog(
    carEntry: CarLibraryEntry,
    onVariationSelected: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            Row {
                TextButton(onClick = onBack) {
                    Text("← Volver")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        },
        title = {
            Column {
                Text(carEntry.name ?: "Modelo")
                Text(
                    text = "${carEntry.variations?.size ?: 0} variaciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(carEntry.variations?.size ?: 0) { index ->
                    val variation = carEntry.variations?.get(index)
                    variation?.let { v ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    v.url?.let { onVariationSelected(it) }
                                },
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = v.url,
                                    contentDescription = "${v.color} - ${v.year}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    v.year?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    v.color?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1
                                        )
                                    }
                                    v.series?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}




