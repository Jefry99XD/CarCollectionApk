package com.example.carcollection.presentation.lateralMenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun UserProfileSection(userName: String, carCount: Int) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // espacio interno
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar de usuario (icono circular)
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f)) // fondo suave
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Nombre + cantidad de carros
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Carros: $carCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}