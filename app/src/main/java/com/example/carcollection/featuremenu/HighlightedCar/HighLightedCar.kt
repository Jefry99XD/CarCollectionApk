package com.example.carcollection.featuremenu.HighlightedCar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.presentation.add_edit_car.CarLibraryEntry
import com.example.carcollection.featurecar.presentation.add_edit_car.CarVariation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.abs

// Data class para el carro del día
data class CarOfTheDay(
    val name: String,
    val year: String,
    val series: String,
    val url: String,
    val color: String = "",
    val description: String = ""
)

/**
 * Función para obtener el carro del día basado en la fecha actual.
 * Usa la fecha como semilla para generar un índice consistente.
 * Todos los usuarios verán el mismo carro el mismo día.
 */
fun getCarOfTheDay(context: Context): CarOfTheDay? {
    return try {
        // Cargar JSON desde assets
        val inputStream = context.assets.open("diecast_images.json")
        val json = inputStream.bufferedReader().use { it.readText() }


        // Parsear JSON
        val gson = Gson()
        val carLibraryEntries = try {
            // Intentar parsear como array
            val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
            val entries = gson.fromJson<List<CarLibraryEntry>>(json, typeArray)
            entries
        } catch (_: Exception) {
            // Si falla, intentar como objeto único
            try {
                val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                val singleEntry = gson.fromJson<CarLibraryEntry>(json, typeSingle)
                listOf(singleEntry)
            } catch (e2: Exception) {
                throw e2
            }
        }


        // Aplanar todas las variaciones
        val allVariations = mutableListOf<Pair<CarLibraryEntry, CarVariation>>()
        carLibraryEntries.forEach { entry ->
            entry.variations?.forEach { variation ->
                val hasValidUrl = variation.url != null &&
                                 variation.url.isNotBlank() &&
                                 !variation.url.contains("Image_Not_Available", ignoreCase = true)

                if (hasValidUrl) {
                    allVariations.add(Pair(entry, variation))
                }
            }
        }

        if (allVariations.isEmpty()) {
            return null
        }

        // Obtener fecha actual en formato YYYY-MM-DD
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(calendar.time)

        // Usar la fecha como semilla para generar índice consistente
        // Hashcode de la fecha será el mismo para todos los usuarios
        val seed = dateString.hashCode()
        val index = abs(seed % allVariations.size)

        // Seleccionar el carro del día
        val (carEntry, variation) = allVariations[index]

        val carOfTheDay = CarOfTheDay(
            name = carEntry.name ?: "Modelo desconocido",
            year = variation.year ?: "N/A",
            series = variation.series ?: "N/A",
            url = variation.url ?: "",
            color = variation.color ?: "",
            description = carEntry.description?.takeIf { it.isNotBlank() }?.trim() ?: "Un clásico por descubrir."
        )

        carOfTheDay

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CarOfTheDayScreen() {
    val context = LocalContext.current
    var carOfTheDay by remember { mutableStateOf<CarOfTheDay?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showImageDialog by remember { mutableStateOf(false) }

    // Cargar el carro del día
    LaunchedEffect(Unit) {
        carOfTheDay = getCarOfTheDay(context)
        isLoading = false
    }

    if (isLoading) {
        // Indicador de carga
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (carOfTheDay == null) {
        // Error: No se pudo cargar el carro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se pudo cargar el carro del día",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    val car = carOfTheDay!!

    // UI del carro del día
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Título general ───
        Text(
            text = "Carro del Día",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // ─── Imagen principal con badge y overlay ───
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {

                // Imagen del carro
                AsyncImage(
                    model = car.url,
                    contentDescription = car.name,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFF0F0F0))
                        .clickable { showImageDialog = true }
                )

                // Badge “HOY”
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HOY",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Overlay inferior con información
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            car.name,
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                            fontWeight = FontWeight.Bold
                        )
                        val detailsText = buildString {
                            append(car.year)
                            if (car.color.isNotBlank()) {
                                append(" • ${car.color}")
                            }
                            if (car.series.isNotBlank()) {
                                append(" • ${car.series}")
                            }
                        }
                        Text(
                            detailsText,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            }
        }

        // ─── Descripción en tarjeta ───
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = car.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // ─── Diálogo de imagen ampliada ───
    if (showImageDialog) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showImageDialog = false },
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Título
                        Text(
                            text = car.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        // Imagen ampliada
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            AsyncImage(
                                model = car.url,
                                contentDescription = car.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            )
                        }

                        // Información del carro
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Año: ${car.year}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (car.color.isNotBlank()) {
                                    Text(
                                        text = "Color: ${car.color}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (car.series.isNotBlank()) {
                                    Text(
                                        text = "Serie: ${car.series}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Instrucción para cerrar
                        Text(
                            text = "Toca en cualquier lugar para cerrar",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}