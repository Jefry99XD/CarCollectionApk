package com.example.carcollection.featureuser

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.People
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
    val scrollState = rememberScrollState()

    val recentCars by userViewModel.recentCars.collectAsState()
    val achievementCount by userViewModel.achievementCount.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
        userViewModel.fetchUserStats()
        userViewModel.fetchRecentCars()
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


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ─── Foto ───
                    AsyncImage(
                        model = user?.photoUrl ?: "",
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(130.dp)
                    )

                    // ─── Nombre y correo ───
                    Text(
                        text = user?.username ?: "Usuario desconocido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // ─── Bio ───
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
                                user?.bio ?: "“Agrega una Bio”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // ─── 📊 Estadísticas básicas ───
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
                                    user?.totalCars ?: 0
                                )
                                StatItem(Icons.Default.LocalOffer, "Tags", user?.totalTags ?: 0)
                                StatItem(Icons.Default.People, "Amigos", user?.totalFriends ?: 0)
                                StatItem(
                                    Icons.Default.BeachAccess,
                                    "Series",
                                    user?.totalSeries ?: 0
                                )
                                StatItem(Icons.Default.Star, "Logros", achievementCount)
                            }
                        }
                    }


                    // ─── 🕹️ Actividad reciente ───
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
                                        imageUrl = car.photoUrl ?: ""
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



                    // ─── Botones ───
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

// ──────────────────────────────
//  COMPONENTES REUTILIZABLES
// ──────────────────────────────


@Composable
fun RecentCarItem(name: String, year: String, imageUrl: String) {
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
            Text("Serie JDM", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
