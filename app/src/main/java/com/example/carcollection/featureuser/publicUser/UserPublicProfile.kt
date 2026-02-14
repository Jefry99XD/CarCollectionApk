package com.example.carcollection.featureuser.publicUser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.featureuser.RecentCarItem
import com.example.carcollection.featureuser.StatItem
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.featureuser.components.BadgeSize
import com.example.carcollection.featureuser.components.LevelBadge
import com.example.carcollection.featureuser.components.XPProgressBar


data class CarPreview(
    val name: String,
    val year: String,
    val photoUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPublicProfile(
    uid: String,
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onViewCollection: () -> Unit,
    onViewAchievements: () -> Unit = {},
    onViewWishlist: () -> Unit = {}
) {
    // 🔄 Observamos los estados del ViewModel
    val publicUser by viewModel.publicUser.collectAsState()
    val publicStats by viewModel.publicStats.collectAsState()
    val publicCars by viewModel.publicRecentCars.collectAsState()

    // 🔄 Cuando se abre el perfil público, cargar datos
    LaunchedEffect(uid) {
        // Clear previous user's data first
        viewModel.clearPublicUserData()
        // Then fetch new user's data
        viewModel.fetchPublicUserProfile(uid)
        viewModel.fetchPublicUserStats(uid)
        viewModel.fetchPublicRecentCars(uid)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(publicUser?.username ?: "Perfil") },
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ============================================================
            // FOTO
            // ============================================================
            AsyncImage(
                model = publicUser?.photoUrl ?: "",
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(130.dp)
            )

            // ============================================================
            // NOMBRE Y NIVEL
            // ============================================================
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = publicUser?.username ?: "Cargando...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                publicUser?.let { user ->
                    LevelBadge(
                        level = user.level,
                        size = BadgeSize.MEDIUM
                    )
                }
            }

            // ============================================================
            // BIO
            // ============================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bio", style = MaterialTheme.typography.titleMedium)
                    Text(
                        publicUser?.bio ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // ============================================================
            // 🎮 NIVEL Y XP
            // ============================================================
            publicUser?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Progreso y Nivel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        XPProgressBar(
                            currentXP = user.currentLevelXP,
                            neededXP = user.xpForNextLevel,
                            level = user.level,
                            showDetailedInfo = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "XP Total: ${String.format("%,d", user.totalXP)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ============================================================
            // 📊 ESTADÍSTICAS
            // ============================================================
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
                            publicStats?.get("cars") ?: 0
                        )
                        StatItem(
                            Icons.Default.BeachAccess,
                            "Series",
                            publicStats?.get("series") ?: 0
                        )
                        StatItem(
                            Icons.Default.Star,
                            "Logros",
                            publicStats?.get("achievements") ?: 0
                        )
                    }

                }
            }

            // ============================================================
            // BOTONES
            // ============================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onViewCollection,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ver colección")
                    }

                    Button(
                        onClick = onViewAchievements,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ver logros")
                    }
                }

                Button(
                    onClick = onViewWishlist,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📋 Ver lista de deseos")
                }
            }

            // ============================================================
            // 🕹️ ACTIVIDAD RECIENTE
            // ============================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Actividad reciente", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // ✅ CÓDIGO CORREGIDO en UserPublicProfile.kt

                    if (publicCars.isNotEmpty()) {
                        publicCars.forEach { car ->
                            RecentCarItem(
                                name = car.name ?: "Sin nombre",
                                year = car.year?.toString()
                                    ?: "Año desconocido",
                                imageUrl = car.photoUrl ?: "",
                                serie = car.serie ?: ""
                            )
                        }
                    } else {
                        Text(
                            "Sin actividad reciente.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                }
            }
        }
    }
}
