package com.example.carcollection.presentation.add_edit_car

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.serialization.json.Json
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.mutableIntStateOf


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarImagePickerDialog(
    context: Context = LocalContext.current,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var allImages by remember { mutableStateOf<List<CarImageEntry>>(emptyList()) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 30

    LaunchedEffect(Unit) {
        allImages = try {
            val inputStream = context.assets.open("diecast_images.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    val filteredImages = allImages.filter {
        it.name?.contains(searchText, ignoreCase = true) != false
    }

    val pageCount = (filteredImages.size + pageSize - 1) / pageSize
    val currentImages = filteredImages.drop(currentPage * pageSize).take(pageSize)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecciona una imagen") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        currentPage = 0
                    },
                    label = { Text("Buscar por nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredImages.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(currentImages) { entry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onImageSelected(entry.url.toString())
                                        onDismiss()
                                    },
                                elevation = CardDefaults.elevatedCardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = entry.url,
                                        contentDescription = entry.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    )
                                    Text(
                                        text = entry.name.toString(),
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Text("Anterior")
                        }

                        Text("Página ${currentPage + 1} de $pageCount")

                        TextButton(
                            onClick = { if ((currentPage + 1) < pageCount) currentPage++ },
                            enabled = (currentPage + 1) < pageCount
                        ) {
                            Text("Siguiente")
                        }
                    }
                } else {
                    Text(
                        text = "No se encontraron imágenes.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}




