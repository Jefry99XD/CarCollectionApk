package com.example.carcollection.presentation.add

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carcollection.data.local.Car
import com.example.carcollection.presentation.main.MainViewModel
import com.example.carcollection.util.ImageSearchUtil
import kotlinx.coroutines.launch

@Composable
fun AddCarScreen(
    viewModel: MainViewModel,
    onCarSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var brand by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

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

                            photoUrl = finalPhoto,
                        )
                        viewModel.insertCar(car)
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
