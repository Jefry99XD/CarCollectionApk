
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.carcollection.R
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen.AutoSizeText
import com.example.carcollection.featuretags.domain.Tag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CarDetailScreen(
    car: Car?,
    allTags: List<Tag>,
    onBackClick: () -> Unit
) {
    var showImageDialog by remember { mutableStateOf(false) }

    // Mostrar loading si el carro aún no se cargó
    if (car == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando...", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val primaryTag = car.tags.firstOrNull()
    val primaryTagColor = allTags.find { it.name == primaryTag }?.color ?: "#CCCCCC"
    val parsedColor = try {
        Color(primaryTagColor.toColorInt())
    } catch (_: Exception) {
        Color.Gray
    }

    val resId = when (car.backgroundName) {
        "fondo1" -> R.drawable.fondo
        "fondo2" -> R.drawable.fondo2
        "fondo3" -> R.drawable.fondo3
        "fondo4" -> R.drawable.fondo4
        "fondo5" -> R.drawable.fondo5
        "fondo6" -> R.drawable.fondo6
        "fondo7" -> R.drawable.fondo7
        "fondo8" -> R.drawable.fondo8
        "fondo10" -> R.drawable.fondo10
        "fondo15" -> R.drawable.fondo15
        "fondo20" -> R.drawable.fondo20
        "fondo23" -> R.drawable.fondo23
        "fondo24" -> R.drawable.fondo24
        "fondo26" -> R.drawable.fondo26
        else -> R.drawable.fondo
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Carro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Card(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Foto del carro
                        AsyncImage(
                            model = car.photoUrl ?: "",
                            contentDescription = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(CircleShape)
                                .clickable { showImageDialog = true },
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Información principal
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                                        color = Color.White,
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

        // Dialogo de imagen
        if (showImageDialog) {
            AlertDialog(
                onDismissRequest = { showImageDialog = false },
                confirmButton = {
                    TextButton(onClick = { showImageDialog = false }) {
                        Text("Cerrar")
                    }
                },
                text = {
                    AsyncImage(
                        model = car.photoUrl ?: "",
                        contentDescription = "Imagen del carro ampliada",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            )
        }
    }
}

