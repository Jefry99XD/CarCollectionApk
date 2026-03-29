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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carcollection.R
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.domain.Tag
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding

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
    // 🔹 Detectar orientación del dispositivo
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val primaryTag = car.tags.firstOrNull()
    val primaryTagColor = allTags.find { it.name == primaryTag }?.color ?: "#CC0000"
    val parsedColor = try {
        Color(primaryTagColor.toColorInt())
    } catch (_: Exception) {
        Color(0xFFCC0000)
    }

    // ✅ USAR backgroundUrl DIRECTAMENTE (viene de Firestore con URL completo)
    val backgroundUrl = car.backgroundUrl.orEmpty()

    // 🔹 VERTICAL (Teléfono vertical o tablet vertical)
    if (!isLandscape) {
        BlisterViewVertical(car, allTags, onImageClick, backgroundUrl, parsedColor, primaryTag)
    }
    // 🔹 HORIZONTAL (Teléfono horizontal)
    else {
        BlisterViewHorizontal(car, allTags, onImageClick, backgroundUrl, parsedColor, primaryTag)
    }
}

// 🔹 VISTA VERTICAL - Blister estilo Hot Wheels con mejoras 3D
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlisterViewVertical(
    car: Car,
    allTags: List<Tag>,
    onImageClick: () -> Unit,
    backgroundUrl: String,
    primaryColor: Color,
    primaryTag: String?
) {
    val context = LocalContext.current

    // Marco principal con sombra elevada 3D
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(0.25f),
                spotColor = Color.Black.copy(0.4f)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFFD4AF37), // Oro plateado tipo Hot Wheels
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // Fondo de imagen si existe
        if (backgroundUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = "Fondo del carro",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 🎨 ENCABEZADO - Logo y nombre de serie
            BlisterHeader(car, primaryColor)

            Divider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp, color = Color(0xFFD4AF37))

            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // LADO PRINCIPAL - Carro y detalles
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Foto del carro con efecto de vidriera
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(220.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFE8E8E8),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(car.photoUrl ?: "")
                                .crossfade(300)
                                .build(),
                            contentDescription = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick() },
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información del carro
                    BlisterCarInfo(car)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags
                    TagsFlowRow(car, allTags)
                }

                // FRANJA LATERAL - Color primario con efecto diagonal
                BlisterSidePanel(primaryColor, primaryTag)
            }
        }
    }
}

// 🔹 VISTA HORIZONTAL - Blister compacto
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlisterViewHorizontal(
    car: Car,
    allTags: List<Tag>,
    onImageClick: () -> Unit,
    backgroundUrl: String,
    primaryColor: Color,
    primaryTag: String?
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(0.25f),
                spotColor = Color.Black.copy(0.4f)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFFD4AF37),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // Fondo de imagen si existe
        if (backgroundUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = "Fondo del carro",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .width(180.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFE8E8E8),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(car.photoUrl ?: "")
                        .crossfade(300)
                        .build(),
                    contentDescription = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Fit
                )
            }

            // Información compacta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Nombre con fondo semi-transparente
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        car.name.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Marca/Año con fondo semi-transparente
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        "${car.brand.orEmpty()} · ${car.year.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Color", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(car.color.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                    Column {
                        Text("Tipo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(car.type.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TagsFlowRow(car, allTags)
            }

            // Franja lateral compacta
            BlisterSidePanel(primaryColor, primaryTag, width = 28.dp)
        }
    }
}

// ...existing code...

// 🔹 ENCABEZADO DEL BLISTER - Logo y nombre de serie
@Composable
private fun BlisterHeader(car: Car, primaryColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo de la app
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .background(Color.White, shape = CircleShape)
                .border(1.5.dp, primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(R.drawable.logo)
                    .crossfade(300)
                    .build(),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxSize(0.8f),
                contentScale = ContentScale.Fit
            )
        }

        // Información de serie
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                car.serie.orEmpty().uppercase().take(20),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                maxLines = 1
            )
            Text(
                "${car.brand.orEmpty()} · ${car.year.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Número de colección (simulado)
        Box(
            modifier = Modifier
                .background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#${car.hashCode() % 1000}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

// 🔹 INFORMACIÓN DEL CARRO
@Composable
private fun BlisterCarInfo(car: Car) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Nombre del carro con fondo semi-transparente
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                car.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Marca con fondo semi-transparente
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                car.brand.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de propiedades
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PropertyBadge("Año", car.year.orEmpty())
                PropertyBadge("Color", car.color.orEmpty())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PropertyBadge("Tipo", car.type.orEmpty())
                PropertyBadge("Serie", car.serie.orEmpty().take(12))
            }
        }
    }
}

// 🔹 BADGE DE PROPIEDAD
@Composable
private fun PropertyBadge(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.45f)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
        Text(
            value.take(15),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

// 🔹 FRANJA LATERAL DEL BLISTER
@Composable
private fun BlisterSidePanel(
    primaryColor: Color,
    primaryTag: String?,
    width: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Texto vertical del tag
        Text(
            text = (primaryTag ?: "TAG")
                .uppercase()
                .take(3),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// 🔹 DIVIDER PERSONALIZADO
@Composable
private fun Divider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = Color.Gray) {
    Box(
        modifier = modifier
            .height(thickness)
            .background(color)
    )
}

// 🔹 Componente: Información del carro horizontal
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlisterInfoBoxHorizontal(car: Car, allTags: List<Tag>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            car.name.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            "${car.brand.orEmpty()} · ${car.year.orEmpty()}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Color", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(car.color.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tipo", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(car.type.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Serie", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(car.serie.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TagsFlowRow(car, allTags)
    }
}

// 🔹 Componente: Columna de tags (vertical)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsColumn(car: Car, allTags: List<Tag>) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Tags:",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        if (car.tags.isEmpty()) {
            Text(
                "Sin tags",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        } else {
            car.tags.forEach { tagName ->
                TagChip(tagName, allTags)
            }
        }
    }
}

// 🔹 Componente: FlowRow de tags (horizontal)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsFlowRow(car: Car, allTags: List<Tag>) {
    if (car.tags.isNotEmpty()) {
        Text(
            "Tags:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            car.tags.forEach { tagName ->
                TagChip(tagName, allTags)
            }
        }
    }
}

// 🔹 Componente: Chip de tag reutilizable
@Composable
private fun TagChip(tagName: String, allTags: List<Tag>) {
    val tagColor = allTags.find { it.name == tagName }?.color ?: "#888888"
    val chipColor = try {
        Color(tagColor.toColorInt())
    } catch (_: Exception) {
        Color.Gray
    }

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

// 🔹 Componente: Franja lateral de tag (antigua)
@Composable
private fun TagSideBar(primaryTag: String?, parsedColor: Color, width: Dp = 48.dp) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        parsedColor,
                        parsedColor.copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = parsedColor.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (primaryTag ?: "TAG")
                .uppercase()
                .take(3),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
