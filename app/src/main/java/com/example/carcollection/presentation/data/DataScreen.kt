import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.utils.exportCarsToCSV
import com.example.carcollection.utils.importCarsFromCSV
import kotlinx.coroutines.runBlocking
import com.example.carcollection.utils.importCarsFromUri
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.carcollection.utils.exportCarsToUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    repository: CarRepository,
    onBackClick: () -> Unit,
    onNavigateToTags: () -> Unit
) {
    val context = LocalContext.current

    val showDialog = remember { mutableStateOf(false) }

    val carsState = remember { mutableStateOf<List<Car>>(emptyList()) }

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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(onClick = {
                runBlocking {
                    val cars = repository.getAllCarsList()
                    carsState.value = cars
                    exportLauncher.launch("car_collection_export.csv") // nombre sugerido
                }
            }) {
                Text("Exportar colección")
            }


            Button(onClick = {
                importLauncher.launch(arrayOf("text/*")) // abre selector de CSVs
            }) {
                Text("Importar desde archivo")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    showDialog.value = true
                }
            ) {
                Text("Borrar toda la colección")
            }
        }


        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        runBlocking {
                            repository.deleteAll()
                            Toast.makeText(context, "Catálogo borrado con éxito", Toast.LENGTH_SHORT).show()
                            showDialog.value = false
                        }
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog.value = false
                    }) {
                        Text("No")
                    }
                },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Desea realmente eliminar todo el catálogo guardado en la app?") }
            )
        }


    }
}