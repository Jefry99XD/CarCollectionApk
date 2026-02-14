package com.example.carcollection.featureWishlist.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.featureWishlist.domain.WishListViewModel
import com.example.carcollection.featureWishlist.domain.WishlistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicWishlistScreen(
    userId: String,
    username: String,
    viewModel: WishListViewModel,
    onBackClick: () -> Unit
) {
    val publicWishlist by viewModel.publicWishlist.collectAsState()
    val isLoading by viewModel.isLoadingPublic.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchPublicWishlist(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de deseos de $username") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                publicWishlist.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "📋",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                "Lista de deseos vacía",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "$username no tiene carros en su lista de deseos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        items(publicWishlist, key = { it.id }) { wishItem ->
                            PublicWishlistItemCard(wishItem)
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicWishlistItemCard(wishItem: WishlistItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del carro
            AsyncImage(
                model = wishItem.imageUrl,
                contentDescription = wishItem.carName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del carro
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del carro
                Text(
                    text = wishItem.carName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Brand
                if (wishItem.brand.isNotBlank()) {
                    Text(
                        text = "🏢 ${wishItem.brand}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Serie
                if (wishItem.serie.isNotBlank()) {
                    Text(
                        text = "🏖️ ${wishItem.serie}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Prioridad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Prioridad",
                        tint = getPriorityColor(wishItem.priority),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = getPriorityText(wishItem.priority),
                        style = MaterialTheme.typography.bodySmall,
                        color = getPriorityColor(wishItem.priority),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Notas (si existen)
                if (wishItem.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💬 ${wishItem.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "urgent", "urgente" -> Color(0xFFE53935) // Rojo
        "high", "alta" -> Color(0xFFFB8C00) // Naranja
        "medium", "media" -> Color(0xFFFDD835) // Amarillo
        "low", "baja" -> Color(0xFF43A047) // Verde
        else -> Color.Gray
    }
}

private fun getPriorityText(priority: String): String {
    return when (priority.lowercase()) {
        "urgent", "urgente" -> "Urgente"
        "high", "alta" -> "Alta"
        "medium", "media" -> "Media"
        "low", "baja" -> "Baja"
        else -> priority
    }
}

