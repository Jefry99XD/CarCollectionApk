import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.runBlocking
import com.example.carcollection.utils.importCarsFromUri
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.carcollection.data.repository.TagRepository
import com.example.carcollection.utils.exportCarsToUri
import com.example.carcollection.utils.exportTagsToUri
import com.example.carcollection.utils.importTagsFromUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    repository: CarRepository,
    onBackClick: () -> Unit,
    tagRepository: TagRepository,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val carsState = remember { mutableStateOf<List<Car>>(emptyList()) }

    // Car Export/Import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri: Uri? ->
            uri?.let {
                exportCarsToUri(context, carsState.value, it)
            }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                importCarsFromUri(context, repository, it)
            }
        }
    )

    // Tag Export/Import
    val exportTagsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    exportTagsToUri(context, tagRepository, it)
                    Toast.makeText(context, "Tags exportados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    val importTagsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    importTagsFromUri(context, tagRepository, it)
                    Toast.makeText(context, "Tags importados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos") },
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
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ===== COLECCIÓN =====
            Text("Colección", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    coroutineScope.launch {
                        carsState.value = repository.getAllCarsList()
                        exportLauncher.launch("car_collection_export.csv")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar colección")
            }

            Button(
                onClick = {
                    importLauncher.launch(arrayOf("text/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Importar colección")
            }

            // ===== TAGS =====
            Spacer(Modifier.height(24.dp))
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    exportTagsLauncher.launch("tags_backup.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Tags")
            }

            Button(
                onClick = {
                    importTagsLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Importar Tags")
            }

            // ===== ACCIONES PELIGROSAS =====
            Spacer(Modifier.height(24.dp))
            Text(
                "Acciones peligrosas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Borrar toda la colección")
            }
        }

        // Confirmación de eliminación
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            repository.deleteAll()
                            Toast.makeText(context, "Catálogo borrado con éxito", Toast.LENGTH_SHORT).show()
                            showDialog.value = false
                        }
                    }) {
                        Text("Sí, borrar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("¿Eliminar todo?") },
                text = { Text("¿Desea realmente eliminar todo el catálogo guardado en la app? Esta acción no se puede deshacer.") }
            )
        }
    }
}