package com.example.carcollection.presentation.consultas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun THScreen(
    thEntries: List<STHEntry>,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("Todos") }

    // Estado para imagen seleccionada y mostrar diálogo
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val availableYears = listOf("Todos") + thEntries.map { it.year.toString() }.distinct()

    val filteredEntries = thEntries.filter {
        (selectedYear == "Todos" || it.year.toString() == selectedYear) &&
                it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treasure Hunt") },
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
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuYearSelector(
                years = availableYears,
                selectedYear = selectedYear,
                onYearSelected = { selectedYear = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredEntries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = entry.regularPhotoUrl,
                                contentDescription = "${entry.name} image",
                                modifier = Modifier
                                    .height(100.dp)
                                    .weight(1f)
                                    .clickable {
                                        selectedImageUrl = entry.regularPhotoUrl
                                    }
                            )

                            Column(modifier = Modifier.weight(2f)) {
                                Text(entry.name, style = MaterialTheme.typography.titleSmall)
                                Text("Año: ${entry.year}", style = MaterialTheme.typography.bodySmall)
                                Text("Serie: ${entry.series}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Diálogo para mostrar la imagen ampliada
            if (selectedImageUrl != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { selectedImageUrl = null },
                    confirmButton = {
                        TextButton(onClick = { selectedImageUrl = null }) {
                            Text("Cerrar")
                        }
                    },
                    text = {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = "Imagen ampliada",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuYearSelector(
    years: List<String>,
    selectedYear: String,
    onYearSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Año: $selectedYear")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}
