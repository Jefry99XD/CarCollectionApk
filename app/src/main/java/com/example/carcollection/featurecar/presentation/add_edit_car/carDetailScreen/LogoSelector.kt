package com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LogoEntry(
    val name: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoSelectorDialog(
    context: Context = LocalContext.current,
    onDismiss: () -> Unit,
    onLogoSelected: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var allLogos by remember { mutableStateOf<List<LogoEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        allLogos = try {
            val inputStream = context.assets.open("achievement_logos.json")
            val json = inputStream.bufferedReader().use { it.readText() }

            println("🔍 LogoSelector: Loading JSON, length = ${json.length}")

            val gson = Gson()
            val logoEntries = try {
                val typeArray = object : TypeToken<List<LogoEntry>>() {}.type
                gson.fromJson<List<LogoEntry>>(json, typeArray)
            } catch (_: Exception) {
                println("⚠️ LogoSelector: Array parsing failed")
                emptyList()
            }

            println("🏆 LogoSelector: Loaded ${logoEntries.size} logo entries")
            logoEntries.sortedBy { it.name }
        } catch (e: Exception) {
            println("❌ LogoSelector: Error loading logos - ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    val filteredLogos = allLogos.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecciona un logo") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar logo") },
                    placeholder = { Text("Buscar por nombre...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredLogos.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(filteredLogos.size) { index ->
                            val logo = filteredLogos[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLogoSelected(logo.url)
                                        onDismiss()
                                    },
                                elevation = CardDefaults.elevatedCardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = logo.url,
                                        contentDescription = logo.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = logo.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (allLogos.isEmpty()) "Cargando logos..." else "No se encontraron logos.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

