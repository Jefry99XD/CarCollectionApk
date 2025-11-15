package com.example.carcollection.featurecar.presentation.add_edit_car.add

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featuretags.domain.Tag
import com.example.carcollection.utils.ImageSearchUtil
import kotlinx.coroutines.launch

@Composable
fun AddCarScreen(
    viewModel: CarViewModel,
    onCarSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var brand by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }


    val allTags by viewModel.allTags.collectAsState()

    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(viewModel.currentCar) {
        viewModel.currentCar?.let { car ->
            brand = car.brand.toString()
            name = car.name.toString()
            serie = car.serie.toString()
            year = car.year.toString()
            color = car.color.toString()
            photo = car.photoUrl.toString()
            type = car.type.toString()
            selectedTags = car.tags
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del carro") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = serie,
            onValueChange = { serie = it },
            label = { Text("Serie") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Año") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = photo,
            onValueChange = { photo = it },
            label = { Text("URL de la foto (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Tags")
        Column {
            allTags.forEach { tag : Tag ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isSelected = selectedTags.contains(tag.name)
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            selectedTags = if (checked) {
                                selectedTags + tag.name
                            } else {
                                selectedTags - tag.name
                            }
                        }
                    )
                    Text(tag.name)
                }
            }
        }



        Button(
            onClick = {
                if (brand.isNotBlank() && name.isNotBlank() && serie.isNotBlank() && year.isNotBlank()) {
                    coroutineScope.launch {
                        val finalPhoto = if (photo.isBlank()) {
                            ImageSearchUtil.searchImageUrl("$brand $name $serie $year") ?: ""
                        } else {
                            photo
                        }
                        Log.d("AddCarScreen", "URL de imagen buscada: $finalPhoto")
                        val car = Car(
                            brand = brand,
                            name = name,
                            serie = serie,
                            color = color,
                            year = year,
                            type = type,
                            tags = selectedTags,
                            createdAt = System.currentTimeMillis(),

                            photoUrl = finalPhoto,
                        )
                        viewModel.addCar(car)
                        Toast.makeText(context, "Carro guardado", Toast.LENGTH_SHORT).show()
                        onCarSaved()
                    }
                } else {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
