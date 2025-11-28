package com.example.carcollection.featurestats


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ----------------------------------------------------
// PANTALLA PRINCIPAL DE ESTADÍSTICAS
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsMainScreen(
    onBackClick: () -> Unit,
    onCategoryClick: (StatsCategory) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    "Categorías disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lista de categorías
            items(StatsCategory.values().size) { index ->
                val category = StatsCategory.values()[index]
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// CARD DE CADA CATEGORÍA
// ----------------------------------------------------
@Composable
fun CategoryCard(category: StatsCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ver estadísticas",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}