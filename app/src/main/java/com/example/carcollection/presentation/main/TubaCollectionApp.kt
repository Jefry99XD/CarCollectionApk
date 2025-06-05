// TubaCollectionApp.kt
package com.example.carcollection.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.R

@Composable
fun TubaCollectionApp(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior con logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // más alto
                .background(Color(0, 187, 187)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de Tuba Collection",
                modifier = Modifier
                    .height(200.dp) // controla el tamaño del logo
            )
        }

        // Contenido principal
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            content()
        }
    }
}
