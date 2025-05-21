// TubaCollectionApp.kt
package com.example.carcollection.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun TubaCollectionApp(
    navController: NavHostController,
    content: @Composable () -> Unit // <- esto es necesario
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Título superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0, 187, 187)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Tuba Collection",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        // Contenedor gris para el resto del contenido
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            content() // <-- aquí usamos el bloque composable que se pasó
        }
    }
}
