package com.example.carcollection.featuremenu.HighlightedCar

import android.content.Context
import androidx.compose.foundation.background
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
        println("🚗 CarOfTheDay: Loading JSON...")

        // Cargar JSON desde assets
        val inputStream = context.assets.open("diecast_images.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        println("🚗 CarOfTheDay: JSON loaded, length = ${json.length}")
        println("🚗 CarOfTheDay: JSON starts with: ${json.take(100)}")

        // Parsear JSON
        val gson = Gson()
        val carLibraryEntries = try {
            // Intentar parsear como array
            val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
            val entries = gson.fromJson<List<CarLibraryEntry>>(json, typeArray)
            println("✅ CarOfTheDay: Parsed as array successfully")
            entries
        } catch (e: Exception) {
            println("⚠️ CarOfTheDay: Array parsing failed: ${e.message}")
            println("🔄 CarOfTheDay: Trying single object...")
            // Si falla, intentar como objeto único
            try {
                val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                val singleEntry = gson.fromJson<CarLibraryEntry>(json, typeSingle)
                println("✅ CarOfTheDay: Parsed as single object")
                listOf(singleEntry)
            } catch (e2: Exception) {
                println("❌ CarOfTheDay: Single object parsing also failed: ${e2.message}")
                throw e2
            }
        }

        println("🚗 CarOfTheDay: Loaded ${carLibraryEntries.size} car entries")

        // Aplanar todas las variaciones
        val allVariations = mutableListOf<Pair<CarLibraryEntry, CarVariation>>()
        carLibraryEntries.forEachIndexed { index, entry ->
            println("🚗 CarOfTheDay: Entry #$index - name=${entry.name}, variations=${entry.variations?.size ?: 0}")
            entry.variations?.forEach { variation ->
                // Filtrar variaciones sin URL o con imagen no disponible
                val hasValidUrl = variation.url != null &&
                                 variation.url.isNotBlank() &&
                                 !variation.url.contains("Image_Not_Available", ignoreCase = true)

                if (hasValidUrl) {
                    allVariations.add(Pair(entry, variation))
                }
            }
        }

        println("🚗 CarOfTheDay: Total valid variations = ${allVariations.size}")

        if (allVariations.isEmpty()) {
            println("❌ CarOfTheDay: No variations found")
            return null
        }

        // Obtener fecha actual en formato YYYY-MM-DD
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(calendar.time)
        println("📅 CarOfTheDay: Today is $dateString")

        // Usar la fecha como semilla para generar índice consistente
        // Hashcode de la fecha será el mismo para todos los usuarios
        val seed = dateString.hashCode()
        val index = Math.abs(seed % allVariations.size)

        println("🎲 CarOfTheDay: Seed = $seed, Index = $index")

        // Seleccionar el carro del día
        val (carEntry, variation) = allVariations[index]

        println("🔍 CarOfTheDay: Selected Entry Details:")
        println("   - Model Name: ${carEntry.name}")
        println("   - Variation Year: ${variation.year}")
        println("   - Variation Color: ${variation.color}")
        println("   - Variation Series: ${variation.series}")
        println("   - Variation URL: ${variation.url}")

        val carOfTheDay = CarOfTheDay(
            name = carEntry.name ?: "Modelo desconocido",
            year = variation.year ?: "N/A",
            series = variation.series ?: "N/A",
            url = variation.url ?: "",
            color = variation.color ?: "",
            description = carEntry.description ?: "Sin descripción disponible."
        )

        println("✅ CarOfTheDay: Selected ${carOfTheDay.name} (${carOfTheDay.year}) - ${carOfTheDay.color}")
        println("   URL: ${carOfTheDay.url}")
        carOfTheDay

    } catch (e: Exception) {
        println("❌ CarOfTheDay: Error - ${e.message}")
        e.printStackTrace()
        null
    }
}

@Composable
fun CarOfTheDayScreen() {
    val context = LocalContext.current
    var carOfTheDay by remember { mutableStateOf<CarOfTheDay?>(null) }
    var isLoading by remember { mutableStateOf(true) }

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
}