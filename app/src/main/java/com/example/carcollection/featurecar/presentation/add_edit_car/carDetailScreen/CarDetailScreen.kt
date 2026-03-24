
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen.CarDetailBlisterView
import com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen.CarDetailModernView
import com.example.carcollection.featuretags.domain.Tag


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CarDetailScreen(
    car: Car?,
    allTags: List<Tag>,
    onBackClick: () -> Unit
) {
    var showImageDialog by remember { mutableStateOf(false) }
    var isModernView by remember { mutableStateOf(false) }

    // Mostrar loading si el carro aún no se cargó
    if (car == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando...", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Carro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para cambiar vista
                    IconButton(onClick = { isModernView = !isModernView }) {
                        Icon(
                            imageVector = if (isModernView) Icons.Default.GridView else Icons.Default.ViewDay,
                            contentDescription = if (isModernView) "Vista blister" else "Vista moderna",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isModernView) {
                // Vista moderna
                CarDetailModernView(
                    car = car,
                    allTags = allTags,
                    onImageClick = { showImageDialog = true }
                )
            } else {
                // Vista blister original
                CarDetailBlisterView(
                    car = car,
                    allTags = allTags,
                    onImageClick = { showImageDialog = true }
                )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentScale = ContentScale.Inside
                    )
                }
            )
        }
    }
}

