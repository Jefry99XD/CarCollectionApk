package com.example.carcollection.presentation.consultas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalConfiguration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun STHScreen(
    sthEntries: List<STHEntry>,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf<String?>(null) }

    // Estado para imagen seleccionada y mostrar diálogo
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    // Obtener años únicos disponibles
    val years = remember(sthEntries) {
        sthEntries.map { it.year }.distinct().sorted()
    }

    // Filtrar según búsqueda y año
    val filteredEntries = sthEntries.filter { entry ->
        val matchesSearch = entry.name.contains(searchQuery, ignoreCase = true)
                || entry.series.contains(searchQuery, ignoreCase = true)

        val matchesYear = selectedYear == null || entry.year == selectedYear
        matchesSearch && matchesYear
    }

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Treasure Hunt") },
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
                .padding(padding)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre o serie") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por año
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filtrar por año:")
                Spacer(Modifier.width(8.dp))

                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedYear?.toString() ?: "Todos")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                selectedYear = null
                                expanded = false
                            }
                        )
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredEntries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // Encabezado
                            Text(entry.name, style = MaterialTheme.typography.titleMedium)
                            Text("Serie: ${entry.series}", style = MaterialTheme.typography.bodySmall)
                            Text("Año: ${entry.year}", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(12.dp))

                            // --- Comparación de imágenes ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(Modifier.weight(1f)) {
                                    ImageCard(
                                        title = "Regular",
                                        imageUrl = entry.regularPhotoUrl,
                                        onClick = { selectedImageUrl = entry.regularPhotoUrl }
                                    )
                                }
                                Box(Modifier.weight(1f)) {
                                    ImageCard(
                                        title = "STH",
                                        imageUrl = entry.sthPhotoUrl,
                                        onClick = { selectedImageUrl = entry.sthPhotoUrl }
                                    )
                                }
                            }

                        }
                    }
                }
            }

            // Diálogo para mostrar la imagen ampliada
            if (selectedImageUrl != null) {
                ImageDialog(
                    imageUrl = selectedImageUrl!!,
                    onDismiss = { selectedImageUrl = null }
                )
            }
        }
    }
}

@Composable
private fun ImageCard(title: String, imageUrl: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .height(160.dp) // misma altura para ambas imágenes
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$title image",
                contentScale = ContentScale.Fit, // evita recorte
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun ImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .width(screenWidth * 0.9f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen ampliada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}



