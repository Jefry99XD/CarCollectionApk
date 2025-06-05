import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.data.local.Car

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    car: Car,
    onBackClick: () -> Unit
) {
    var isImageFullScreen by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = car.photoUrl,
                contentDescription = "${car.brand} ${car.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isImageFullScreen) 400.dp else 200.dp)
                    .clickable { isImageFullScreen = !isImageFullScreen },
                contentScale = ContentScale.Crop
            )

            Text("Marca: ${car.brand}", style = MaterialTheme.typography.titleMedium)
            Text("Nombre: ${car.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Serie: ${car.serie}", style = MaterialTheme.typography.bodyMedium)
            Text("Color: ${car.color}", style = MaterialTheme.typography.bodySmall)
            Text("Año: ${car.year}", style = MaterialTheme.typography.bodySmall)
            Text("Tipo: ${car.type}", style = MaterialTheme.typography.bodySmall)
            Text("Tags: ${car.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

