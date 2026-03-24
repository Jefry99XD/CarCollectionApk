package com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.getBackgroundUrlById
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.launch

// Función helper para calcular si un color es claro u oscuro
private fun Color.isLightColor(): Boolean {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255

    // Calcular luminancia usando la fórmula estándar
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return luminance > 0.5
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarDetailBlisterView(
    car: Car,
    allTags: List<Tag>,
    onImageClick: () -> Unit
) {
    val primaryTag = car.tags.firstOrNull()
    val primaryTagColor = allTags.find { it.name == primaryTag }?.color ?: "#CCCCCC"
    val parsedColor = try {
        Color(primaryTagColor.toColorInt())
    } catch (_: Exception) {
        Color.Gray
    }

    // Estado para cargar la URL del fondo
    val context = LocalContext.current
    val backgroundUrl = remember { mutableStateOf("") }

    // Cargar URL del fondo en background
    LaunchedEffect(car.backgroundName) {
        backgroundUrl.value = getBackgroundUrlById(context, car.backgroundName.orEmpty())
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo desde URL del JSON
            if (backgroundUrl.value.isNotEmpty()) {
                AsyncImage(
                    model = backgroundUrl.value,
                    contentDescription = "Fondo del carro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                // Fallback a color si no hay URL
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Foto del carro
                    AsyncImage(
                        model = car.photoUrl ?: "",
                        contentDescription = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(200.dp)
                            .clip(CircleShape)
                            .clickable { onImageClick() },
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Información principal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    car.name.orEmpty(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${car.brand.orEmpty()} · ${car.year.orEmpty()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    car.color.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    car.type.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    car.serie.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }

                            // Tags
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Tags:",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (car.tags.isEmpty()) {
                                        Text(
                                            "Sin tags",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    } else {
                                        car.tags.forEach { tagName ->
                                            val tagColor = allTags.find { it.name == tagName }?.color
                                                ?: "#888888"
                                            val chipColor = try {
                                                Color(tagColor.toColorInt())
                                            } catch (_: Exception) {
                                                Color.Gray
                                            }

                                            // Calcular color de texto con buen contraste
                                            val textColor = if (chipColor.isLightColor()) {
                                                Color.Black
                                            } else {
                                                Color.White
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = chipColor,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = tagName,
                                                    color = textColor,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Franja de tag derecha
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .background(parsedColor),
                    contentAlignment = Alignment.Center
                ) {
                    AutoSizeText(
                        text = primaryTag?.map { "$it\n" }?.joinToString("") ?: "",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        backgroundColor = parsedColor
                    )
                }
            }
        }
    }
}

