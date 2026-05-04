package com.example.carcollection.featureuser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carcollection.featureuser.components.LevelCard
import com.example.carcollection.presentation.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMain(
    userViewModel: UserViewModel,
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit,
    navController: NavController
) {
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()

    val recentCars by userViewModel.recentCars.collectAsState()
    val favoriteCars by userViewModel.favoriteCars.collectAsState()
    val carCount by userViewModel.carCount.collectAsState()
    val tagCount by userViewModel.tagCount.collectAsState()
    val achievementCount by userViewModel.achievementCount.collectAsState()
    val seriesCount by userViewModel.seriesCount.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
        userViewModel.fetchUserStats()
        userViewModel.fetchRecentCars()
        userViewModel.fetchFavoriteCars()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${errorMessage ?: "Ocurrió un error inesperado"}")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ─── Foto ───
                    item {
                        AsyncImage(
                            model = user?.photoUrl ?: "",
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(130.dp)
                        )
                    }

                    // ─── Nombre y correo ───
                    item {
                        Text(
                            text = user?.username ?: "Usuario desconocido",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    // ─── Bio ───
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Bio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    user?.bio ?: "Agrega una Bio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // ─── 🎮 Sistema de Niveles ───
                    item {
                        user?.let { currentUser ->
                            LevelCard(
                                level = currentUser.level,
                                totalXP = currentUser.totalXP,
                                currentLevelXP = currentUser.currentLevelXP,
                                xpForNextLevel = currentUser.xpForNextLevel,
                                xpFromCars = currentUser.xpFromCars,
                                xpFromAchievements = currentUser.xpFromAchievements
                            )
                        }
                    }

                    // ─── 📊 Estadísticas básicas ───
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Estadísticas", style = MaterialTheme.typography.titleMedium)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem(
                                        Icons.Default.DirectionsCar,
                                        "Carros",
                                        carCount
                                    )
                                    StatItem(Icons.Default.LocalOffer, "Tags", tagCount)
                                    StatItem(
                                        Icons.Default.BeachAccess,
                                        "Series",
                                        seriesCount
                                    )
                                    StatItem(Icons.Default.Star, "Logros", achievementCount)
                                }
                            }
                        }
                    }

                    // ─── ❤️ Carros Favoritos ───
                    if (favoriteCars.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Favoritos",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Mis Favoritos (${favoriteCars.size}/10)", style = MaterialTheme.typography.titleMedium)
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    // Grid 2 columnas
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        var currentRow = 0
                                        while (currentRow < favoriteCars.size) {
                                            val car1 = favoriteCars.getOrNull(currentRow)
                                            val car2 = favoriteCars.getOrNull(currentRow + 1)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (car1 != null) {
                                                    FavoriteCarCard(car = car1, modifier = Modifier.weight(1f).height(180.dp))
                                                }
                                                if (car2 != null) {
                                                    FavoriteCarCard(car = car2, modifier = Modifier.weight(1f).height(180.dp))
                                                } else if (car1 != null) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                            currentRow += 2
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── 🕹️ Actividad reciente ───
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Actividad reciente", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))

                                if (recentCars.isNotEmpty()) {
                                    recentCars.forEach { car ->
                                        RecentCarItem(
                                            name = car.name ?: "Sin nombre",
                                            year = car.year?.toString() ?: "Año desconocido",
                                            imageUrl = car.photoUrl ?: "",
                                            serie = car.serie ?: ""
                                        )
                                    }
                                } else {
                                    Text(
                                        "Aún no has agregado autos recientes.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // ─── Botones ───
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editar perfil")
                            }

                            OutlinedButton(
                                onClick = {
                                    userViewModel.logoutUser()
                                    navController.navigate(NavRoutes.MENU)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Cerrar sesión"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cerrar sesión")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────
//  COMPONENTES REUTILIZABLES
// ──────────────────────────────


@Composable
fun RecentCarItem(name: String, year: String, imageUrl: String, serie: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("$year - $name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (serie.isNotBlank()) {
                Text(serie, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                Text("Sin serie", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}


@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .padding(6.dp)
        )
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}


@Composable
fun FavoriteCarCard(car: com.example.carcollection.featurecar.domain.Car, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Imagen del carro
            AsyncImage(
                model = car.photoUrl ?: "",
                contentDescription = "${car.brand} ${car.name}",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Nombre del carro
            Text(
                text = car.name ?: "Sin nombre",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                textAlign = TextAlign.Center
            )

            // Serie completa
            if (car.serie?.isNotBlank() == true) {
                Text(
                    text = car.serie ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }

            // Año
            Text(
                text = "${car.year ?: "?"}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}
