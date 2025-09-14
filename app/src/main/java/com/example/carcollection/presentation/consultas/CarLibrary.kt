package com.example.carcollection.presentation.consultas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.carcollection.presentation.add_edit_car.CarImageEntry
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBackClick: () -> Unit,
    viewModel: CarLibraryViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cars by viewModel.paginatedCars.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    // Estado para el modal de imagen
    val selectedImageUrl = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca General") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearch,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre") },
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cars) { car ->
                    CarLibraryCard(car) {
                        selectedImageUrl.value = it
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.prevPage() }) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                    Text("Anterior")
                }
                Text("Pág ${currentPage + 1}")
                TextButton(onClick = { viewModel.nextPage() }) {
                    Text("Siguiente")
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                }
            }
        }

        // Modal con la imagen ampliada
        selectedImageUrl.value?.let { imageUrl ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { selectedImageUrl.value = null },
                confirmButton = {
                    TextButton(onClick = { selectedImageUrl.value = null }) {
                        Text("Cerrar")
                    }
                },
                text = {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen ampliada",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    }
}

@Composable
fun CarLibraryCard(
    car: CarImageEntry,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(car.url),
                contentDescription = car.name,
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onImageClick(car.url.toString()) } // Clic en imagen
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = car.name.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Año: ${car.year}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!car.series.isNullOrBlank()) {
                    Text(
                        text = "Serie: ${car.series}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}