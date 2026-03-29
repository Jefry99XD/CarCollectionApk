package com.example.carcollection.featureuser.publicUser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureuser.UserViewModel


@Composable
fun PublicUserCarList(
    uid: String,
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onCarClick: (Car) -> Unit = {}
) {
    val cars by viewModel.publicUserCars.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortDescending by remember { mutableStateOf(true) }

    // ✅ NUEVO: Estado para diálogo de imagen
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    // Cargar autos si no existen todavía
    LaunchedEffect(uid) {
        viewModel.fetchPublicUserCars(uid)
    }

    val filteredCars = cars
        .filter { car ->
            searchQuery.isBlank() || run {
                val q = searchQuery.lowercase()
                listOf(
                    car.brand,
                    car.name,
                    car.serie,
                    car.year,
                    car.color,
                    car.type
                ).any { it?.lowercase()?.contains(q) == true } ||
                        car.tags.any { it.lowercase().contains(q) }
            }
        }
        .sortedBy { it.createdAt ?: 0 }
        .let { if (sortDescending) it.reversed() else it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
            .padding(16.dp)
    ) {
        // HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Colección",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar autos...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        Spacer(Modifier.height(10.dp))

        // SORT BUTTON
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ordenar por fecha",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { sortDescending = !sortDescending }) {
                Icon(
                    imageVector = if (sortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(12.dp))


        // LISTA
        if (filteredCars.isEmpty()) {
            Text(
                text = if (cars.isEmpty()) "No hay autos en esta colección" else "No se encontraron autos con ese filtro",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCars) { car ->
                    PublicCarCard(
                        car = car,
                        onClick = { onCarClick(car) },
                        onImageClick = {
                            selectedImageUrl = car.photoUrl ?: ""
                            showImageDialog = true
                        }
                    )
                }
            }
        }
    }

    // ✅ NUEVO: Diálogo para mostrar la imagen completa del carro
    if (showImageDialog && selectedImageUrl.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón cerrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showImageDialog = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }

                    // Imagen del carro
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedImageUrl)
                            .crossfade(300)
                            .build(),
                        contentDescription = "Carro",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun PublicCarCard(
    car: Car,
    onClick: () -> Unit = {},
    onImageClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // FOTO
            AsyncImage(
                model = car.photoUrl,
                contentDescription = car.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onImageClick() },
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "${car.brand ?: ""} ${car.name ?: ""}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = car.serie ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = car.year ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


