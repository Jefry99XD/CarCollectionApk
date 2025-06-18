package com.example.carcollection.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.carcollection.R

@Composable
fun MenuScreen(
    onNavigateToCollection: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToConsultas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally // ⬅️ centramos todo el contenido
    ) {
        Text(
            "Hola Tubas",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = onNavigateToCollection, modifier = Modifier.fillMaxWidth()) {
            Text("Colección")
        }

        Button(onClick = onNavigateToData, modifier = Modifier.fillMaxWidth()) {
            Text("Datos")
        }

        Button(onClick = onNavigateToTags, modifier = Modifier.fillMaxWidth()) {
            Text("Tags")
        }
        Button(onClick = onNavigateToConsultas, modifier = Modifier.fillMaxWidth()) {
            Text("Consultas")
        }

    }

}