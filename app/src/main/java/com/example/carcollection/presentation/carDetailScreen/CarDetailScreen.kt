import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.R
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.local.Tag
import androidx.core.graphics.toColorInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.carcollection.presentation.carDetailScreen.AutoSizeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    car: Car,
    allTags: List<Tag>,
    onBackClick: () -> Unit
) {
    var showImageDialog by remember { mutableStateOf(false) }

    val primaryTag = car.tags.firstOrNull()
    val primaryTagColor = allTags.find { it.name == primaryTag }?.color ?: "#CCCCCC"
    val parsedColor = try {
        Color(primaryTagColor.toColorInt())
    } catch (e: Exception) {
        Color.Gray
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
                .fillMaxHeight()
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.f2),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    /*------------- COLUMNA IZQUIERDA -------------*/
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        /** Logo arriba */
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit

                        )


                        Spacer(modifier = Modifier.weight(1f)) // Empuja el contenido siguiente al fondo

                        /** Foto del carro */
                        AsyncImage(
                            model = car.photoUrl,
                            contentDescription = "${car.brand} ${car.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(1.dp, Color.Transparent, CircleShape)
                                .clip(CircleShape)
                                .clickable { showImageDialog = true },
                            contentScale = ContentScale.Fit
                        )

                        /** Card de información */
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                // Primera columna con datos principales
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        car.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        car.brand,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        "Año ${car.year}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        car.color,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        "Tipo ${car.type}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        car.serie,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Segunda columna con tags
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(max = 100.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Tags:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    if (car.tags.isEmpty()) {
                                        Text(
                                            "Sin tags",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    } else {
                                        car.tags.forEach { tag ->
                                            Text(
                                                "- $tag",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                            }
                        }


                    }

                    /*------------- FRANJA DE TAG DERECHA -------------*/
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

        /*------------- DIALOGO DE IMAGEN -------------*/
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
                        model = car.photoUrl,
                        contentDescription = "Imagen del carro ampliada",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            )
        }
    }
}
