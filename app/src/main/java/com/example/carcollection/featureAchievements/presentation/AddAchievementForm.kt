package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcollection.featureAchievements.domain.AchievementCondition
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.AchievementType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAchievementForm(
    viewModel: AchievementViewModel,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    // ─── Campos para condiciones ───
    var selectedType by remember { mutableStateOf(AchievementType.GENERAL) }
    var tag by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var namesList by remember { mutableStateOf("") }


    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar logro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─── Campos básicos ───
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del logro") },
                modifier = Modifier.fillMaxWidth(),
                isError = title.isBlank()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = iconUrl,
                onValueChange = { iconUrl = it },
                label = { Text("URL del ícono (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // ─── Tipo de logro ───
            Text("Tipo de logro", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedType.name)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AchievementType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ─── Campos condicionales según el tipo ───
            when (selectedType) {
                AchievementType.TAG -> {
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Tag objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AchievementType.SERIE -> {
                    OutlinedTextField(
                        value = serie,
                        onValueChange = { serie = it },
                        label = { Text("Serie objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AchievementType.COLOR -> {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AchievementType.BRAND -> {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AchievementType.YEAR -> {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Año objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AchievementType.LIST_BY_NAME -> {
                    OutlinedTextField(
                        value = namesList,
                        onValueChange = {
                            namesList = it

                            // --- Actualizar goal automáticamente ---
                            val count = it.split(",")
                                .map { s -> s.trim() }
                                .filter { s -> s.isNotEmpty() }
                                .size

                            goal = count.toString()
                        },
                        label = { Text("Lista de nombres (separados por coma)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }



                else -> {}
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = goal,
                onValueChange = {
                    if (selectedType != AchievementType.LIST_BY_NAME) {
                        goal = it
                    }
                },
                label = {
                    Text(
                        if (selectedType == AchievementType.LIST_BY_NAME)
                            "Meta (generada automáticamente)"
                        else
                            "Meta numérica (por ejemplo, 10 autos)"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedType != AchievementType.LIST_BY_NAME, // ⛔ no editable en LIST_BY_NAME
                readOnly = selectedType == AchievementType.LIST_BY_NAME // evita edición por teclado
            )


            Spacer(Modifier.height(20.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            if (successMessage.isNotEmpty()) {
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            // ─── Botón guardar ───
            Button(
                onClick = {
                    errorMessage = ""
                    successMessage = ""

                    if (title.isBlank() || goal.isBlank()) {
                        errorMessage = "El título y la meta son obligatorios."
                        return@Button
                    }

                    scope.launch {
                        try {
                            val newAchievement = AchievementGlobal(
                                id = "",
                                title = title.trim(),
                                description = description.trim(),
                                iconUrl = iconUrl.trim(),
                                goal = goal.toIntOrNull() ?: 0,
                                type = selectedType,
                                condition = AchievementCondition(
                                    tag = tag.ifBlank { null },
                                    serie = serie.ifBlank { null },
                                    color = color.ifBlank { null },
                                    brand = brand.ifBlank { null },
                                    year = year.ifBlank { null },
                                    namesList = namesList.ifBlank { null }
                                    ),
                                createdAt = System.currentTimeMillis()
                            )

                            viewModel.addAchievement(newAchievement)

                            successMessage = "Logro agregado correctamente."
                            title = ""
                            description = ""
                            iconUrl = ""
                            goal = ""
                            tag = ""
                            serie = ""
                            color = ""
                            brand = ""
                            year = ""
                            namesList = ""
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Error desconocido"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar logro")
                }
            }
        }
    }
}