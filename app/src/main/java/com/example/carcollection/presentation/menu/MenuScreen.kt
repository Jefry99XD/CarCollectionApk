package com.example.carcollection.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MenuScreen(
    onNavigateToCollection: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToStatistics: () -> Unit

) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                "Hola Tubas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        item {
            Button(
                onClick = onNavigateToCollection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Colección")
            }
        }
        item {
            Button(
                onClick = onNavigateToData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Datos")
            }
        }
        item {
            Button(
                onClick = onNavigateToTags,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tags")
            }
        }
        item {
            Button(
                onClick = onNavigateToConsultas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Consultas")
            }
        }
        item {
            Button(
                onClick = onNavigateToStatistics,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Estadisticas")
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "Powered by Jefry Cuendiz. V1.2",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}