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
import com.example.carcollection.presentation.carDetailScreen.AutoSizeText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row(modifier = Modifier.fillMaxSize()) {

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

                        Spacer(modifier = Modifier.height(8.dp)) // Empuja el contenido siguiente al fondo

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
                        Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ){
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                car.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "${car.brand} · ${car.year}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                            Text(
                                                car.color,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                            Text(
                                                car.type,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                            Text(
                                                car.serie,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }

                                        // Segunda columna con tags
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


                                            FlowRow( // Requiere accompanist-flowlayout
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
                                                        val tagColor = allTags.find { it.name == tagName }?.color ?: "#888888"
                                                        val chipColor = try {
                                                            Color(tagColor.toColorInt())
                                                        } catch (_: Exception) {
                                                            Color.Gray
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .background(color = chipColor, shape = RoundedCornerShape(50))
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
