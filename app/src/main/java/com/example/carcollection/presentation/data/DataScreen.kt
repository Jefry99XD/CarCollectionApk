package com.example.carcollection.presentation.data
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.utils.exportCarsToUri
import com.example.carcollection.utils.importCarsFromUri
import com.example.carcollection.utils.exportTagsToUri
import com.example.carcollection.utils.importTagsFromUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val carsState = remember { mutableStateOf<List<Car>>(emptyList()) }

    // Loading states
    val isImportingCars = remember { mutableStateOf(false) }
    val isImportingTags = remember { mutableStateOf(false) }
    val isExportingCars = remember { mutableStateOf(false) }
    val isExportingTags = remember { mutableStateOf(false) }

    // Import progress states
    val importProgress = remember { mutableStateOf(0) }
    val importTotal = remember { mutableStateOf(0) }
    val showImportDialog = remember { mutableStateOf(false) }

    // Car Export/Import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri: Uri? ->
            uri?.let {
                isExportingCars.value = true
                coroutineScope.launch {
                    exportCarsToUri(context, carsState.value, it)
                    isExportingCars.value = false
                }
            }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                isImportingCars.value = true
                showImportDialog.value = true
                importProgress.value = 0
                importTotal.value = 0

                coroutineScope.launch {
                    importCarsFromUri(
                        context = context,
                        uri = it,
                        onProgressUpdate = { current, total ->
                            importProgress.value = current
                            importTotal.value = total
                        },
                        onComplete = { _, _ ->
                            isImportingCars.value = false
                            showImportDialog.value = false
                        }
                    )
                }
            }
        }
    )

    // Tag Export/Import
    val exportTagsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                isExportingTags.value = true
                coroutineScope.launch {
                    exportTagsToUri(context, it)
                    isExportingTags.value = false
                    Toast.makeText(context, "Tags exportados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    val importTagsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                isImportingTags.value = true
                coroutineScope.launch {
                    val (added, updated) = importTagsFromUri(context, it)
                    isImportingTags.value = false
                    Toast.makeText(context, "Tags importados: $added añadidos, $updated actualizados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Dialog de progreso de importación
    if (showImportDialog.value) {
        AlertDialog(
            onDismissRequest = { }, // No permitir cerrar durante la importación
            title = { Text("Importando colección") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Procesando: ${importProgress.value} de ${importTotal.value} coches",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (importTotal.value > 0) {
                                importProgress.value.toFloat() / importTotal.value.toFloat()
                            } else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { }
        )
    }

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
                        val carMethods = CarMethods()
                        val result = carMethods.getUserCars()
                        if (result.isSuccess) {
                            carsState.value = result.getOrDefault(emptyList())
                            exportLauncher.launch("car_collection_export.csv")
                        } else {
                            Toast.makeText(context, "Error al obtener coches: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isExportingCars.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExportingCars.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Exportando...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar colección")
                }
            }

            Button(
                onClick = {
                    importLauncher.launch(arrayOf("text/*"))
                },
                enabled = !isImportingCars.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImportingCars.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Importando...")
                } else {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar colección")
                }
            }

            // ===== TAGS =====
            Spacer(Modifier.width(24.dp))
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    exportTagsLauncher.launch("tags_backup.json")
                },
                enabled = !isExportingTags.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExportingTags.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Exportando...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar Tags")
                }
            }

            Button(
                onClick = {
                    importTagsLauncher.launch(arrayOf("application/json"))
                },
                enabled = !isImportingTags.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImportingTags.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Importando...")
                } else {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar Tags")
                }
            }
        }
    }
}
