package com.example.carcollection.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MenuScreen(
    onNavigateToCollection: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToConfig: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Hola Tubas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuButton("Colección", Icons.Filled.CarCrash, onNavigateToCollection)
                    MenuButton("Tags", Icons.Filled.Tag, onNavigateToTags)
                    MenuButton("Consultas", Icons.Default.QueryStats, onNavigateToConsultas)
                    MenuButton("Estadísticas", Icons.Default.BarChart, onNavigateToStatistics)
                    MenuButton("Configuración", Icons.Default.Settings, onNavigateToConfig)
                }
            }


        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Powered by Jefry Cuendiz. V2.0",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MenuButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}