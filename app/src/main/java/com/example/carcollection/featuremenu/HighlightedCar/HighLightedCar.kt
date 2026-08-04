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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.CarLibraryEntry
import com.example.carcollection.featurecar.presentation.add_edit_car.CarVariation
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

// ─────────────────────────────────────────────────────────────────────────────
// Singleton cache — persists across recompositions and navigation.
// The JSON file (~large) is parsed at most ONCE per calendar day per process.
// ─────────────────────────────────────────────────────────────────────────────
object CarOfTheDayCache {
    @Volatile private var cachedCar: CarOfTheDay? = null
    @Volatile private var cachedDate: String? = null

    fun getOrLoad(context: Context): CarOfTheDay? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        // Return cached value when date matches (same car all day, no re-read needed)
        if (cachedDate == today && cachedCar != null) return cachedCar
        val result = getCarOfTheDay(context)
        cachedCar = result
        cachedDate = today
        return result
    }

    /** Call when the assets file changes (dev/testing only). */
    fun invalidate() {
        cachedCar = null
        cachedDate = null
    }
}

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
            val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
            gson.fromJson<List<CarLibraryEntry>>(json, typeArray)
        } catch (_: Exception) {
            try {
                val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                listOf(gson.fromJson<CarLibraryEntry>(json, typeSingle))
            } catch (e2: Exception) {
                throw e2
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // NUEVO ALGORITMO:
        //  1. Deduplicar por nombre de modelo → una entrada por carro único
        //  2. Usar el hash de la fecha para elegir el MODELO del día
        //  3. Usar un hash secundario para elegir la VARIACIÓN de ese modelo
        //
        // Esto garantiza que cada día salga un modelo diferente, sin importar
        // cuántas variaciones tenga (evita ver el mismo carro toda una semana).
        // ─────────────────────────────────────────────────────────────────────

        // Paso 1: filtrar entradas que tengan al menos una variación con URL válida
        val validEntries = carLibraryEntries
            .filter { entry ->
                entry.name != null &&
                entry.variations?.any { v ->
                    v.url != null &&
                    v.url.isNotBlank() &&
                    !v.url.contains("Image_Not_Available", ignoreCase = true)
                } == true
            }
            .distinctBy { it.name?.trim()?.lowercase() } // un registro por modelo único

        if (validEntries.isEmpty()) return null

        // Paso 2: fecha como semilla → índice del modelo
        val calendar  = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(calendar.time)

        val dateSeed   = dateString.hashCode()
        val modelIndex = abs(dateSeed % validEntries.size)
        val selectedEntry = validEntries[modelIndex]

        // Paso 3: elegir variación con hash secundario (distinto al de modelo)
        val validVariations = selectedEntry.variations?.filter { v ->
            v.url != null &&
            v.url.isNotBlank() &&
            !v.url.contains("Image_Not_Available", ignoreCase = true)
        } ?: emptyList()

        if (validVariations.isEmpty()) return null

        val variationSeed  = dateSeed * 31 + modelIndex   // hash diferente al del modelo
        val variationIndex = abs(variationSeed.toLong() % validVariations.size).toInt()
        val selectedVariation = validVariations[variationIndex]

        CarOfTheDay(
            name        = selectedEntry.name ?: "Modelo desconocido",
            year        = selectedVariation.year   ?: "N/A",
            series      = selectedVariation.series ?: "N/A",
            url         = selectedVariation.url    ?: "",
            color       = selectedVariation.color  ?: "",
            description = selectedEntry.description
                ?.takeIf { it.isNotBlank() }?.trim()
                ?: "Un clásico por descubrir."
        )

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CarOfTheDayScreen(
    userCars: List<Car> = emptyList()
) {
    val context = LocalContext.current
    var carOfTheDay by remember { mutableStateOf<CarOfTheDay?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showImageDialog by remember { mutableStateOf(false) }
    var isClaimingAchievement by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf("") }
    var showClaimMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val achievementMethods = remember { AchievementMethods() }

    // Verificar si el usuario ya desbloqueó el logro
    var achievementUnlocked by remember { mutableStateOf(false) }
    
    // Contador de reclamaciones
    var claimCount by remember { mutableStateOf(0) }
    
    // Verificar si el usuario tiene un carro que contiene el nombre del carro del día
    var canClaimAchievement by remember { mutableStateOf(false) }

    // Use the process-level cache — avoids re-reading the large JSON on every composition
    LaunchedEffect(Unit) {
        carOfTheDay = CarOfTheDayCache.getOrLoad(context)
        isLoading = false
    }

    // Actualizar estado del logro cuando cambian los carros o el carro del día
    LaunchedEffect(userCars, carOfTheDay) {
        if (carOfTheDay != null && userCars.isNotEmpty()) {
            val canClaim = userCars.any { car ->
                car.name?.contains(carOfTheDay!!.name, ignoreCase = true) == true
            }
            canClaimAchievement = canClaim
        }
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Carro del Día",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )

            // Botón "Reclamar Logro" en la esquina superior derecha
            if (canClaimAchievement && !achievementUnlocked) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isClaimingAchievement = true
                                
                                // Verificar nuevamente que tenga el carro
                                val hasCarOfTheDay = userCars.any { car ->
                                    car.name?.contains(carOfTheDay!!.name, ignoreCase = true) == true
                                }

                                if (!hasCarOfTheDay) {
                                    // El usuario NO tiene el carro - bloquear después del tap
                                    claimMessage = "No tienes este carro en tu colección"
                                    canClaimAchievement = false
                                    showClaimMessage = true
                                    isClaimingAchievement = false
                                    return@launch
                                }

                                // Llamar al método para actualizar el logro
                                val result = achievementMethods.manualClaimCarOfTheDayAchievement()
                                
                                if (result.success) {
                                    claimMessage = result.message
                                    achievementUnlocked = true
                                    claimCount += 1  // Incrementar contador
                                } else {
                                    // Mostrar el motivo del fallo
                                    claimMessage = result.message
                                    
                                    // Si ya reclamó hoy, bloquear el botón
                                    if (result.reason == "ALREADY_CLAIMED_TODAY") {
                                        canClaimAchievement = false
                                    }
                                }
                                showClaimMessage = true
                                isClaimingAchievement = false
                            } catch (e: Exception) {
                                claimMessage = "Error al reclamar el logro: ${e.message}"
                                showClaimMessage = true
                                isClaimingAchievement = false
                            }
                        }
                    },
                    enabled = !isClaimingAchievement,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (isClaimingAchievement) "Reclamando..." else "Reclamar Logro",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                }
            } else if (!canClaimAchievement && !achievementUnlocked) {
                // No puede reclamar porque no tiene el carro
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "⚠ Sin carro",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            } else if (achievementUnlocked) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "✓ Desbloqueado",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    if (claimCount > 0) {
                        Text(
                            "Reclamado $claimCount ${if (claimCount == 1) "vez" else "veces"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

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

        // ─── Mensaje de reclamación de logro ───
        if (showClaimMessage) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (achievementUnlocked)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = claimMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(16.dp),
                    color = if (achievementUnlocked)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
            
            LaunchedEffect(showClaimMessage) {
                delay(3000)
                showClaimMessage = false
            }
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